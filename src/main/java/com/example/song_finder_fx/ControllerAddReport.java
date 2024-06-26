package com.example.song_finder_fx;

import com.example.song_finder_fx.Controller.AlertBuilder;
import com.example.song_finder_fx.Controller.ItemSwitcher;
import com.example.song_finder_fx.Controller.SceneController;
import com.example.song_finder_fx.Model.ReportMetadata;
import com.example.song_finder_fx.Model.Songs;
import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ControllerAddReport {

    @FXML
    private ComboBox<String> comboMonth;

    @FXML
    private Label lblImport;

    @FXML
    private Label lblReportProgress;

    @FXML
    private Label lblMissingISRCs;

    @FXML
    private Label lblMissingPayees;

    @FXML
    private TextField txtYear;

    @FXML
    private HBox hboxMissingAssets;

    @FXML
    void initialize() {
        // Create a list of month names
        String[] months = {
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        };

        // Add the months to the ComboBox
        comboMonth.getItems().addAll(months);
    }

    @FXML
    void onLoadReport(ActionEvent event) {
        boolean ifAnyNull = checkData();

        if (!ifAnyNull) {
            int month = comboMonth.getSelectionModel().getSelectedIndex();
            int year = Integer.parseInt(txtYear.getText());
            final String[] reportName = new String[1];

            // Getting report name
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Report Name");
            dialog.setHeaderText("Enter a name for report");
            dialog.setContentText("Name: ");
            dialog.showAndWait().ifPresent(name -> reportName[0] = name);
            // Setting report name if not exists
            if (reportName[0].isEmpty()) {
                reportName[0] = ItemSwitcher.setMonth(month + 1).toLowerCase() + "_" + year;
            }

            // Getting report location
            Scene scene = SceneController.getSceneFromEvent(event);
            File file = Main.browseForCSV(SceneController.getWindowFromScene(scene));

            if (file != null) {
                System.out.println("Report Month Chooser Index: " + month);
                System.out.println("Report Name: " + reportName[0]);

                ReportMetadata report = new ReportMetadata(reportName[0], month + 1, year, file);

                Thread thread = getThread(report);
                thread.start();
            }

        }
    }

    private @NotNull Thread getThread(ReportMetadata report) {
        Task<Void> task;
        final int[] key = new int[1];

        task = new Task<>() {
            @Override
            protected Void call() {
                key[0] = DatabasePostgres.importReport(report, lblImport, lblReportProgress);

                Platform.runLater(() -> {
                    hboxMissingAssets.setDisable(false);
                    lblMissingISRCs.setText("Loading");
                    lblMissingPayees.setText("Loading");
                });

                try {
                    int missingPayeeCount = DatabasePostgres.getMissingPayeeCount(key[0]);
                    Platform.runLater(() -> lblMissingPayees.setText(String.valueOf(missingPayeeCount)));
                } catch (SQLException e) {
                    Platform.runLater(() -> {
                        AlertBuilder.sendErrorAlert("Error", "Unable to fetch details", e.toString());
                        e.printStackTrace();
                    });
                }

                try {
                    int missingISRC_Count = DatabasePostgres.getMissingISRC_Count(key[0]);
                    Platform.runLater(() -> lblMissingISRCs.setText(String.valueOf(missingISRC_Count)));
                } catch (SQLException e) {
                    Platform.runLater(() -> {
                        AlertBuilder.sendErrorAlert("Error", "Unable to fetch details", e.toString());
                        e.printStackTrace();
                    });
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        return thread;
    }

    private boolean checkData() {
        boolean status = false;

        String month = comboMonth.getSelectionModel().getSelectedItem();
        String year = txtYear.getText();

        /*System.out.println("month = " + month);
        System.out.println("year = " + year);*/

        if (month == null) {
            status = true;
            comboMonth.setStyle("-fx-border-color: red;");
        } else {
            comboMonth.setStyle("-fx-border-color: '#e9ebee';");
        }

        if (year.isEmpty()) {
            status = true;
            txtYear.setStyle("-fx-border-color: red;");
        } else {
            // TODO: Validate Further
            if (year.matches("\\d{4}")) {
                // Valid 4-digit number
                txtYear.setStyle("-fx-border-color: '#e9ebee';");
                status = false;
            } else {
                // Invalid input (not a 4-digit number)
                txtYear.setStyle("-fx-border-color: red;");
                status = true;
            }
        }

        return status;
    }

    @FXML
    void onExportISRCsClick(MouseEvent event) {
        boolean ifAnyNull = checkData();

        if (!ifAnyNull) {
            int month = comboMonth.getSelectionModel().getSelectedIndex();
            int year = Integer.parseInt(txtYear.getText());
            // TODO: Modify Export Missing Metadata Method
        }

        try {
            // Getting User Location
            Node node = (Node) event.getSource();
            Scene scene = node.getScene();
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
            chooser.setTitle("Save As");
            File file = chooser.showSaveDialog(scene.getWindow());

            if (file != null) {
                File openFile = writeMissingISRCs(file);

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(openFile);
                }
            }

        } catch (SQLException e) {
            AlertBuilder.sendErrorAlert("Error", "Error Getting Songs", e.toString());
        } catch (IOException e) {
            AlertBuilder.sendErrorAlert("Error", "Error Generating CSV", e.toString());
        }
    }

    private static @NotNull File writeMissingISRCs(File file) throws SQLException, IOException {
        ArrayList<Songs> songs =  DatabasePostgres.getMissingISRCs();
        String path = file.getAbsolutePath();
        CSVWriter writer = new CSVWriter(new FileWriter(path));
        java.util.List<String[]> rows = new ArrayList<>();
        String[] header = new String[]{"UPC", "ISRC"};
        rows.add(header);

        for (Songs song : songs) {
            String[] row = {song.getUPC(), song.getISRC()};
            rows.add(row);
        }

        writer.writeAll(rows);
        writer.close();

        File openFile = new File(path);
        return openFile;
    }

    @FXML
    void onExportPayeesClick(MouseEvent event) {
        boolean ifAnyNull = checkData();

        if (!ifAnyNull) {
            int month = comboMonth.getSelectionModel().getSelectedIndex();
            int year = Integer.parseInt(txtYear.getText());
            // TODO: Modify Export Missing Metadata Method
        }

        try {
            // Getting User Location
            Node node = (Node) event.getSource();
            Scene scene = node.getScene();
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
            chooser.setTitle("Save As");
            File file = chooser.showSaveDialog(scene.getWindow());

            if (file != null) {
                File openFile = writeMissingPayees(file);

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(openFile);
                }
            }

        } catch (SQLException e) {
            AlertBuilder.sendErrorAlert("Error", "Error Getting Songs", e.toString());
        } catch (IOException e) {
            AlertBuilder.sendErrorAlert("Error", "Error Generating CSV", e.toString());
        }
    }

    private File writeMissingPayees(File file) throws SQLException, IOException {
        ArrayList<Songs> songs =  DatabasePostgres.getMissingPayees();
        String path = file.getAbsolutePath();
        CSVWriter writer = new CSVWriter(new FileWriter(path));
        List<String[]> rows = new ArrayList<>();
        String[] header = new String[]{"UPC", "ISRC"};
        rows.add(header);

        for (Songs song : songs) {
            String[] row = {song.getUPC(), song.getISRC()};
            rows.add(row);
        }

        writer.writeAll(rows);
        writer.close();

        File openFile = new File(path);
        return openFile;
    }
}
