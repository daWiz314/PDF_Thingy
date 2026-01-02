package PDF_Tool;

import PDF_Ext.classes.SearchResult;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Main {

    static public ProgressBar myProgressBar;
    static public ProgressBar overallProgress;
    static public Label statusLabel;
    static public Label overallStatusLabel;
    static public Button searchStop;

    static Set<File> selectedFiles;
    static String searchQuery;

    static public Task<Void> activeSearchTask;
    static public Consumer<SearchResult> call_back_function_for_matches;

    public void main() {
        System.out.println("Hello World");
    }

    public static void updateSelectedFile(Set<File> files) {
        selectedFiles = files;
        System.out.println("Updated files to: " + selectedFiles);
    }

    public static void updateSearchQuery(String newSearch) {
        searchQuery = newSearch;
        System.out.println("Updated search query to: " + searchQuery);
    }

    public static void attemptSearch() {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            overallStatusLabel.setText("Please select files first!");
        return;
        }
        activeSearchTask = new Task<>() {
            @Override protected Void call() throws Exception {
                int fileCount = 0;
                int totalFiles = selectedFiles.size();

                for (File file : selectedFiles) {
                    if (isCancelled()) break;

                    fileCount++;

                    updateProgress(fileCount, totalFiles);
                    updateMessage("Scanning file " + fileCount + " of " + totalFiles + ": " + file.getName());

                    try (PDDocument doc = Loader.loadPDF(file)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        int totalPages = doc.getNumberOfPages();

                        for (int i = 1; i <= totalPages; i++) {
                            if (isCancelled()) break;

                            stripper.setStartPage(i);
                            stripper.setEndPage(i);
                            String pageText = stripper.getText(doc);

                            if (pageText.toLowerCase().contains(searchQuery.toLowerCase())) {
                                int page = i;
                                javafx.application.Platform.runLater(() -> {
                                    call_back_function_for_matches.accept(new SearchResult(file, page));
                                    System.out.println("Found match on page " + page);

                                });
                            }

                            double filePercent = (double) i / totalPages;
                            int pages = i;
                            javafx.application.Platform.runLater(() -> {
                                statusLabel.setText("Page " + pages + " out of " + totalPages);
                                myProgressBar.setProgress(filePercent);
                            });
                        }
                        doc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };

        searchStop.setDisable(false);

        overallProgress.progressProperty().bind(activeSearchTask.progressProperty());
        overallStatusLabel.textProperty().bind(activeSearchTask.messageProperty());

        activeSearchTask.runningProperty().addListener((obs, wasRunning, isNowRunning) -> {
            if (!isNowRunning) {
                javafx.application.Platform.runLater(() -> {
                    searchStop.setDisable(true);
                    unbindProgress();
                    
                    // If the user cancelled, set a specific status
                    if (activeSearchTask.isCancelled()) {
                        overallStatusLabel.setText("Search Stopped.");
                        statusLabel.setText("Idle");
                    }
                });
            }
        });

        activeSearchTask.setOnSucceeded(e -> {
        statusLabel.setText("Search Complete!");
        });

        activeSearchTask.setOnFailed(e -> {
        overallStatusLabel.setText("Error during scan.");
        activeSearchTask.getException().printStackTrace();
        });

        new Thread(activeSearchTask).start();
    }

    public static void stop() {
        if (activeSearchTask != null && activeSearchTask.isRunning()) {
            activeSearchTask.cancel();
            statusLabel.setText("Stopping...");
        }
    }

    public static void unbindProgress() {
        overallProgress.progressProperty().unbind();
        overallStatusLabel.textProperty().unbind();
    }
}
