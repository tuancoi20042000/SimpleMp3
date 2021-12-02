package com.example.mp3app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button  btnnext, btnprev, btnff, btnfr;
    ImageView btnplay, imageView;
    TextView txtsName, txtsStart, txtsStop;
    SeekBar seekMusic;
    BarVisualizer visualizer;
    String sName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int postion;
    ArrayList<File> mySongs;
    Thread updateSeekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer!= null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        init();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        getSupportActionBar().setTitle("Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = bundle.getString("songname");
        postion = bundle.getInt("pos",0);
        txtsName.setSelected(true);
        startMedia(postion);

        updateSeekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int curentpositon = 0;
                while(curentpositon<totalDuration){
                    try{
                        sleep(200);
                        curentpositon = mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(curentpositon);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        seekMusic.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekMusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.simpleyellow), PorterDuff.Mode.MULTIPLY);
        seekMusic.getThumb().setColorFilter(getResources().getColor(R.color.simpleyellow),PorterDuff.Mode.SRC_IN);
        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        final Handler handler = new Handler();
        final int delay =1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currenttime = createTime(mediaPlayer.getCurrentPosition());
                txtsStart.setText(currenttime);
                handler.postDelayed(this,delay);
            }
        },delay);
        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                  btnplay.setImageResource(R.drawable.ic_play);
                  btnplay.setBackgroundResource(R.drawable.ic_play);
                  mediaPlayer.pause();
                }else{
                    btnplay.setImageResource(R.drawable.ic_pause);
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                postion = (postion+1) % mySongs.size();
               startMedia(postion);

            }
        });
        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(postion!=0) {
                    postion = (postion - 1) % mySongs.size();
                }
                startMedia(postion);
            }
        });
        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()&& mediaPlayer.getCurrentPosition()>10000){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
            }
        });
    }
    // Start a media
    public void startMedia(int position){
        Uri uri = Uri.parse(mySongs.get(postion).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        sName = mySongs.get(postion).getName();
        txtsName.setText(sName);
        btnplay.setImageResource(R.drawable.ic_pause);
        txtsStop.setText(createTime(mediaPlayer.getDuration()));
        startAnimation(imageView);
        int audioSessionID = mediaPlayer.getAudioSessionId();
        if(audioSessionID!= -1){
            visualizer.setAudioSessionId(audioSessionID);
        }

    }
    // init view
    public void init(){
        btnplay = findViewById(R.id.btnplay);
        btnnext = findViewById(R.id.btnnext);
        btnff=findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        btnprev= findViewById(R.id.btnprev);
        txtsName = findViewById(R.id.txtsn);
        txtsStart = findViewById(R.id.txtstart);
        txtsStop= findViewById(R.id.txtend);
        seekMusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imageview);
    }
    // Start animation
    public void startAnimation(View view){
        // Rotate music note from 0 to 360 degree
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    // format time
    public String createTime(int duration){
        String time ="";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time+=min+":";
        if(sec<10){
           time+="0";
        }
        time+=sec;
        return time;
    }
}