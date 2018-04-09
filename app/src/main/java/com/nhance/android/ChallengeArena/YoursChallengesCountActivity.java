package com.nhance.android.ChallengeArena;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.utils.FontUtils;

public class YoursChallengesCountActivity extends AppCompatActivity {

    private String channelId;
    private TextView closed_challengetext,result_awaited_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_yours_challenges_count);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Your Challenges");
        channelId=getIntent().getStringExtra("channel_id");

        CardView result_awaited=(CardView)findViewById(R.id.card_view_resultawaited);
        result_awaited_text=(TextView)findViewById(R.id.result_awaited_text);
        closed_challengetext=(TextView)findViewById(R.id.closed_challengetext);
        FontUtils.setTypeface(closed_challengetext, FontUtils.FontTypes.ROBOTO_LIGHT);
        FontUtils.setTypeface(result_awaited_text, FontUtils.FontTypes.ROBOTO_LIGHT);

        result_awaited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(YoursChallengesCountActivity.this,ResultAwaitedActivity.class);
                intent.putExtra("channel_id",channelId);
                startActivity(intent);

            }
        });

        CardView closed_challenges=(CardView)findViewById(R.id.card_view_yc_closed);

        closed_challenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(YoursChallengesCountActivity.this,YoursChallengesActivity.class);
                intent.putExtra("channel_id",channelId);
                startActivity(intent);

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

}
