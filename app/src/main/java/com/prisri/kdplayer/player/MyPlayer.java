package com.prisri.kdplayer.player;

import static xyz.doikki.videoplayer.util.PlayerUtils.getNavigationBarHeight;
import static xyz.doikki.videoplayer.util.PlayerUtils.getStatusBarHeight;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.prisri.kdplayer.R;
import com.prisri.kdplayer.ijk.MyIjkPlayerFactory;

import java.util.ArrayList;

import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import xyz.doikki.videoplayer.player.BaseVideoView;

public class MyPlayer {
    ArrayList<String>  audioLanguages= new ArrayList<>();
    ArrayList<String>  audioLanguagesSetted= new ArrayList<>();
    ArrayList<String>  subtitleList= new ArrayList<>();
    BaseVideoView player;
    private int audioSelected=0;
    int language,a=0,ab=0;
    private  boolean shop=true;
    private int currentSpeed=3;
    private SeekBar seekBar;
    private DrawerLayout drawerLayout;
    private int STATUS_ERROR=-1;
    private int STATUS_IDLE=0;
    private int STATUS_LOADING=1;
    private int STATUS_PLAYING=2;
    private int STATUS_PAUSE=3;
    private int STATUS_COMPLETED=4;
    private long pauseTime;
    private int initHeight;
    private int defaultTimeout=3000;
    private boolean isLive = false;
    private boolean isShop = true;
    private boolean playerSupport;
    private boolean lock=true;
    private static final int MESSAGE_SHOW_PROGRESS = 1;
    private static final int MESSAGE_FADE_OUT = 2;
    private static final int MESSAGE_SEEK_NEW_POSITION = 3;
    private static final int MESSAGE_HIDE_CENTER_BOX = 4;
    private static final int MESSAGE_RESTART_PLAY = 5;
    private AudioManager audioManager;

    private  int mMaxVolume;

    private final Activity activity;

    private boolean portrait;
    private boolean fullScreenOnly ;
    private Query $;
    private boolean longPress=false;
    private int status=STATUS_IDLE;
    private int screenWidthPixels;






    private OnErrorListener onErrorListener= (what, extra) -> {
    };
    private Runnable oncomplete = () -> {

    };
    private OnInfoListener onInfoListener= (what, extra) -> {

    };
    private OnControlPanelVisibilityChangeListener onControlPanelVisibilityChangeListener= isShowing -> {

    };
    public void setDefaultRetryTime(long defaultRetryTime) {
        this.defaultRetryTime = defaultRetryTime;
    }



