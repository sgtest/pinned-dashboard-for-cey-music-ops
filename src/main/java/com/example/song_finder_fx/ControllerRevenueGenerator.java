package com.example.song_finder_fx;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ControllerRevenueGenerator {
    //<editor-fold desc="Buttons">
    public Button btnLoadReport;
    public Button btnGenerateFullBreakdown;
    //</editor-fold>

    //<editor-fold desc="Labels">
    public Label lblUpdatePayee;
    public Label lblUpdateSongsDatabase;
    public Label lblAsset01;
    public Label lblAsset02;
    public Label lblAsset03;
    public Label lblAsset04;
    public Label lblAsset05;
    public Label lblAsset01Streams;
    public Label lblAsset02Streams;
    public Label lblAsset03Streams;
    public Label lblAsset04Streams;
    public Label lblAsset05Streams;
    public Label lblTotalAssets;
    public Label lblCountry01;
    public Label lblCountry02;
    public Label lblCountry03;
    public Label lblCountry04;
    public Label lblCountry05;
    public Label lblCountry01Streams;
    public Label lblCountry02Streams;
    public Label lblCountry03Streams;
    public Label lblCountry04Streams;
    public Label lblCountry05Streams;
    public Label lblDSP01;
    public Label lblDSP02;
    public Label lblDSP03;
    public Label lblDSP04;
    public Label lblAmountDSP01;
    public Label lblAmountDSP02;
    public Label lblAmountDSP03;
    public Label lblAmountDSP04;
    public Label lblTitleMonth;
    //</editor-fold>

    public ImageView imgDSP01;
    public ImageView imgDSP02;
    public ImageView imgDSP03;
    public ImageView imgDSP04;
    public ScrollPane scrlpneMain;
    public HBox btnCheckMissingISRCs;
    public ComboBox<String> comboPayees;
    private final UIController mainUIController;

    public ControllerRevenueGenerator(UIController uiController) {
        mainUIController = uiController;
    }

    public void loadRevenueGenerator() throws IOException {
        FXMLLoader loaderMain = new FXMLLoader(ControllerSettings.class.getResource("layouts/revenue-generator.fxml"));
        FXMLLoader loaderSide = new FXMLLoader(ControllerSettings.class.getResource("layouts/sidepanel-revenue-analysis.fxml"));
        loaderMain.setController(this);
        loaderSide.setController(this);
        Parent newContentMain = loaderMain.load();
        Parent newContentSide = loaderSide.load();
        ItemSwitcher itemSwitcher = new ItemSwitcher();

        mainUIController.mainVBox.getChildren().setAll(newContentMain);
        mainUIController.sideVBox.getChildren().setAll(newContentSide);

        Task<Void> task;

        task = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    try {
                        if (InitPreloader.month != null) {
                            lblTitleMonth.setText(itemSwitcher.setMonth(InitPreloader.month));
                        }
                        loadTopStreamedAssets(InitPreloader.top5StreamedAssets);
                        if (InitPreloader.count != null) {
                            lblTotalAssets.setText(InitPreloader.count);
                        }
                        loadTop5Territories(InitPreloader.top5Territories);
                        loadTop4DSPs(InitPreloader.top4DSPs);
                        // scrlpneMain.setVvalue(0.0);
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
                return null;
            }
        };

        Thread t = new Thread(task);
        t.start();
    }

    private void loadTop5Territories(ResultSet rs) throws SQLException, ClassNotFoundException {
        if (rs != null) {
            rs.beforeFirst();
            DecimalFormat df = new DecimalFormat("0.00");
            double revenue;
            String currency;
            ItemSwitcher itemSwitcher = new ItemSwitcher();

            rs.next();
            lblCountry01.setText(itemSwitcher.setCountry(rs.getString(1)));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblCountry01Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblCountry02.setText(itemSwitcher.setCountry(rs.getString(1)));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblCountry02Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblCountry03.setText(itemSwitcher.setCountry(rs.getString(1)));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblCountry03Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblCountry04.setText(itemSwitcher.setCountry(rs.getString(1)));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblCountry04Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblCountry05.setText(itemSwitcher.setCountry(rs.getString(1)));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblCountry05Streams.setText(currency + " " + df.format(revenue));
        }
    }

    private void loadTop4DSPs(ResultSet rs) throws SQLException, ClassNotFoundException {
        if (rs != null) {
            rs.beforeFirst();
            DecimalFormat df = new DecimalFormat("0.00");
            ItemSwitcher itemSwitcher = new ItemSwitcher();
            double revenue;
            String currency;

            rs.next();
            lblDSP01.setText(rs.getString(1));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblAmountDSP01.setText(currency + " " + df.format(revenue));
            imgDSP01.setImage(itemSwitcher.setImage(rs.getString(1)));

            rs.next();
            lblDSP02.setText(rs.getString(1));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblAmountDSP02.setText(currency + " " + df.format(revenue));
            imgDSP02.setImage(itemSwitcher.setImage(rs.getString(1)));

            rs.next();
            lblDSP03.setText(rs.getString(1));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblAmountDSP03.setText(currency + " " + df.format(revenue));
            imgDSP03.setImage(itemSwitcher.setImage(rs.getString(1)));

            rs.next();
            lblDSP04.setText(rs.getString(1));
            revenue = rs.getDouble(2);
            currency = rs.getString(3);
            lblAmountDSP04.setText(currency + " " + df.format(revenue));
            imgDSP04.setImage(itemSwitcher.setImage(rs.getString(1)));
        }
    }

    private void loadTopStreamedAssets(ResultSet rs) throws SQLException, ClassNotFoundException {
        if (rs != null) {
            rs.beforeFirst();
            DecimalFormat df = new DecimalFormat("0.00");
            double revenue;
            String currency;

            rs.next();
            lblAsset01.setText(rs.getString(2));
            revenue = rs.getDouble(3);
            currency = rs.getString(4);
            lblAsset01Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblAsset02.setText(rs.getString(2));
            revenue = rs.getDouble(3);
            currency = rs.getString(4);
            lblAsset02Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblAsset03.setText(rs.getString(2));
            revenue = rs.getDouble(3);
            currency = rs.getString(4);
            lblAsset03Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblAsset04.setText(rs.getString(2));
            revenue = rs.getDouble(3);
            currency = rs.getString(4);
            lblAsset04Streams.setText(currency + " " + df.format(revenue));

            rs.next();
            lblAsset05.setText(rs.getString(2));
            revenue = rs.getDouble(3);
            currency = rs.getString(4);
            lblAsset05Streams.setText(currency + " " + df.format(revenue));
        }
    }

    public void onLoadReportButtonClick() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select FUGA Report");

        File report = chooser.showOpenDialog(mainUIController.mainVBox.getScene().getWindow());

        if (report != null) {
            btnLoadReport.setText("Working on...");

            Task<Void> task = loadReport(report);

            Thread t = new Thread(task);
            t.start();
        } else {
            System.out.println("No Report Imported");
        }
    }

    private Task<Void> loadReport(File report) {
        Task<Void> task;
        task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                boolean status = DatabaseMySQL.loadReport(report, btnLoadReport);

                Platform.runLater(() -> {
                    if (status) {
                        btnLoadReport.setText("CSV Imported to Database");
                    }
                });
                return null;
            }
        };
        return task;
    }

    public void onGenerateFullBreakdownBtnClick(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            try {
                ResultSet rs = DatabaseMySQL.getFullBreakdown();
                Path tempDir = Files.createTempDirectory("ceymusic_dashboard");
                Path csvFile = tempDir.resolve("revenue_breakdown_full.csv");

                CSVWriter writer = new CSVWriter(new FileWriter(csvFile.toFile()));
                List<String[]> rows = new ArrayList<>();
                String[] header = new String[]{
                        "ISRC",
                        "Reported Royalty Summary",
                        "AU Earnings",
                        "After GST Deduction",
                        "Rest of the world Earnings",
                        "Reported Royalty After GST",
                        "Reported Royalty for CeyMusic"
                };
                rows.add(header);

                while (rs.next()) {
                    System.out.println("rs.getString(1) = " + rs.getString(1));

                    String[] row = new String[]{
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7)
                    };

                    rows.add(row);
                }

                writer.writeAll(rows);
                writer.close();

                Node node = (Node) mouseEvent.getSource();
                Scene scene = node.getScene();
                File destination = Main.browseLocationNew(scene.getWindow());
                Path destinationPath = destination.toPath().resolve(csvFile.getFileName());
                Files.copy(csvFile, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File copied successfully to " + destinationPath);
            } catch (SQLException | ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void onGenerateReportDSPClicked(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            try {
                ResultSet rs = DatabaseMySQL.getBreakdownByDSP();
                Path tempDir = Files.createTempDirectory("ceymusic_dashboard");
                Path csvFile = tempDir.resolve("revenue_breakdown_dsp.csv");
                CSVWriter writer = new CSVWriter(new FileWriter(csvFile.toFile()));
                List<String[]> rows = new ArrayList<>();
                String[] header = new String[]{
                        "ISRC",
                        "Reported Royalty Summary",
                        "Youtube Ad Supported",
                        "Youtube Music",
                        "Spotify",
                        "TikTok",
                        "Apple Music",
                        "Facebook",
                        "Others"
                };
                rows.add(header);

                while (rs.next()) {
                    System.out.println("rs.getString(1) = " + rs.getString(1));

                    String[] row = new String[]{
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7),
                            rs.getString(8),
                            rs.getString(9)
                    };

                    rows.add(row);
                }

                writer.writeAll(rows);
                writer.close();

                Node node = (Node) mouseEvent.getSource();
                Scene scene = node.getScene();
                File destination = Main.browseLocationNew(scene.getWindow());
                Path destinationPath = destination.toPath().resolve(csvFile.getFileName());
                Files.copy(csvFile, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File copied successfully to " + destinationPath);
            } catch (SQLException | ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void onGenerateReportTerritoryBtnClicked(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            try {
                ResultSet rs = DatabaseMySQL.getBreakdownByTerritory();

                Path tempDir = Files.createTempDirectory("ceymusic_dashboard");
                Path csvFile = tempDir.resolve("revenue_breakdown_territory.csv");
                CSVWriter writer = new CSVWriter(new FileWriter(csvFile.toFile()));
                List<String[]> rows = new ArrayList<>();
                String[] header = new String[]{
                        "ISRC",
                        "Reported Royalty Summary",
                        "AU",
                        "US",
                        "GB",
                        "IT",
                        "KR",
                        "LK",
                        "AE",
                        "JP",
                        "CA",
                        "Rest"
                };
                rows.add(header);

                while (rs.next()) {
                    System.out.println("rs.getString(1) = " + rs.getString(1));

                    String[] row = new String[]{
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7),
                            rs.getString(8),
                            rs.getString(9),
                            rs.getString(10),
                            rs.getString(11),
                            rs.getString(12)
                    };

                    rows.add(row);
                }

                writer.writeAll(rows);
                writer.close();

                Node node = (Node) mouseEvent.getSource();
                Scene scene = node.getScene();
                File destination = Main.browseLocationNew(scene.getWindow());
                Path destinationPath = destination.toPath().resolve(csvFile.getFileName());
                Files.copy(csvFile, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File copied successfully to " + destinationPath);
            } catch (SQLException | ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void onCheckMissingISRCsBtnClick(MouseEvent mouseEvent) throws SQLException, ClassNotFoundException, IOException {
        ResultSet resultSet = DatabaseMySQL.checkMissingISRCs();

        Path tempDir = Files.createTempDirectory("missing_isrcs");
        Path csvFile = tempDir.resolve("missing_isrcs.csv");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile.toFile()));

        List<String[]> rows = new ArrayList<>();

        Node node = (Node) mouseEvent.getSource();
        Scene scene = node.getScene();

        while (resultSet.next() && ((resultSet.getString(2) == null) && (resultSet.getString(3) == null))) {
            String[] row = new String[]{
                    resultSet.getString(1)
            };
            rows.add(row);
        }

        csvWriter.writeAll(rows);
        csvWriter.close();

        showErrorDialogWithLog("Missing ISRCs", rows.size() + " Missing ISRCs", "Click OK to Save List of Missing ISRCs", scene.getWindow());
    }

    public void onUpdateSongsDatabaseBtnClick(MouseEvent mouseEvent) throws CsvValidationException, IOException, SQLException, ClassNotFoundException {
        Node node = (Node) mouseEvent.getSource();
        Scene scene = node.getScene();
        Window window = scene.getWindow();
        File file = Main.browseForFile(window);
        boolean status = DatabaseMySQL.updateSongsTable(file);

        if (status) {
            lblUpdateSongsDatabase.setText("Database Updated");
        } else {
            lblUpdateSongsDatabase.setText("Error");
        }
    }

    public void onUpdatePayeeDetailsBtnClick(MouseEvent mouseEvent) throws IOException, CsvValidationException, SQLException, ClassNotFoundException {
        Node node = (Node) mouseEvent.getSource();
        Scene scene = node.getScene();
        Window window = scene.getWindow();
        File file = Main.browseForFile(window);
        boolean status = DatabaseMySQL.updatePayeeDetails(file);

        if (status) {
            lblUpdatePayee.setText("Payee List Updated");
        } else {
            lblUpdatePayee.setText("Error");
        }
    }

    private static void showErrorDialogWithLog(String title, String headerText, String contentText, Window window) throws IOException {
        // Alert
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);

        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);

        Optional<String> dialogResult = dialog.showAndWait();

        // Optional<ButtonType> result = alert.showAndWait();

        if (dialogResult.isPresent()) {
            Path sourcePath = Paths.get("missing_isrcs.csv");
            File destination = Main.browseLocationNew(window);
            Path destinationPath = destination.toPath().resolve(sourcePath.getFileName());
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully to " + destinationPath);
        }
    }

    public void loadArtistReports() throws IOException {
        FXMLLoader loaderMain = new FXMLLoader(ControllerSettings.class.getResource("layouts/artist-reports.fxml"));
        // FXMLLoader loaderSide = new FXMLLoader(ControllerSettings.class.getResource("layouts/sidepanel-revenue-analysis.fxml"));
        loaderMain.setController(this);
        // loaderSide.setController(this);
        Parent newContentMain = loaderMain.load();
        // Parent newContentSide = loaderSide.load();
        ItemSwitcher itemSwitcher = new ItemSwitcher();

        mainUIController.mainVBox.getChildren().setAll(newContentMain);
        // mainUIController.sideVBox.getChildren().setAll(newContentSide);

        Task<Void> task;

        task = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    try {
                        ResultSet rsPayees = DatabaseMySQL.getPayees();
                        while (rsPayees.next()) {
                            comboPayees.getItems().add(rsPayees.getString(1));
                        }
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
                return null;
            }
        };

        Thread t = new Thread(task);
        t.start();
    }

    public void OnComboPayeeKeyPress(KeyEvent event) {
        String s = jumpTo(event.getText(), comboPayees.getValue(), comboPayees.getItems());
        if (s != null) {
            comboPayees.setValue(s);
        }
    }

    static String jumpTo(String keyPressed, String currentlySelected, List<String> items) {
        String key = keyPressed.toUpperCase();
        if (key.matches("^[A-Z]$")) {
            // Only act on letters so that navigating with cursor keys does not
            // try to jump somewhere.
            boolean letterFound = false;
            boolean foundCurrent = currentlySelected == null;
            for (String s : items) {
                if (s.toUpperCase().startsWith(key)) {
                    letterFound = true;
                    if (foundCurrent) {
                        return s;
                    }
                    foundCurrent = s.equals(currentlySelected);
                }
            }
            if (letterFound) {
                return jumpTo(keyPressed, null, items);
            }
        }
        return null;
    }

    public void comboPayeeOnAction() {
        String selectedItem = comboPayees.getSelectionModel().getSelectedItem();
        System.out.println("Selected item: " + selectedItem);
    }
}
