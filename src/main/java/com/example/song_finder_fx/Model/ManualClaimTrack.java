package com.example.song_finder_fx.Model;

import com.example.song_finder_fx.DatabasePostgres;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.time.LocalDate;

public class ManualClaimTrack {
    private final int id;
    private final String trackName;
    private final String lyricist;
    private final String composer;
    private final String youtubeID;
    private final LocalDate date;
    private boolean status = false;
    private String trimStart;
    private String trimEnd;
    private BufferedImage image;
    private BufferedImage previewImage;
    private final int claimType;

    public ManualClaimTrack(int id, String trackName, String lyricist, String composer, String youTubeID, LocalDate date, int claimType) {
        this.id = id;
        this.trackName = trackName;
        this.lyricist = lyricist;
        this.composer = composer;
        this.youtubeID = youTubeID;
        this.date = date;
        this.claimType = claimType;
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

    public String getYouTubeURL() {
        return "https://www.youtube.com/watch?v=" + youtubeID;
    }

    public void addTrimTime(String trimStart, String trimEnd) {
        this.trimStart = trimStart;
        this.trimEnd = trimEnd;
    }

    public String getTrimStart() {
        return trimStart;
    }

    public String getTrimEnd() {
        return trimEnd;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public Image getImage() {
        return SwingFXUtils.toFXImage(image, null);
    }

    public void setPreviewImage(BufferedImage image) {
        this.previewImage = image;
    }

    public Image getPreviewImage() {
        return SwingFXUtils.toFXImage(previewImage, null);
    }

    public BufferedImage getBufferedImage() {
        return image;
    }

    public BufferedImage getBufferedPreviewImage() {
        return previewImage;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getClaimType() {
        return claimType;
    }

    public String getClaimTypeString() {
        return switch (claimType) {
            case 2 -> "TV Programs";
            case 3 -> "Manual Claim";
            case 4 -> "Single SR";
            default -> "Unspecified";
        };
    }

    public int unArchive() throws SQLException {
        int status = DatabasePostgres.unArchiveManualClaim(id);
        return status;
    }
}
