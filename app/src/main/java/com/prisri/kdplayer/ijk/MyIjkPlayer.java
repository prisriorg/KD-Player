package com.prisri.kdplayer.ijk;

import android.content.Context;
import android.widget.Toast;

import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;
import xyz.doikki.videoplayer.ijk.IjkPlayer;

public class MyIjkPlayer extends IjkPlayer {
    public MyIjkPlayer(Context context) {
        super(context);
    }
    public int selectedTrack(int trackType){
        return mMediaPlayer.getSelectedTrack(trackType);
    }
    public void selectTrack(int trackType){
        mMediaPlayer.selectTrack(trackType);
    }
    public IjkTrackInfo[] getTrackInfo(){
        return mMediaPlayer.getTrackInfo();
    }
    public void deselectTrack(int trackType){
        mMediaPlayer.deselectTrack(trackType);
    }

    public void myPl(Context context){
        StringBuilder aa = new StringBuilder("hello");
        IjkTrackInfo[] trackInfo = mMediaPlayer.getTrackInfo();
        int ii = mMediaPlayer.getSelectedTrack(ITrackInfo.MEDIA_TRACK_TYPE_AUDIO);
        if (trackInfo == null){
            aa = new StringBuilder("null hai");
        }
        int sub=0;
        int time=0;
        for (IjkTrackInfo info : trackInfo) {
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
               aa = new StringBuilder("video hai = ");
            }
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                aa.append(info.getLanguage());
            }
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                aa.append(info.getLanguage());
                time++;
            }
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
                sub++;
            }
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_UNKNOWN) {
                aa.append("MEte "+info.getInfoInline()+" ");
            }

        }
        mMediaPlayer.pause();
        Toast.makeText(context, aa.toString() +ii+" sub="+sub+" time="+time, Toast.LENGTH_SHORT).show();
    }
}