    @SuppressLint("ClickableViewAccessibility")
    public MyPlayer(final Activity activity) {
        this.activity=activity;
        $=new Query(activity);
        player = activity.findViewById(R.id.allVideo);
        player.setPlayerFactory(MyIjkPlayerFactory.create());
        initViews();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        screenWidthPixels = activity.getResources().getDisplayMetrics().widthPixels;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        seekBar = (SeekBar) activity.findViewById(R.id.app_video_seekBar);
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(mSeekListener);
        drawerLayout =activity.findViewById(R.id.drawer_layout);
        $.id(R.id.app_video_play).clicked(onClickListener);
        $.id(R.id.app_video_fullscreen).clicked(onClickListener);
        $.id(R.id.app_video_finish).clicked(onClickListener);
        $.id(R.id.replay).clicked(onClickListener);
        $.id(R.id.loack).clicked(onClickListener);
        $.id(R.id.ffwd).clicked(onClickListener);
        $.id(R.id.rew).clicked(onClickListener);
        $.id(R.id.play_pause).clicked(onClickListener);
        $.id(R.id.play_pause1).clicked(onClickListener);
        $.id(R.id.Main_me).clicked(onClickListener);
        $.id(R.id.SetSpeed).clicked(onClickListener);
        $.id(R.id.audio_trk).clicked(onClickListener);
        $.id(R.id.subtitle_trk).clicked(onClickListener);
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        GestureDetector gestureDetector = new GestureDetector(activity, new PlayerGestureListener());
        ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(activity, new MyScaleGestureListener());
        View liveBox = activity.findViewById(R.id.app_video_box);
        liveBox.setClickable(true);

        player.setOnStateChangeListener(new BaseVideoView.OnStateChangeListener() {
            @Override
            public void onPlayerStateChanged(int playerState) {
                statusChange(playerState);
            }

            @Override
            public void onPlayStateChanged(int playState) {
                statusChange(playState);

            }
        });
        liveBox.setOnTouchListener((view, motionEvent) -> {
            mScaleGestureDetector.onTouchEvent(motionEvent);
            gestureDetector.onTouchEvent(motionEvent);
            // 处理手势结束
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_UP:
                    if(longPress){
                        setSpeed(1.0f);
                        $.id(R.id.speed_box).gone();
                    }
                    endGesture();
                    break;
            }
            return true;
        });
        initHeight = activity.findViewById(R.id.app_video_box).getLayoutParams().height;
        show(3000);
//        hideAll();
    }

    public void setVideoSize(CharSequence size){
        $.id(R.id.video_size).text(size);
    }
    public void setVideoName(CharSequence name){
        $.id(R.id.video_name).text(name);
    }
    public void setVideoDate(CharSequence date){
        $.id(R.id.video_date).text(date);
    }
    public void setVideoResolution(CharSequence resolution){
        $.id(R.id.video_hw).text(resolution);
    }

    private long duration;
    private boolean instantSeeking;
    private boolean isDragging;
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;
            $.id(R.id.app_video_status).gone();
            int newPosition = (int) ((duration * progress*1.0) / 1000);
            String time = generateTime(newPosition);
            if (instantSeeking){
                player.seekTo(newPosition);
            }
            $.id(R.id.app_video_currentTime).text(time);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            show(3600000);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            if (instantSeeking){
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!instantSeeking){
                player.seekTo((int) ((duration * seekBar.getProgress()*1.0) / 1000));
            }
            show(defaultTimeout);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isDragging = false;
            handler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        }
    };

    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }
    @SuppressWarnings("HandlerLeak")
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_FADE_OUT:
                    hide(false);
                    break;
                case MESSAGE_HIDE_CENTER_BOX:
                    $.id(R.id.app_video_volume_box).gone();
                    $.id(R.id.app_video_brightness_box).gone();
                    $.id(R.id.app_video_fastForward_box).gone();
                    break;
                case MESSAGE_SEEK_NEW_POSITION:
                    if (!isLive && newPosition >= 0) {
                        player.seekTo((int) newPosition);
                        newPosition = -1;
                    }
                    break;
                case MESSAGE_SHOW_PROGRESS:
                    setProgress();
                    if (!isDragging && isShowing) {
                        msg = obtainMessage(MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000);
                        updatePlayPause();
                    }
                    break;
                case MESSAGE_RESTART_PLAY:
