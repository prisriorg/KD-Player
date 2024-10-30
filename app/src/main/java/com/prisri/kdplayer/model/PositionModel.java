package com.prisri.kdplayer.model;

public class PositionModel {
    private int videoPositipn;
    private String videoPath;

    public PositionModel(int videoPositipn, String videoPath) {
        this.videoPositipn = videoPositipn;
        this.videoPath = videoPath;
    }

    public int getVideoPositipn() {
        return videoPositipn;
    }

    public void setVideoPositipn(int videoPositipn) {
        this.videoPositipn = videoPositipn;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
