package com.example.song_finder_fx.Controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {
    public static void main(String[] args) {
        System.out.println("Formatted start time: " + formatTime("input1"));
    }

    public static String formatTime(String input) {
        try {
            String[] parts = input.split("\\.");
            if (parts.length == 3) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);

                LocalTime time = LocalTime.of(hours, minutes, seconds);
                return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            } else if (parts.length == 2) {
                int hours = 0; // Default to 0 hours
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);

                LocalTime time = LocalTime.of(hours, minutes, seconds);
                return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
        } catch (DateTimeParseException | ArrayIndexOutOfBoundsException e) {
            return "";
        }
        return "";
    }

    // Extract YouTube ID from URL
    public static String extractYoutubeID(String url) {
        // Regular expression to match YouTube video URLs
        String pattern = "^https?://(?:www\\.|m\\.)?(?:youtube\\.com|youtu\\.be)/(?:watch\\?v=|embed/|v/|watch\\?.*v=|)([^&?/]+).*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1); // Extract the video ID
        } else {
            return null; // No valid video ID found
        }
    }
}
