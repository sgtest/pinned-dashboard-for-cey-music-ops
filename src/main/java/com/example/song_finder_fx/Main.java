package com.example.song_finder_fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {

    public static void main(String[] args) {
        new Thread(() -> launch(args)).start();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Loading layout file
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public File browseFile() {
        FileChooser fileChooser = new FileChooser();

        return fileChooser.showOpenDialog(null);
    }

    public File browseLocation() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        File selectedDirectory = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile();
            System.out.println("Selected audio database directory: " + selectedDirectory.getAbsolutePath());
        }
        return selectedDirectory;
    }

/*    public void executeFunction(ActionEvent event) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Execute your function here
                for (int i = 0; i < 100; i++) {
                    // Simulate work
                    Thread.sleep(50);

                    // Update the progress property of the Task
                    final int progressValue = i + 1;
                    updateProgress(progressValue, 100);

                    // Debug console output
                    System.out.println("Here! Progress: " + progressValue);
                }
                return null;
            }
        };

        // Bind the ProgressBar's progressProperty to the Task's progressProperty
        progressBar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }*/

    public String searchAudios(String ISRCs, File directory, File destination) throws SQLException, ClassNotFoundException, IOException {
        String[] ISRCCodes = ISRCs.split("\\n");
        Database.SearchSongsFromDB(ISRCCodes, directory, destination);
        return "Done";
    }
    public File browseDestination() {
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

// TODO: Show progress
// TODO: Implement tabs
// TODO: Implement get song data by ISRCs
// TODO: Remember last entered database location, and destination location
// TODO: Show a report of copied files, what are not found,
//  offer a button to export the file names/ send an email to an admin to collect those audio files
// TODO: Check current progress with test cases