//                    play();
                    break;
            }
        }
    };

    public void onConfigurationChanged(final Configuration newConfig) {
        portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        doOnConfigurationChanged(portrait);
    }

    private void doOnConfigurationChanged(final boolean portrait) {
        if (player != null && !fullScreenOnly) {
            handler.post(() -> {
                tryFullScreen(!portrait);
                if (portrait) {
                    $.id(R.id.app_video_box).height(initHeight, false);
                } else {
                    int heightPixels = activity.getResources().getDisplayMetrics().heightPixels;
                    int widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
                    $.id(R.id.app_video_box).height(Math.min(heightPixels,widthPixels), false);
                }
                updateFullScreenButton();
            });
//            orientationEventListener.enable();
        }
    }
    private void statusChange(int newStatus) {
        status=newStatus;
        if (!isLive && newStatus == STATUS_COMPLETED) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            hideAll();
            $.id(R.id.replay).visible();
        }else if (newStatus == STATUS_ERROR) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            hideAll();
            Toast.makeText(activity, "Something wrong", Toast.LENGTH_SHORT).show();
            if (isLive) {
               // showStatus("Small Problem");
                if (defaultRetryTime>0) {
                    handler.sendEmptyMessageDelayed(MESSAGE_RESTART_PLAY, defaultRetryTime);
                }
            } else {
            //    showStatus("Small Problem");
            }
        } else if(newStatus == STATUS_LOADING){
            hideAll();
            $.id(R.id.app_video_loading).visible();
        } else if (newStatus == STATUS_PLAYING) {
            hideAll();
        }else if (newStatus== BaseVideoView.STATE_BUFFERING){
            hideAll();
            $.id(R.id.app_video_loading).visible();
        }else if(newStatus == BaseVideoView.STATE_BUFFERED){
            hideAll();
        }else if (newStatus== BaseVideoView.STATE_PREPARING){
            hideAll();
            $.id(R.id.app_video_loading).visible();
        }else if(newStatus == BaseVideoView.STATE_PREPARED){
            hideAll();
        }

    }
    private void hideAll() {
        $.id(R.id.app_video_loading).invisible();
        $.id(R.id.app_video_fullscreen).invisible();
        $.id(R.id.app_video_status).gone();
        showBottomControl(false);
        onControlPanelVisibilityChangeListener.change(false);
    }
    private void endGesture() {
        if(lock) {
            volume = -1;
            brightness = -1f;
            if (newPosition >= 0) {
                handler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
                handler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
            }
            handler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
            handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);
        }

    }
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.app_video_fullscreen) {
                toggleFullScreen();
            } else if (v.getId() == R.id.app_video_play) {
                doPauseResume();
            } else if (v.getId() == R.id.replay) {
                player.replay(true);
            } else if (v.getId() == R.id.app_video_finish) {
                if (!fullScreenOnly && !portrait) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                        activity.finish();
                    }
                }
            } else if (v.getId() == R.id.ffwd){
                long post=getCurrentPosition();
                player.seekTo(post+10000);
            } else if (v.getId() == R.id.rew){
                long post2=getCurrentPosition();
                player.seekTo(post2-10000);
            } else if (v.getId() == R.id.play_pause){
                doPauseResume();
            } else if (v.getId() == R.id.play_pause1){
                doPauseResume();
            }else if(v.getId() == R.id.loack){
                if(lock){
                    lock=false;
                    $.id(R.id.loack).image(R.drawable.lock);
                    $.id(R.id.linearLayout4).invisible();
                    $.id(R.id.midBox2).invisible();
                    $.id(R.id.midBox3).invisible();
                    $.id(R.id.linearLayout3).invisible();
                }else {
                    $.id(R.id.loack).image(R.drawable.unlock);
                    show(3000);
                    lock=true;
                }
            }else if (v.getId() == R.id.Main_me){
                MainBox();
            }else if (v.getId() == R.id.SetSpeed){
                SpeedChng(currentSpeed);
            }else if (v.getId() == R.id.audio_trk){
                audioTrack();
            }else if (v.getId() == R.id.subtitle_trk){
                subtitleTrack();
            }
        }
    };
    private void subtitleTrack() {
        final ITrackInfo[] trackInfo = player.getTrackInfo();
        final ArrayList<String> day_radio_mid = new ArrayList<String>();
        language = player.selectedTrack(2);
        if (trackInfo != null) {
            for (int i = 0; i < trackInfo.length; i++) {
                if (trackInfo[i].getTrackType() == 3) {
                    day_radio_mid.add(trackInfo[i].getLanguage());
                }
            }
        }

        final CharSequence[] day_radio = new CharSequence[day_radio_mid.size()];
        if (trackInfo != null) {
            for (int i = 0; i < day_radio_mid.size(); i++) {
                day_radio[i] = day_radio_mid.get(i);
            }
        }
        AlertDialog.Builder alrt= new AlertDialog.Builder(activity);
        alrt.setTitle("Select Subtitle");
        alrt.setSingleChoiceItems(day_radio,ab, (dialog, which) -> {
            for (int i = 0; i < trackInfo.length; i++) {
                if (trackInfo[i].getLanguage() == day_radio[which]) {
                    if (language != i) {
                        language = i;
                        ab=which;
                        player.selectTrack(i);
//                        long pos=player.getCurrentPosition();
//                        player.seekTo(pos);
//                        player.start();
                        dialog.cancel();
                    }

                }
            }
        });
        alrt.create().show();
    }

    private void audioTrack(){
        final ITrackInfo[] trackInfo = player.getTrackInfo();
        final ArrayList<String> day_radio_mid = new ArrayList<String>();
        language = player.selectedTrack(2);
        if (trackInfo != null) {
            for (int i = 0; i < trackInfo.length; i++) {
                if (trackInfo[i].getTrackType() == 2) {
                    day_radio_mid.add(trackInfo[i].getLanguage());
                }
            }
        }
        final CharSequence[] day_radio = new CharSequence[day_radio_mid.size()];
        if (trackInfo != null) {
            for (int i = 0; i < day_radio_mid.size(); i++) {
                day_radio[i] = day_radio_mid.get(i);
            }
        }

        AlertDialog.Builder alrt= new AlertDialog.Builder(activity);
        alrt.setTitle("Select Audio");
        alrt.setSingleChoiceItems(day_radio,language-1, (dialog, which) -> {
            for (int i = 0; i < trackInfo.length; i++) {
                if (trackInfo[i].getLanguage() == day_radio[which]) {
                    if (language != i) {
                        player.selectTrack(i);
                        long pos=player.getCurrentPosition();
                        player.seekTo(pos);
                        player.start();
                        dialog.cancel();
                    }

                }
            }
        });
        alrt.create().show();
    }

