package com.nhance.android.adapters.library;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhance.android.enums.EntityType;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.pojos.content.infos.ModuleBasicInfo;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class LibraryContentAdapter extends ArrayAdapter<LibraryContentRes> {

    private List<LibraryContentRes>      items;
    private int                          layoutResource;
    private Set<String>                  attemptedTestIds;
    private OnClickListener              onInfoClickListner;
    private EntityType                   activeEntityType;
    private String                       firstModuleLinkId;
    private String                       firstNonModuleLinkId;
    private OnClickListener              libraryContentClickListner;
    private Map<EntityType, Set<String>> activeSDCardContent;

    public LibraryContentAdapter(Context context, int layoutResource,
                                 List<LibraryContentRes> items, OnClickListener onInfoClickListner,
                                 Map<EntityType, Set<String>> activeSDCardContent) {

        super(context, layoutResource, items);
        this.items = items;
        this.layoutResource = layoutResource;
        this.onInfoClickListner = onInfoClickListner;
        this.libraryContentClickListner = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                LibraryUtils.onLibraryItemClickListnerImpl(getContext(),
                        (LibraryContentRes) v.getTag());
            }
        };
        this.activeSDCardContent = activeSDCardContent;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            layoutResource = R.layout.list_item_view_library_content;
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layoutResource, parent, false);
        }

        ImageView showContentInfo = (ImageView) view.findViewById(R.id.library_item_info);
        showContentInfo.setOnClickListener(onInfoClickListner);
        final LibraryContentRes contentRes = items != null ? items.get(position) : null;

        if (contentRes == null
                || (contentRes.type.equals(EntityType.COMPOUNDMEDIA.name()) && !Boolean
                .parseBoolean(SessionManager.getConfigProperty("show.compoundmedia.demo")))) {
            view.setVisibility(View.GONE);
        } else {

            showContentInfo.setVisibility(contentRes.type.equals(EntityType.COMPOUNDMEDIA.name()) ? View.INVISIBLE
                    : View.VISIBLE);

            populateView(contentRes, view);
            showContentInfo.setTag(position);
        }
        View clickView = view.findViewById(R.id.library_content_info_container);
        clickView.setTag(contentRes);
        clickView.setOnClickListener(libraryContentClickListner);
        return view;
    }

    public void setAttemptedTestIds(Set<String> attemptedTestIds) {

        this.attemptedTestIds = attemptedTestIds;
    }

    public void setActiveEntityType(EntityType type) {

        this.activeEntityType = type;
    }

    public void setFirstModuleAndNonModuleIds(String firstModuleLinkId, String firstNonModuleLinkId) {

        this.firstModuleLinkId = firstModuleLinkId;
        this.firstNonModuleLinkId = firstNonModuleLinkId;
    }

    private void populateView(LibraryContentRes contentRes, View view) {

        String contentType = contentRes.type;
        TextView titleView = (TextView) view.findViewById(R.id.library_item_title);
        TextView statsView = (TextView) view.findViewById(R.id.library_item_stats);

        titleView.setText(contentRes.name);
        // FontUtils.setTypeface(titleView, FontTypes.ROBOTO_CONDENSED);

        EntityType type = EntityType.valueOfKey(contentType);
        TextView headView = ((TextView) view.findViewById(R.id.library_content_head));
        if (activeEntityType.equals(EntityType.ALL)) {
            if (StringUtils.equals(firstModuleLinkId, contentRes.linkId)) {
                headView.setVisibility(View.VISIBLE);
                headView.setText(R.string.modules_head);
            } else if (StringUtils.equals(firstNonModuleLinkId, contentRes.linkId)) {
                headView.setVisibility(View.VISIBLE);
                headView.setText(R.string.content_head);
            } else {
                headView.setVisibility(View.GONE);
            }
        } else {
            headView.setVisibility(View.GONE);
        }

        ImageView thumbnailView = (ImageView) view.findViewById(R.id.library_item_image);

        boolean isModuleContentOnSDCard = false;

        if (EntityType.MODULE.equals(contentRes.type) && contentRes.downloadableEntities != null
                && activeSDCardContent != null && activeSDCardContent.get(type) != null) {
            for (SrcEntity entity : contentRes.downloadableEntities) {
                if (isModuleContentOnSDCard = activeSDCardContent.get(entity.type).contains(
                        entity.id)) {
                    break;
                }
            }
        }

        View sdCardIndicator = view.findViewById(R.id.library_item_image_sdcard);
        if (activeSDCardContent != null
                && activeSDCardContent.get(type) != null
                && (activeSDCardContent.get(type).contains(contentRes.id) || isModuleContentOnSDCard)
                && !"ADDED".equals(contentRes.subType)) {
            sdCardIndicator.setVisibility(View.VISIBLE);
        } else {
            sdCardIndicator.setVisibility(View.GONE);
        }

        if (type == EntityType.TEST && attemptedTestIds != null
                && attemptedTestIds.contains(contentRes.id)) {
            thumbnailView.setImageResource(R.drawable.icon_test_attempted);
        } else {
            thumbnailView.setImageResource(type.icon_res_id);
        }
        LibraryUtils.addStarViewStatus(view, contentRes.starred);
        view.setTag(contentRes);
        view.findViewById(R.id.library_content_info_container).setTag(contentRes);
        LibraryUtils.setStatsView(getContext(), contentRes, statsView, type, false);
        if (type.equals(EntityType.MODULE)) {
            // View progressBarHolder = view
            // .findViewById(R.id.library_content_progress_bar_holder);
            // progressBarHolder.setVisibility(View.VISIBLE);
//            view.findViewById(R.id.library_item_image).setBackgroundResource(R.color.slpgreen);
            ModuleBasicInfo moduleBasicInfo = (ModuleBasicInfo) contentRes.toContentBasicInfo();
            int percent = moduleBasicInfo.totalContentCount == 0 ? 0
                    : moduleBasicInfo.consumedContentCount * 100
                    / moduleBasicInfo.totalContentCount;
            int width = Math.round(ViewUtils.dpToPx(100, getContext()) * percent / 100);
            View progressBar = view.findViewById(R.id.library_content_module_progress);
            LayoutParams params = progressBar.getLayoutParams();
            params.width = width;
            progressBar.setLayoutParams(params);
            ((ImageView) view.findViewById(R.id.library_item_image_sdcard))
                    .setImageResource(R.drawable.sdcard_slp);
        } else {
            view.findViewById(R.id.library_content_progress_bar_holder).setVisibility(View.GONE);
            view.findViewById(R.id.library_item_image).setBackgroundResource(R.color.lightestgrey);
            ((ImageView) view.findViewById(R.id.library_item_image_sdcard))
                    .setImageResource(R.drawable.sdcard);
        }
    }
}
