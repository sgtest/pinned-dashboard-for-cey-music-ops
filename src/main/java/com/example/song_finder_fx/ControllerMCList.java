package com.example.song_finder_fx;

import com.example.song_finder_fx.Controller.*;
import com.example.song_finder_fx.Model.ManualClaimTrack;
import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ControllerMCList {

    @FXML
    public javafx.scene.control.Button btnArchive;

    @FXML
    private Label lblClaimCount;

    @FXML
    private VBox vbClaimsList;

    @FXML
    private Label lblPaginationInfo;


    public static List<CheckBox> checkBoxes = new ArrayList<>();

    public static List<HBox> hBoxes = new ArrayList<>();

    public static List<Label> labelsSongNo = new ArrayList<>();

    public static List<Label> labelsSongName = new ArrayList<>();

    public static List<ImageView> ivArtworks = new ArrayList<>();

    public static List<Label> labelsComposer = new ArrayList<>();

    public static List<Label> labelsLyricist = new ArrayList<>();

    // public static List<ManualClaimTrack> manualClaims = new ArrayList<>();

    public static List<ManualClaimTrack> finalManualClaims = new ArrayList<>();

    public static Map<Integer, ManualClaimTrack> claimMap = new HashMap<>();

    private final int pageSize = 50;

    private int currentPage = 0;

    public static List<ManualClaimTrack> allManualClaims;

    private final List<Node> allClaimEntries = new ArrayList<>();

    private volatile Thread threadValidation;

    private volatile Thread threadArtworks;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> debounceFuture;

    private static final long DEBOUNCE_DELAY = 300; // milliseconds

    @FXML
    public void initialize() throws SQLException, IOException {
        checkBoxes.clear();
        hBoxes.clear();
        labelsSongNo.clear();
        labelsSongName.clear();
        labelsComposer.clear();
        labelsLyricist.clear();
        ivArtworks.clear();

        lblClaimCount.setText("Loading...");

        Task<Void> taskGetManualClaims = getTaskLoadManualClaims();

        taskGetManualClaims.setOnSucceeded(event -> debouncedRunValidationAndArtworkThreads());

        Thread threadGetManualClaims = new Thread(taskGetManualClaims);
        threadGetManualClaims.start();

    }

    private @NotNull Task<Void> getTaskLoadManualClaims() {
        return new Task<>() {
            @Override
            protected Void call() throws SQLException {
                // Platform.runLater(() -> UIController.lblDatabaseStatusStatic.setText("Loading Manual Claims"));
                allManualClaims = DatabasePostgres.getManualClaims();
                // manualClaims = DatabasePostgres.getManualClaims();

                // Pagination Modification
                for (ManualClaimTrack claim : allManualClaims) {
                    claimMap.put(claim.getId(), claim);

                    Node node;
                    try {
                        node = createClaimEntryNode(claim);
                        allClaimEntries.add(node);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                Platform.runLater(() -> {
                    try {
                        lblClaimCount.setText(DatabasePostgres.getManualClaimCount());
                        displayCurrentPage();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                return null;
            }
        };
    }

    private void debouncedRunValidationAndArtworkThreads() {
        if (debounceFuture != null) {
            debounceFuture.cancel(false);
        }

        debounceFuture = scheduler.schedule(this::runValidationAndArtworkThreads, DEBOUNCE_DELAY, TimeUnit.MILLISECONDS);
    }

    private void runValidationAndArtworkThreads() {
        stopExistingThreads();

        threadValidation = new Thread(() -> {
            try {
                int startIndex = currentPage * pageSize;
                int endIndex = Math.min(startIndex + pageSize, allManualClaims.size());

                for (int i = startIndex; i < endIndex; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return; // Exit the method if the thread has been interrupted
                    }

                    final int index = i;
                    String composer = labelsComposer.get(i).getText();
                    boolean status = DatabasePostgres.checkIfArtistValidated(composer);
                    if (!status) {
                        Platform.runLater(() -> labelsComposer.get(index).setStyle("-fx-text-fill: red"));
                    }

                    String lyricist = labelsLyricist.get(i).getText();
                    status = DatabasePostgres.checkIfArtistValidated(lyricist);
                    if (!status) {
                        Platform.runLater(() -> labelsLyricist.get(index).setStyle("-fx-text-fill: red"));
                    }
                }
            } catch (SQLException e) {
                Platform.runLater(e::printStackTrace);
            }
        });

        threadArtworks = new Thread(() -> {
            int startIndex = currentPage * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allManualClaims.size());

            for (int i = startIndex; i < endIndex; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    return; // Exit the method if the thread has been interrupted
                }

                final int index = i;
                ImageView imageView = ivArtworks.get(i);
                MCTrackController controller = new MCTrackController(allManualClaims.get(i));
                try {
                    Platform.runLater(() -> System.out.println("Fetching Artworks for: " + allManualClaims.get(index).getTrackName()));
                    ManualClaimTrack updatedClaim = controller.fetchArtwork();
                    allManualClaims.set(index, updatedClaim);
                    Platform.runLater(() -> {
                        try {
                            imageView.setImage(setImage(updatedClaim, index));
                        } catch (IOException | URISyntaxException e) {
                            Platform.runLater(e::printStackTrace);
                        }
                    });
                } catch (SQLException e) {
                    Platform.runLater(e::printStackTrace);
                }
            }
        });

        threadArtworks.start();
        threadValidation.start();
    }

    private void stopExistingThreads() {
        if (threadValidation != null) {
            threadValidation.interrupt();
            try {
                threadValidation.join(1000); // Wait for up to 1 second for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (threadArtworks != null) {
            threadArtworks.interrupt();
            try {
                threadArtworks.join(1000); // Wait for up to 1 second for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void displayCurrentPage() {
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allClaimEntries.size());

        vbClaimsList.getChildren().clear();
        for (int i = startIndex; i < endIndex; i++) {
            vbClaimsList.getChildren().add(allClaimEntries.get(i));
        }

        int displayStart = startIndex + 1;
        int totalClaims = allManualClaims.size();

        int currentPageNumber = currentPage + 1;
        int totalPages = (int) Math.ceil((double) totalClaims / pageSize);
        String paginationInfo = String.format("Page %d of %d (%d-%d of %d claims)",
                currentPageNumber, totalPages,
                displayStart, endIndex, totalClaims);
        System.out.println(paginationInfo);

        // Assuming you have a Label called lblPaginationInfo in your FXML
        lblPaginationInfo.setText(paginationInfo);

        lblClaimCount.setText(String.valueOf(allManualClaims.size()));
        // Update pagination controls (e.g., enable/disable next/previous buttons)
        updatePaginationControls();

        // Run validation and artwork threads for the current page
        debouncedRunValidationAndArtworkThreads();
    }

    private void updatePaginationControls() {
        // TODO: Implement this method to update your pagination controls
        // For example, enable/disable next/previous buttons based on currentPage
    }

    public void nextPage() {
        if ((currentPage + 1) * pageSize < allClaimEntries.size()) {
            currentPage++;
            displayCurrentPage();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            displayCurrentPage();
        }
    }

    private Node createClaimEntryNode(ManualClaimTrack claim) throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("layouts/manual_claims/manual-claims-list-entry.fxml")));

        Label lblSongNo = (Label) node.lookup("#lblSongNo");
        labelsSongNo.add(lblSongNo);
        Label lblSongName = (Label) node.lookup("#lblSongName");
        labelsSongName.add(lblSongName);
        Label lblComposer = (Label) node.lookup("#lblComposer");
        labelsComposer.add(lblComposer);
        Label lblLyricist = (Label) node.lookup("#lblLyricist");
        labelsLyricist.add(lblLyricist);
        Label lblDate = (Label) node.lookup("#lblDate");
        Label lblClaimType = (Label) node.lookup("#lblClaimType");
        CheckBox checkBox = (CheckBox) node.lookup("#checkBox");
        checkBoxes.add(checkBox);
        HBox hboxEntry = (HBox) node.lookup("#hboxEntry");
        hBoxes.add(hboxEntry);
        ImageView image = (ImageView) node.lookup("#image");
        ivArtworks.add(image);

        lblSongNo.setText(String.valueOf(claim.getId()));
        lblSongName.setText(claim.getTrackName());
        lblComposer.setText(claim.getComposer());
        lblLyricist.setText(claim.getLyricist());
        lblDate.setText(TextFormatter.getDaysAgo(claim.getDate()));
        lblDate.setStyle(setColor(claim.getDate()));
        lblClaimType.setText(claim.getClaimTypeString());

        return node;
    }

    private Image setImage(ManualClaimTrack claim, int listIndex) throws IOException, URISyntaxException {
        if (claim.getPreviewImage() != null) {
            return claim.getPreviewImage();
        } else {
            String youtubeID = claim.getYoutubeID();
            String thumbnailURL = "https://i.ytimg.com/vi/" + youtubeID + "/maxresdefault.jpg";
            BufferedImage image = ImageProcessor.getDownloadedImage(thumbnailURL);
            image = ImageProcessor.cropImage(image);

            // Setting Thumbnail and Preview Images to the model
            // Resize the image for preview (adjust dimensions as needed)
            int previewWidth = 200; // Adjust this value to your desired preview width
            int previewHeight = 200; // Adjust this value to your desired preview height
            BufferedImage previewImage = ImageProcessor.resizeImage(previewWidth, previewHeight, image);
            claim.setPreviewImage(previewImage);
            image = ImageProcessor.resizeImage(1400, 1400, image);
            claim.setImage(image);

            allManualClaims.set(listIndex, claim);

            return SwingFXUtils.toFXImage(image, null);
        }
    }

    private String setColor(LocalDate localDate) {
        // Get the current system date
        LocalDate currentDate = LocalDate.now();

        // Calculate the difference in days
        long daysDifference = ChronoUnit.DAYS.between(localDate, currentDate);

        // Determine the appropriate label based on the difference
        String label;
        if (daysDifference == 0) {
            label = "-fx-text-fill: #72a276";
        } else if (daysDifference == 1) {
            // #F28F3B
            label = "-fx-text-fill: #F28F3B";
        } else {
            // #A72608
            label = "-fx-text-fill: #A72608";
        }
        return label;
    }

    @FXML
    void onGoBack(MouseEvent event) throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(ControllerSettings.class.getResource("layouts/manual_claims/manual-claims-main.fxml")));
        Scene scene = ((Node) event.getSource()).getScene();
        VBox mainVBox = (VBox) scene.lookup("#mainVBox");
        mainVBox.getChildren().setAll(node);
    }

    @FXML
    void onCheck(ActionEvent event) throws IOException {
        finalManualClaims.clear();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                // ID, Name, Composer, Lyricist
                int id = allManualClaims.get(i).getId();
                ManualClaimTrack claim = claimMap.get(id);
                System.out.println("claim.getBufferedImage() = " + allManualClaims.get(i).getBufferedImage());
                finalManualClaims.add(claim);
                // finalSongNames.add(labelsSongName.get(i).getText());
            }
        }

        Node node = SceneController.loadLayout("layouts/manual_claims/manual-claims-identifiers.fxml");
        Scene scene = SceneController.getSceneFromEvent(event);
        VBox main = SceneController.getMainVBox(scene);
        main.getChildren().clear();
        main.getChildren().add(node);
    }

    @FXML
    void onSelectNone() {
        int count = 0;

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allManualClaims.size());

        for (int i = startIndex; i < endIndex; i++) {
            checkBoxes.get(i).setSelected(false);
            count++;
        }

        System.out.println(count + " items deselected");
    }

    @FXML
    void onSelectAll() {
        int count = 0;

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allManualClaims.size());

        for (int i = startIndex; i < endIndex; i++) {
            checkBoxes.get(i).setSelected(true);
            count++;
        }

        System.out.println(count + " items selected");
    }

    public void onExportSelected(ActionEvent actionEvent) {
        List<ManualClaimTrack> selectedClaims = new ArrayList<>();

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selectedClaims.add(allManualClaims.get(i));
            }
        }

        if (selectedClaims.isEmpty()) {
            System.out.println("No claims selected. Aborting export.");
        } else {
            File file = showSaveDialog(actionEvent);
            if (file == null) {
                System.out.println("No file selected. Aborting export.");
            } else {
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".csv")) {
                    path += ".csv";
                }

                try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
                    List<String[]> rows = getRows(selectedClaims);

                    writer.writeAll(rows);
                    System.out.println("CSV file created successfully: " + path);

                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(new File(path));
                    }
                } catch (IOException e) {
                    System.err.println("Error creating CSV file: " + e.getMessage());
                    // You might want to show an error dialog to the user here
                }
            }
        }
    }

    private static @NotNull List<String[]> getRows(List<ManualClaimTrack> selectedClaims) {
        List<String[]> rows = new ArrayList<>();

        // Add header
        String[] header = {"ID", "Track Name", "Lyricist", "Composer", "YouTube ID", "YouTube URL", "Date", "Trim Start", "Trim End", "Claim Type"};
        rows.add(header);

        // Add data rows
        for (ManualClaimTrack claim : selectedClaims) {
            String[] row = {
                    String.valueOf(claim.getId()),
                    claim.getTrackName(),
                    claim.getLyricist(),
                    claim.getComposer(),
                    claim.getYoutubeID(),
                    claim.getYouTubeURL(),
                    claim.getDate().toString(),
                    claim.getTrimStart() != null ? claim.getTrimStart() : "",
                    claim.getTrimEnd() != null ? claim.getTrimEnd() : "",
                    claim.getClaimTypeString()
            };
            rows.add(row);
        }
        return rows;
    }

    static File showSaveDialog(ActionEvent actionEvent) {
        // Getting User Location
        Node node = (Node) actionEvent.getSource();
        Scene scene = node.getScene();
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        chooser.setTitle("Save As");
        return chooser.showSaveDialog(scene.getWindow());
    }

    @FXML
    void onArchiveSelected() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                Platform.runLater(() -> {
                    btnArchive.setText("Archiving Claims");
                    btnArchive.setDisable(true);
                });

                int count;
                int archivedCount = 0;

                count = (int) checkBoxes.stream().filter(CheckBox::isSelected).count();

                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        String songNo = labelsSongNo.get(i).getText();
                        try {
                            DatabasePostgres.archiveSelectedClaim(songNo);
                            archivedCount++;
                            int finalI = i;
                            int finalArchivedCount = archivedCount;
                            Platform.runLater(() -> {
                                hBoxes.get(finalI).setDisable(true);
                                checkBoxes.get(finalI).setSelected(false);
                                btnArchive.setText("Archiving " + finalArchivedCount + " of " + count);
                            });
                        } catch (SQLException e) {
                            Platform.runLater(() -> {
                                AlertBuilder.sendErrorAlert("Error", "Something went wrong when archiving manual claim", e.toString());
                                e.printStackTrace();
                            });
                        }
                    }
                }

                Platform.runLater(() -> {
                    btnArchive.setText("Archive Selected");
                    btnArchive.setDisable(false);
                });

                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    public void onBackPage() {
        previousPage();
    }

    public void onNextPage() {
        nextPage();
    }
}
