package com.prisri.kdplayer.ijk;

import android.content.Context;

import xyz.doikki.videoplayer.ijk.IjkPlayer;
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;

public class MyIjkPlayerFactory extends IjkPlayerFactory {
    public static MyIjkPlayerFactory create() {
        return new MyIjkPlayerFactory();
    }
    @Override
    public MyIjkPlayer createPlayer(Context context) {
        return new MyIjkPlayer(context);
    }
}
