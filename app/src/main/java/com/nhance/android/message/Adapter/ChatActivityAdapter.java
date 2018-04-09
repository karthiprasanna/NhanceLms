package com.nhance.android.message.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.message.ChatItem;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class ChatActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{



    private Context context;
    private LayoutInflater inflater;
    private List<ChatItem> feedsList;
    private ChatItem chatItem = new ChatItem();
    List<ChatItem> data= Collections.emptyList();
   // private List<ChatItem> msg = new ArrayList<>();
    int currentPos=0;
    ProgressDialog pd;
    private static final int VIEW_HOLDER_RIGHT = 0;
    private static final int VIEW_HOLDER_LEFT = 1;

    public ChatActivityAdapter(Context context, List<ChatItem> data){
        pd = new ProgressDialog(context);
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
        this.notifyDataSetChanged();


    }
    // Inflate the layout when viewholder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.chatactivity_list_item_msg, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind data

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {



        MyHolder myHolder= (MyHolder) holder;
        final ChatItem current=data.get(position);
        if(current.isMe == true){
            System.out.println("Hello World "+chatItem.isMe);
            myHolder.relativelayoutleft.setVisibility(View.GONE);
            myHolder.username.setText(current.firstName);
            //myHolder.content.setText(Html.fromHtml(current.content));
            myHolder.content.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + current.content.replace("null",""), "text/html", "UTF-8", null);
            // myHolder.content.loadDataWithBaseURL(null, current.content, "text/html", "UTF-8", null);
            myHolder.timestamp.setText(getDate(current.timestamp));
            new DownloadImageTask(myHolder.thumbnail).execute(current.thumbnail);

        }else {
            System.out.println("Hello World "+chatItem.isMe);
            myHolder.relativelayoutright.setVisibility(View.GONE);
            myHolder.usernameleft.setText(current.firstName);
           // myHolder.contentleft.setText(Html.fromHtml(current.content));
            myHolder.contentleft.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + current.content.replace("null",""), "text/html", "UTF-8",null);
            myHolder.timestampleft.setText(getDate(current.timestamp));
            new DownloadImageTask(myHolder.thumbnailleft).execute(current.thumbnail);
        }




       /* myHolder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailIntent = new Intent(v.getContext(), ChatActivity.class);
                detailIntent.putExtra("conversationId", current.conversationId);
                detailIntent.putExtra("userConversationId", current.userConversationId);
                detailIntent.putExtra("firstMessageId", current.firstMessageId);

                //Cannot resolve method
                System.out.println("Url ---"+current.conversationId+ " "+current.firstMessageId);
                context.startActivity(detailIntent);
            }
        });
*/

    }

    @Override
    public void onClick(View view) {

    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            /*pd.setTitle("Loading.... ");
            pd.show();*/
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            pd.dismiss();
            bmImage.setImageBitmap(result);
        }
    }


    public String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        System.currentTimeMillis();
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy hh:mm a", cal).toString();
        return date;
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }

    // return total item from List
    class MyHolder extends RecyclerView.ViewHolder  {
        TextView username,usernameleft;
        WebView content,contentleft;
        ImageView thumbnail,thumbnailleft;
        TextView timestamp,timestampleft;
        Button send;
        EditText editText;
        RecyclerView lv;
        RelativeLayout relativelayoutright,relativelayoutleft;


        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            content = (WebView) itemView.findViewById(R.id.lmsg);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            timestamp = (TextView) itemView.findViewById(R.id.time);
            lv = (RecyclerView) itemView.findViewById(R.id.recycler_view);
            usernameleft = (TextView) itemView.findViewById(R.id.usernameleft);
            contentleft = (WebView) itemView.findViewById(R.id.msgleft);
            thumbnailleft = (ImageView) itemView.findViewById(R.id.thumbnailleft);
            timestampleft = (TextView) itemView.findViewById(R.id.timeleft);
            relativelayoutright = (RelativeLayout) itemView.findViewById(R.id.re1);
            relativelayoutleft = (RelativeLayout) itemView.findViewById(R.id.re2);

            editText = (EditText) itemView.findViewById(R.id.Etext);
            send = (Button) itemView.findViewById(R.id.chatSendButton);
            // rl = (RelativeLayout) itemView.findViewById(R.id.header);
            // username.setOnClickListener(this);

            content.getSettings().setJavaScriptEnabled(true);
            WebSettings settings = content.getSettings();
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setMinimumFontSize(60);
            content.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            content.setBackgroundColor(Color.TRANSPARENT);
            content.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    view.scrollTo(150,150);
                }
            });

            contentleft.getSettings().setJavaScriptEnabled(true);
            WebSettings settings1 = contentleft.getSettings();
            settings1.setUseWideViewPort(true);
            settings1.setLoadWithOverviewMode(true);
            settings1.setMinimumFontSize(60);
            contentleft.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            contentleft.setBackgroundColor(Color.TRANSPARENT);
            contentleft.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    view.scrollTo(150,150);
                }
            });

        }
    }
}
