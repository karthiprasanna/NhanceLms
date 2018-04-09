package com.nhance.android.managers;
//package com.vedantu.android.managers;
//
//import android.text.format.DateUtils;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.EditText;
//import android.widget.PopupWindow;
//import android.widget.RadioGroup;
//import android.widget.RadioGroup.OnCheckedChangeListener;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.android.vedantu.dlp.ConstantGlobal;
//import com.android.vedantu.dlp.R;
//import com.android.vedantu.dlp.db.datamanager.UserActivityDataManager;
//import com.android.vedantu.dlp.db.models.Note;
//import com.android.vedantu.dlp.enums.NoteType;
//import com.android.vedantu.dlp.utils.ViewUtils;
//
//public class NotePopupManager {
//
//    private static final String TAG = "NotePopupManager";
//
//    public enum PopupViewMode {
//        VIEW {
//
//            @Override
//            public int getViewLayout() {
//
//                return R.layout.view_note_popup;
//            }
//        },
//        ADD {
//
//            @Override
//            public int getViewLayout() {
//
//                return R.layout.add_note_popup;
//            }
//        };
//
//        public abstract int getViewLayout();
//    }
//
//    private PopupWindow             popUpAddNote;
//    private EditText                addNoteInput;
//    private LayoutInflater          inflator;
//    private UserActivityDataManager dataManager;
//    private INotesUpdater           notesUpdater;
//    private String                  name;                    // heading of the content//contentName
//    private String                  entityId;
//    private String                  progId;
//    private String                  entityType;
//    private NoteType                noteType;
//    private String                  contentType;
//    private String                  courseBrdId;
//    private String                  courseBrdName;
//    private String                  thumb;
//    private String                  by;
//    private PopupViewMode           mode = PopupViewMode.ADD;
//
//    public NotePopupManager(LayoutInflater inflator, UserActivityDataManager dataManager,
//            INotesUpdater notesUpdater) {
//
//        this.inflator = inflator;
//        this.dataManager = dataManager;
//        this.notesUpdater = notesUpdater;
//    }
//
//    public void setMode(PopupViewMode mode) {
//
//        this.mode = mode;
//    }
//
//    public void opneAddNotePopup(View parent) {
//
//        View popupLayout = inflator.inflate(mode.getViewLayout(), null);
//        popUpAddNote = new PopupWindow(popupLayout);
//        popUpAddNote.setHeight(LayoutParams.MATCH_PARENT);
//        popUpAddNote.setWidth(LayoutParams.MATCH_PARENT);
//        popUpAddNote.setFocusable(true);
//        popUpAddNote.setTouchable(true);
//        popUpAddNote.showAtLocation(parent, Gravity.CENTER, 0, 0);
//
//        View closePopUp = popupLayout.findViewById(R.id.add_note_popup_close);
//        closePopUp.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                closeAddNotePopup();
//            }
//        });
//        if (mode == PopupViewMode.ADD) {
//            setUpAddNoteView(popupLayout);
//        } else {
//            setUpViewNoteView(popupLayout);
//        }
//    }
//
//    Note viewNote;
//
//    public void opneViewNotePopup(View parent, Note note) {
//
//        viewNote = note;
//        mode = PopupViewMode.VIEW;
//        opneAddNotePopup(parent);
//    }
//
//    public void closeAddNotePopup() {
//
//        if (popUpAddNote != null) {
//            popUpAddNote.dismiss();
//        }
//        if (addNoteInput != null) {
//            addNoteInput.setText(LocalManager.EMPTY_TEXT);
//        }
//    }
//
//    private OnClickListener getSaveNoteClickListner() {
//
//        return new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                String desc = addNoteInput.getText().toString();
//                if (desc == null || desc.length() < 10) {
//                    Toast.makeText(inflator.getContext(), "Note should have min 10 characters",
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                closeAddNotePopup();
//                Note note = new Note(name, SessionManager.getInstance(inflator.getContext())
//                        .getSessionStringValue(ConstantGlobal.USER_ID), progId, noteType, entityId,
//                        entityType, contentType, thumb, desc, courseBrdName, courseBrdId, by);
//                try {
//                    dataManager.insertNote(note);
//                    Toast.makeText(inflator.getContext(), "Successfully added note",
//                            Toast.LENGTH_SHORT).show();
//                    if (notesUpdater != null) {
//                        notesUpdater.updateNotes();
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, e.getMessage(), e);
//                }
//            }
//        };
//    }
//
//    public void setNoteParams(String name, String progId, NoteType noteType, String entityId,
//            String entityType, String contentType, String thumb, String courseBrdName,
//            String courseBrdId, String by) {
//
//        this.name = name;
//        this.progId = progId;
//        this.entityId = entityId;
//        this.entityType = entityType;
//        this.noteType = noteType;
//        this.contentType = contentType;
//        this.thumb = thumb;
//        this.courseBrdName = courseBrdName;
//        this.courseBrdId = courseBrdId;
//        this.by = by;
//    }
//
//    private void setUpAddNoteView(View popupLayout) {
//
//        popupLayout.findViewById(R.id.save_note).setOnClickListener(getSaveNoteClickListner());
//        addNoteInput = (EditText) popupLayout.findViewById(R.id.add_note_input);
//
//        RadioGroup radioGroup = (RadioGroup) popupLayout.findViewById(R.id.my_notes_filter);
//        View personalNote = popupLayout.findViewById(R.id.note_popup_personal_note_text);
//        // if (noteType == NoteType.PERSONAL) {
//        // personalNote.setVisibility(View.VISIBLE);
//        // radioGroup.setVisibility(View.GONE);
//        // } else {
//        noteType = NoteType.IMPORTANT;
//        personalNote.setVisibility(View.GONE);
//
//        radioGroup.setVisibility(View.VISIBLE);
//        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//                noteType = NoteType.IMPORTANT;
//                if (checkedId == R.id.my_notes_filter_doubt) {
//                    noteType = NoteType.DOUBT;
//                } else if (checkedId == R.id.my_notes_filter_revision) {
//                    noteType = NoteType.REVISION;
//                }
//            }
//        });
//        // }
//    }
//
//    private void setUpViewNoteView(View popupLayout) {
//
//        NotePopupManager.setUpNoteGridView(viewNote, popupLayout);
//        viewNote.lastViewed = String.valueOf(System.currentTimeMillis());
//        final EditText noteText = (EditText) popupLayout.findViewById(R.id.note_text);
//        noteText.setFocusable(false);
//        popupLayout.findViewById(R.id.view_note_popup_edit_note).setOnClickListener(
//                new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//
//                        TextView view = (TextView) v;
//                        String opt = view.getText().toString();
//                        if (opt.equals(v.getContext().getResources().getString(R.string.edit_note))) {
//                            noteText.setFocusableInTouchMode(true);
//                            view.setText(v.getContext().getResources()
//                                    .getString(R.string.save_note));
//                        } else if (opt.equals(v.getContext().getResources()
//                                .getString(R.string.save_note))) {
//                            noteText.setFocusable(false);
//                            try {
//                                viewNote.desc = noteText.getText().toString();
//                                new UserActivityDataManager(v.getContext()).updatetNote(viewNote);
//                            } catch (Exception e) {
//                                Log.e(TAG, e.getMessage(), e);
//                            }
//                            closeAddNotePopup();
//                            if (notesUpdater != null) {
//                                notesUpdater.updateNotes();
//                            }
//                            view.setText(v.getContext().getResources()
//                                    .getString(R.string.edit_note));
//                        }
//                    }
//                });
//
//    }
//
//    public interface INotesUpdater {
//
//        public void updateNotes();
//    }
//
//    public static void setUpNoteGridView(Note note, View parent) {
//
//        View personalView = parent.findViewById(R.id.note_personal);
//        View videoView = parent.findViewById(R.id.note_video);
//        View documentView = parent.findViewById(R.id.note_document);
//        personalView.setVisibility(View.GONE);
//        videoView.setVisibility(View.GONE);
//        documentView.setVisibility(View.GONE);
//        personalView.setVisibility(View.VISIBLE);
//
//        // EntityType entityType = note.entityType == null ? null : EntityType
//        // .valueOf(note.entityType);
//
//        NoteType noteType = NoteType.valueOfKey(note.noteType);
//        // TextView noteTypeView = (TextView) parent.findViewById(R.id.note_type);
//        TextView noteTypeView = (TextView) personalView;
//
//        noteTypeView.setText(noteType.getDisplayName(parent.getContext()));
//        noteTypeView.setCompoundDrawablesWithIntrinsicBounds(noteType.getDrawableLeft(), 0, 0, 0);
//        //
//        // if (entityType == EntityType.VIDEO) {
//        // videoView.setVisibility(View.VISIBLE);
//        // ImageView videoThumb = (ImageView) videoView.findViewById(R.id.video_thum);
//        // VideoView.setVideoThumb(note.entityId, note.thumb,
//        // ContentType.valueOfKey(note.contentType), videoThumb);
//        // setNoteContentInfo(videoView.findViewById(R.id.content_info_container), note);
//        // } else if (entityType == EntityType.DOCUMENT) {
//        // documentView.setVisibility(View.VISIBLE);
//        // ImageView docThumb = (ImageView) documentView.findViewById(R.id.doc_thum);
//        // // ContentResponse rsp = FileManager.computeDisplayUrlComponent(parent.getContext(),
//        // // EntityType.DOCUMENT, note.entityId, note.thumb, MediaType.IMAGE,
//        // // FileCategory.CONVERTED, ImageSize.SMALL, null, null);
//        // // if (rsp.errorCode == null) {
//        // // LocalManager.downloadImage(rsp.url, docThumb);
//        // // }
//        // setNoteContentInfo(videoView.findViewById(R.id.content_info_container), note);
//        // } else {
//        // personalView.setVisibility(View.VISIBLE);
//        // }
//        ViewUtils.setTextViewValue(parent, R.id.note_text, note.desc);
//        ViewUtils.setTextViewValue(parent, R.id.note_time_taken, DateUtils
//                .getRelativeTimeSpanString(Long.valueOf(note.timeCreated)).toString());
//    }
//
//    @SuppressWarnings("unused")
//    // TODO: enable personal note
//            private static
//            void setNoteContentInfo(View container, Note note) {
//
//        ViewUtils.setTextViewValue(container, R.id.course_name, note.courseBrdName);
//        ViewUtils.setTextViewValue(container, R.id.my_note_content_name, note.name);
//        ViewUtils.setTextViewValue(container, R.id.my_note_content_by, note.by);
//        NoteType noteType = NoteType.valueOfKey(note.noteType);
//        TextView noteTypeView = (TextView) container.findViewById(R.id.note_type);
//        noteTypeView.setText(noteType.getDisplayName(container.getContext()));
//        noteTypeView.setCompoundDrawablesWithIntrinsicBounds(noteType.getDrawableLeft(), 0, 0, 0);
//    }
//}