//    private void subtitleTrack() {
//        ArrayList yy=getSubTracks();
//        String arr[]=new String[yy.size()];
//        for (int y=0;y<yy.size();y++){
//            arr[y]= yy.get(y).toString();
//        }
//        int tt=0;
//        AlertDialog.Builder alrt= new AlertDialog.Builder(activity);
//        alrt.setTitle("Select Audio");
//        alrt.setSingleChoiceItems(arr,tt, (dialog, which) -> {
//            for (int i=0;i< arr.length;i++){
//                if (i==which){
//                    setSubTrack(arr[i]);
//                    dialog.cancel();
//                }
//            }
//        });
//        alrt.create().show();
//        subtitleList.clear();
//    }

//    private void setSubtitleTrack(String s) {
//
//    }

//    private ArrayList getSubTracks(){
//        int track=0;
////        for(int i = 0; i < player.getCurrentTrackGroups().length; i++){
////            String format = player.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
////            String lang = player.getCurrentTrackGroups().get(i).getFormat(0).language;
////            String id = player.getCurrentTrackGroups().get(i).getFormat(0).id;
////            System.out.println(player.getCurrentTrackGroups().get(i).getFormat(0));
////            if(format.contains("sub") && id != null && lang != null){
////                subtitleList.add("track "+track+" [ "+lang+" ]");
////                track++;
////            }
////
////        }
//        return subtitleList;
//    }
//    private void audioTrack() {
//      ArrayList yy=getAllTracks();
//        String arr[]=new String[yy.size()];
//        for (int y=0;y<yy.size();y++){
//            arr[y]= yy.get(y).toString();
//        }
//        AlertDialog.Builder alrt= new AlertDialog.Builder(activity);
//        alrt.setTitle("Select Audio");
//        alrt.setSingleChoiceItems(arr,audioSelected, (dialog, which) -> {
//            for (int i=0;i< arr.length;i++){
//                if (i==which){
//                    setAudioTrack(arr[i]);
//                    audioSelected=i;
//                    dialog.cancel();
//                }
//            }
//        });
//        alrt.create().show();
//        audioLanguages.clear();
//        audioLanguagesSetted.clear();
//    }
//    private ArrayList getAllTracks(){
//
//
//
//        ITrackInfo[] trackInfo = player.getTrackInfo();
//        if (trackInfo != null) {
//            for (int i = 0; i < trackInfo.length; i++) {
//                if (trackInfo[i].getTrackType() == 2) {
//                    audioLanguages.add(trackInfo[i].getLanguage());
//                }
//            }
//        }
////        for(int i = 0; i < player.getCurrentTrackGroups().length; i++){
////            String format = player.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
////            String lang = player.getCurrentTrackGroups().get(i).getFormat(0).language;
////            String id = player.getCurrentTrackGroups().get(i).getFormat(0).id;
////            System.out.println(player.getCurrentTrackGroups().get(i).getFormat(0));
////            if(format.contains("audio") && id != null && lang != null){
////                audioLanguages.add(lang);
////            }
////        }
//        return audioLanguages;
//    }
//    public void setAudioTrack(String lang) {
////        player.setTrackSelectionParameters( player.getTrackSelectionParameters()
////                .buildUpon()
////                .setMaxVideoSizeSd()
////                .setPreferredAudioLanguage(lang)
////                .build());
//    }

