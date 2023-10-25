package com.example.song_finder_fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main extends Application {

    static List<String> songList = new ArrayList<>();
    static File selectedDirectory = null;
    static Clip clip;

    public static Clip getClip() {
        return clip;
    }

    public static void main(String[] args) {
        new Thread(() -> launch(args)).start();
    }

    public static void addSongToList(String isrc) {
        songList.add(isrc);
        System.out.println("========================");
        for (String isrcs : songList) {
            System.out.println(isrcs);
        }
    }

    public static List<String> getSongList() {
        return songList;
    }

    public static void directoryCheck() {
        if (selectedDirectory != null) {
            System.out.println(selectedDirectory.getAbsolutePath());
        } else {
            System.out.println("No audio database directory specified");
            selectedDirectory = Main.browseLocation();
            System.out.println(selectedDirectory.getAbsolutePath());
        }
    }

    static boolean playAudio(Path start, String isrc) throws IOException {
        try (Stream<Path> stream = Files.walk(start)) {
            Path path = getFileByISRC(isrc, stream);

            if (path != null) {
                // TODO: Play audio, handle audio player UI
                File file = new File(path.toUri());

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
                return true;
            } else {
                // TODO: Handle UI showing audio file is missing
                System.out.println("Cannot load file!");
                return false;
            }

        } catch (SQLException | ClassNotFoundException | LineUnavailableException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported audio");
        }
        return false;
    }

    private static Path getFileByISRC(String isrc, Stream<Path> stream) throws SQLException, ClassNotFoundException {
        String fileName = DatabaseMySQL.searchFileName(isrc);
        return stream
                .filter(path -> path.toFile().isFile())
                .filter(path -> path.getFileName().toString().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Loading layout file
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("layouts/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1030, 610);

        stage.setTitle("CeyMusic Toolkit 2023.1");
        stage.setScene(scene);
        stage.setMinWidth(995);
        stage.setMinHeight(650);

        Image image = new Image("com/example/song_finder_fx/icons/icon.png");

        stage.getIcons().add(image);

        stage.show();

        stage.setOnCloseRequest(e -> Platform.exit());
    }

    public File browseFile() {
        FileChooser fileChooser = new FileChooser();

        return fileChooser.showOpenDialog(null);
    }

    public static File browseLocation() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile();
            System.out.println("Selected audio database directory: " + selectedDirectory.getAbsolutePath());
        }
        return selectedDirectory;
    }

    public String searchAudios(String ISRCs, File directory, File destination) throws SQLException, ClassNotFoundException, AWTException {
        String[] ISRCCodes = ISRCs.split("\\n");
        DatabaseMySQL.SearchSongsFromDB(ISRCCodes, directory, destination);
        NotificationBuilder nb = new NotificationBuilder();
        nb.displayTrayInfo("Execution Completed", "Please check your destination folder for the copied audio files");
        return "Done";
    }

    public static File browseDestination() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        File selectedDirectory = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile();
            System.out.println("Selected destination directory: " + selectedDirectory.getAbsolutePath());
        }
        return selectedDirectory;
    }
}

// TODO: Check current progress with test cases
// TODO: Add a function to open output folder when done
// TODO: Implement a settings page to set database location
// TODO: Add singer's name when searching songs
// TODO: In DatabaseMySQL, make SearchSongsFromDB which is used to copy songs by ISRC method uses MySQL database
// TODO: Adding admin switch
// TODO: Song list view
// TODO: Click to copy data
// TODO: Make about section
// TODO: Add a separate threads for open file location, copy to, and check database
// TODO: Add a place to show the featuring artist in song-view.fxml
// TODO: Implement a column in database to put CeyMusic share
// TODO: Add another VBox to the song-view.fxml to show similar results for the song that user is viewing by song title or something
// TODO: Keyboard movement handling for search
// TODO: Code is malfunctioning when pasted UPCs
// TODO: File copying thread works again and again
