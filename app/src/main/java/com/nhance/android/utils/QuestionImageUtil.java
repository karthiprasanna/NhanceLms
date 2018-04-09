package com.nhance.android.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.FileManager;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.StorageHelper;

public class QuestionImageUtil {

    private static final String IMG_IDENTIFIER = "v-uid";
    private static final String IMG_SRC        = "src";
    private static final String TAG            = "QuestionImageUtil";

    public static String annotateQuestionContentWithHtml(String qusContent) {

        return TextUtils.htmlEncode(LocalManager.formatMathJaxString(QuestionImageUtil
                .addImageSrcUrl(EntityType.QUESTION, qusContent)));
    }

    public static String annotateQuestionOptionsWithHtml(String options) {

        String htmlOptions = QuestionImageUtil.addImageSrcUrl(EntityType.QUESTION, options);
        return LocalManager.joinString(Arrays.asList(TextUtils.split(
                LocalManager.formatMathJaxString(htmlOptions), SQLDBUtil.SEPARATOR)), "'", true);

    }

    public static String addImageSrcUrl(EntityType entityType, String html) {

        if (StringUtils.isEmpty(html)) {
            return LocalManager.EMPTY_TEXT;
        }
        return html.replace("\n", LocalManager.EMPTY_TEXT);
    }

    // update accordingly to current structure
    public static String removeImageSrcUrl(String html, Set<String> urls, EntityType entityType) {
        String pPath  = StorageHelper.getStorages(false).get(0).file.getAbsolutePath();
        String lCommonFile = pPath.replace(FileManager.MOUNT_PATH+"/", "");
         if (TextUtils.isEmpty(html)) {
            Log.d(TAG, "Question content HTML is empty");

            return LocalManager.EMPTY_TEXT;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();

        while (it.hasNext()) {
            Element es = it.next();
            String uuid = es.attr(IMG_IDENTIFIER);// v-uid=uuid
            String url = es.attr(IMG_SRC);
            Log.d(TAG, "uuid " + uuid + ", imgSrc: " + url);
            urls.add(url);
            es.attr(IMG_SRC, StringUtils.join(
                    new String[] { FileManager.baseUrl, lCommonFile, "nhance",
                            entityType.name().toLowerCase(),
                            StringUtils.substringAfterLast(url, File.separator) }, File.separator));
        }
           return doc.body().html();
    }

    // private static String getImgUrl(EntityType entityType, String dataUid) {
    //
    // String url = StringUtils.join(new String[] { FileManager.baseUrl, "vedantu",
    // entityType.name().toLowerCase(), dataUid + ".jpg" }, File.separator);
    // return url;
    // }
}