//    public void setSubTrack(String lang) {
////        player.setTrackSelectionParameters( player.getTrackSelectionParameters()
////                .buildUpon()
////                .setMaxVideoSizeSd()
////                .setPreferredTextLanguage(lang)
////                .build());
//    }
    private void toggleFullScreen() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void doPauseResume() {
        if(player.isPlaying()){
            player.pause();
            updatePlayPause();
        }else {
            player.start();
            updatePlayPause();
        }
    }
    private void MainBox() {

        drawerLayout.openDrawer(Gravity.RIGHT);
    }
    public void play(String url) {

        player.setUrl(url);
        player.start();
        $.id(R.id.pause).visible();
        $.id(R.id.play).gone();
        $.id(R.id.pause1).visible();
        $.id(R.id.play1).gone();

    }
    public void setTitle(CharSequence title) {
        $.id(R.id.Name).text(title);
    }

    public void onPause() {
      player.pause();
      $.id(R.id.play).visible();
      $.id(R.id.pause).gone();
      $.id(R.id.play1).visible();
      $.id(R.id.pause1).gone();
    }

    private void updatePlayPause() {
        if (player.isPlaying()){
            $.id(R.id.pause).visible();
            $.id(R.id.play).gone();
            $.id(R.id.pause1).visible();
            $.id(R.id.play1).gone();
        }else {
            $.id(R.id.play).visible();
            $.id(R.id.pause).gone();
            $.id(R.id.play1).visible();
            $.id(R.id.pause1).gone();
        }
    }

    public void onResume() {
        player.resume();
        $.id(R.id.pause).visible();
        $.id(R.id.play).gone();
        $.id(R.id.pause1).visible();
        $.id(R.id.play1).gone();
    }
    public void onBackPressed(){

    }
    public long getCurrentPosition(){
        return player.getCurrentPosition();
    }
    public long getDuration(){
        return player.getDuration();
    }

    private void getRes(){

    }

    private boolean isShowing;

    private float brightness=-1;
    private int volume=-1;
    private long newPosition = -1;
    private long defaultRetryTime=5000;

    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        hide(true);

        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        int percent2 = (int) (index * 1.0 / mMaxVolume * 200);
        // 变更进度条
        int i =  (int) (index * 1.0 / mMaxVolume * 100/7);
        String s = String.valueOf(i);
        if (i == 0) {
            s = "0";
        }
        // 显示
        $.id(R.id.app_video_volume_icon).image(i==0?R.drawable.sound_off:R.drawable.sound);
        $.id(R.id.app_video_brightness_box).gone();
        $.id(R.id.app_video_fastForward_box).gone();
        $.id(R.id.app_video_volume_box).visible();
        SeekBar ss2=activity.findViewById(R.id.app_video_volume_seekbar);
        ss2.setProgress((int)(percent2));
        $.id(R.id.app_video_volume).text(s).visible();
    }

    private void onProgressSlide(float percent) {
        long position = getCurrentPosition();
        long duration = getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);

        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition=0;
            delta=-position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            $.id(R.id.app_video_brightness_box).gone();
            $.id(R.id.app_video_volume_box).gone();
            $.id(R.id.app_video_volume).gone();
            $.id(R.id.app_video_fastForward_box).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            $.id(R.id.app_video_fastForward).text(text + "s");
            $.id(R.id.app_video_fastForward_target).text(generateTime(newPosition)+"/");
            $.id(R.id.app_video_fastForward_all).text(generateTime(duration));
        }
    }

    public void show(int timeout) {
        showBottomControl(true);
        if(lock){
            if (!isShowing) {
                if (!isLive) {
                    showBottomControl(true);
                }
                if (!fullScreenOnly) {
                    $.id(R.id.app_video_fullscreen).visible();
                }
                isShowing = true;
                onControlPanelVisibilityChangeListener.change(true);
            }
            updatePlayPause();
            handler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
            handler.removeMessages(MESSAGE_FADE_OUT);
            if (timeout != 0) {
                handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_FADE_OUT), timeout);
            }
            $.id(R.id.midBox2).visible();
            $.id(R.id.midBox3).visible();
        }else {
            $.id(R.id.midBox2).gone();
            $.id(R.id.midBox3).gone();
            $.id(R.id.midBox).visible();
            $.id(R.id.linearLayout4).gone();
            $.id(R.id.linearLayout3).gone();
        }
    }
    private void showBottomControl(boolean show) {
        $.id(R.id.app_video_play).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_currentTime).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_endTime).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_seekBar).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.midBox).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.linearLayout4).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.linearLayout3).visibility(show ? View.VISIBLE : View.GONE);
    }
    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = activity.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f){
                brightness = 0.50f;
            }else if (brightness < 0.01f){
                brightness = 0.01f;
            }
        }
        Log.d(this.getClass().getSimpleName(),"brightness:"+brightness+",percent:"+ percent);
        $.id(R.id.app_video_brightness_box).visible();
        $.id(R.id.app_video_volume_box).gone();
        $.id(R.id.app_video_volume).gone();
        $.id(R.id.app_video_fastForward_box).gone();

        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f){
            lpa.screenBrightness = 1.0f;
        }else if (lpa.screenBrightness < 0.01f){
            lpa.screenBrightness = 0.01f;
        }

        SeekBar ss =activity.findViewById(R.id.app_video_brightness_seekbar);
        ss.setProgress((int) (lpa.screenBrightness * 100));
        $.id(R.id.app_video_brightness).text(String.valueOf((int) (lpa.screenBrightness * 100) / 7 ));
        activity.getWindow().setAttributes(lpa);

    }

    private long setProgress() {
        if (isDragging){
            return 0;
        }
        long position = getCurrentPosition();
        long duration = getDuration();
        if (seekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
//            int percent = videoView.getBufferPercentage();
//            seekBar.setSecondaryProgress(percent * 10);
        }

        this.duration = duration;
        $.id(R.id.app_video_currentTime).text(generateTime(position));
        $.id(R.id.app_video_endTime).text(generateTime(this.duration));
        return position;
    }

    public void hide(boolean force) {
        if (force || isShowing) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            showBottomControl(false);
//            $.id(R.id.app_video_top_box).gone();
//            $.id(R.id.app_video_fullscreen).invisible();
            $.id(R.id.midBox).invisible();

//            $.id(R.id.istream).gone();
            isShowing = false;
            onControlPanelVisibilityChangeListener.change(false);
        }
    }

    private void updateFullScreenButton() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//            $.id(R.id.app_video_fullscreen).image(R.drawable.exit_fullscreen);
