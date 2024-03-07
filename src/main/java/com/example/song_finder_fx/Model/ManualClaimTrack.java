package com.example.song_finder_fx.Model;

public class ManualClaimTrack {
    private final int id;
    private final String trackName;
    private final String lyricist;
    private final String composer;
    private final String youtubeID;
    private boolean status = false;

    /*public ManualClaimTrack(String trackName, String lyricist, String composer, String url) {
        this.trackName = trackName;
        this.lyricist = lyricist;
        this.composer = composer;
        this.youtubeID = url.substring(32);
    }*/

    public ManualClaimTrack(int id, String trackName, String lyricist, String composer, String url) {
        this.id = id;
        this.trackName = trackName;
        this.lyricist = lyricist;
        this.composer = composer;
        this.youtubeID = url.substring(32);
    }

    public String getTrackName() {
        return trackName;
    }

    public String getLyricist() {
        return lyricist;
    }

    public String getComposer() {
        return composer;
    }

    public String getYoutubeID() {
        return youtubeID;
    }

    public int getId() {
        return id;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
