package com.nhance.android.activities.content.players;



import android.os.Bundle;
import com.nhance.android.R;
import android.app.Activity;
import android.util.Log;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfActivity extends Activity implements OnPageChangeListener,OnLoadCompleteListener{
    private static final String TAG = PdfActivity.class.getSimpleName();
    //        public static final String SAMPLE_FILE = "android_tutorial.pdf";
    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    String sdofoi;
    File fle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        ArrayList<File> documents = (ArrayList<File>)getIntent().getSerializableExtra("pdffile");
        fle  = documents.get(0);
        //sdofoi =getIntent().getStringExtra("pdffile");
        Log.e("pdffffffff",fle.getPath());


        pdfView= (PDFView)findViewById(R.id.pdfView);
        //File file = new File(sdofoi);
        pdfView.fromFile(fle)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)

                .scrollHandle(new DefaultScrollHandle(this))
                .load();

    }
    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }






}
/*

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
    }
}
*/