//            activity.findViewById(R.id.exo_controller).setPadding(10,0,10,0);
        } else {
//            $.id(R.id.app_video_fullscreen).image(R.drawable.full_screen);
//            activity.findViewById(R.id.exo_controller).setPadding(70,0,10,0);
        }
    }
    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(lock) {
                float dtX = activity.findViewById(R.id.play_pause).getX();
                float deltaX = e.getX();
                long pos = getCurrentPosition();
                if (deltaX <= dtX) {
                    player.seekTo(pos - 10000);
                    player.resume();
                } else if (dtX <= deltaX && deltaX < dtX * 2) {
                    if (player == null)
                        return false;
                    if (player.isPlaying())
                        player.pause();
                    else
                        player.resume();
                } else if (deltaX >= dtX * 2) {
                    player.seekTo(pos + 10000);
                    player.resume();
                }
                updatePlayPause();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {

            super.onLongPress(e);
            if(lock) {
                longPress = true;
                setSpeed(2.0f);
                $.id(R.id.speed_box).visible();
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(lock) {
                if (isShop) {
                    float mOldX = e1.getX(), mOldY = e1.getY();
                    float deltaY = mOldY - e2.getY();
                    float deltaX = mOldX - e2.getX();
                    if (firstTouch) {
                        toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                        volumeControl = mOldX > screenWidthPixels * 0.5f;
                        firstTouch = false;
                    }

                    if (toSeek) {
                        if (true) {
                            onProgressSlide(-deltaX / activity.findViewById(R.id.app_video_box).getWidth());
                        }
                    } else {
                        float percent = deltaY / activity.findViewById(R.id.app_video_box).getHeight();
                        if (volumeControl) {
                            onVolumeSlide(percent);
                        } else {
                            onBrightnessSlide(percent);
                        }
                    }
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isShowing) {
                hide(false);
            } else {
                show(defaultTimeout);
            }
            return true;
        }
    }
    private  float scale_factor =1.0f;
    private class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float mW,mH;
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if(lock) {
                isShop = false;
                $.id(R.id.app_video_brightness_box).gone();
                $.id(R.id.app_video_fastForward_box).gone();
                $.id(R.id.app_video_volume_box).gone();
                // scale our video view
                scale_factor *= detector.getScaleFactor();
                scale_factor = Math.max(0.25f, Math.min(scale_factor, 6.0f));
                player.setScaleX(scale_factor);
                player.setScaleY(scale_factor);
                $.id(R.id.app_video_zoom).text(" " + ((int) (scale_factor * 100)) + "%");
                $.id(R.id.app_video_zoom_box).visible();
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            mW = player.getWidth();
            mH = player.getHeight();
//            isShop=false;
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            $.id(R.id.app_video_zoom_box).gone();
            detector.getScaleFactor();
            isShop=true;
            super.onScaleEnd(detector);
        }
    }
    private void SpeedChng(int speed){
        String itm[]={"0.25x","0.50x","0.75x","1.0x","1.25x","1.50x","1.75x","2.0x"};
        float r[]={0.25f,0.5f,0.75f,1.0f,1.25f,1.5f,1.75f,2.0f};
        AlertDialog.Builder alrt= new AlertDialog.Builder(activity);
        alrt.setTitle("Select Speed");//.setPositiveButton("OK",null);
        alrt.setSingleChoiceItems(itm, speed, (dialog, which) -> {
            switch (which){
                case 0:
                    setSpeed(r[0]);
                    currentSpeed=0;
                    dialog.cancel();
                    break;
                case 1:
                    setSpeed(r[1]);
                    currentSpeed=1;
                    dialog.cancel();
                    break;
                case 2:
                    setSpeed(r[2]);
                    currentSpeed=2;
                    dialog.cancel();
                    break;
                case 3:
                    setSpeed(r[3]);
                    currentSpeed=3;
                    dialog.cancel();
                    break;
                case 4:
                    setSpeed(r[4]);
                    currentSpeed=4;
                    dialog.cancel();
                    break;
                case 5:
                    setSpeed(5);
                    currentSpeed=5;
                    dialog.cancel();
                    break;
                case 6:
                    setSpeed(r[6]);
                    currentSpeed=6;
                    dialog.cancel();
                    break;
                case 7:
                    setSpeed(r[7]);
                    currentSpeed=7;
                    dialog.cancel();
                    break;

            }
        });
        alrt.create();
        alrt.show();
    }

    public void seekPos(long newPosition){
        player.seekTo(newPosition);
        player.start();
    }
    private int getScreenOrientation() {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90  || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }

        }

        return orientation;
    }

    private void tryFullScreen(boolean fullScreen) {
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        }
        setFullScreen(fullScreen);
    }
    private void setFullScreen(boolean fullScreen) {
        if (activity != null) {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }

    }
    private float getSpeed() {
        return 0.0f;
    }
    private void setSpeed(float v) {
        player.setSpeed(v);
    }
    private void Tost(String toast){
        Toast.makeText(activity, toast, Toast.LENGTH_SHORT).show();
    }
    class Query {
        private final Activity activity;
        private View view;

        public Query(Activity activity) {
            this.activity=activity;
        }

        public Query id(int id) {
            view = activity.findViewById(id);
            return this;
        }

        public Query image(int resId) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            }
            return this;
        }

        public Query visible() {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            return this;
        }

        public Query gone() {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        public Query invisible() {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
            return this;
        }

        public Query clicked(View.OnClickListener handler) {
            if (view != null) {
                view.setOnClickListener(handler);
            }
            return this;
        }

        public Query text(CharSequence text) {
            if (view!=null && view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public Query visibility(int visible) {
            if (view != null) {
                view.setVisibility(visible);
            }
            return this;
        }

        private void size(boolean width, int n, boolean dip){

            if(view != null){
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if(n > 0 && dip){
                    n = dip2pixel(activity, n);
                }
                if(width){
                    lp.width = n;
                }else{
                    lp.height = n;
                }
                view.setLayoutParams(lp);
            }
        }
        public void height(int height, boolean dip) {
            size(false,height,dip);
        }
        public int dip2pixel(Context context, float n){
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
            return value;
        }

        public float pixel2dip(Context context, float n){
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = n / (metrics.densityDpi / 160f);
            return dp;
        }
    }



    public interface OnErrorListener{
        void onError(int what, int extra);
    }

    public interface OnControlPanelVisibilityChangeListener{
        void change(boolean isShowing);
    }

    public interface OnInfoListener{
        void onInfo(int what, int extra);
    }

    public MyPlayer onError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public MyPlayer onComplete(Runnable complete) {
        this.oncomplete = complete;
        return this;
    }

    public MyPlayer onInfo(OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
        return this;
    }

    public MyPlayer onControlPanelVisibilityChang(OnControlPanelVisibilityChangeListener listener){
        this.onControlPanelVisibilityChangeListener = listener;
        return this;
    }

    public MyPlayer live(boolean isLive) {
        this.isLive = isLive;
        return this;
    }

    public MyPlayer toggleAspectRatio(){
        if (player != null) {
//            player.toggleAspectRatio();
        }
        return this;
    }

    public MyPlayer onControlPanelVisibilityChange(OnControlPanelVisibilityChangeListener listener){
        this.onControlPanelVisibilityChangeListener = listener;
        return this;
    }
}
