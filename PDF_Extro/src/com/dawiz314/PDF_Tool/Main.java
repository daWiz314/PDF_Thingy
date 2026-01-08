package PDF_Tool;

import PDF_Ext.classes.SearchResult;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.io.IOUtils;

public class Main {
    // UI Elements for updating progress from the search task
    static public ProgressBar myProgressBar;    // Top progress bar - Gives incremental progress of current file
    static public ProgressBar overallProgress;  // Bottom ProgressBar - Gives incremental progress of overall search - BOUND TO TASK
    static public Label statusLabel;            // Top status label - Gives current page being scanned
    static public Label overallStatusLabel;     // Bottom status label - Gives overall search status                 - BOUND TO TASK
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
        System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");
        MemoryUsageSetting memSetting = MemoryUsageSetting.setupMixed(512 * 1024 * 1024);
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            overallStatusLabel.setText("Please select files first!");
            return;
        }

        activeSearchTask = new Task<>() {
            @Override protected Void call() throws Exception {
                int fileCount = 0;
                int totalFiles = selectedFiles.size();
                int pageCount = 0;
                int totalOverallPages = 0;

                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Starting pre-scan...");
                });

                for (File file : selectedFiles) {
                    statusLabel.setText("Starting pre-scan...");
                    // This lambda tells PDFBox to create a temporary file cache for this document
                    try (PDDocument doc = Loader.loadPDF(file, IOUtils.createTempFileOnlyStreamCache())) {
                        totalOverallPages += doc.getNumberOfPages();
                        updateProgress(fileCount, totalFiles);
                        updateMessage("Pre-scanning file " + (fileCount + 1) + " of " + totalFiles + ": " + file.getName() + "\nPages scanned: " + totalOverallPages);
                        doc.close();
                        fileCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                fileCount = 0;
                for (File file : selectedFiles) {
                    if (isCancelled()) break;

                    fileCount++;
                    System.err.println("Scanning file " + fileCount + " of " + totalFiles + ": " + file.getName());

                    updateProgress(fileCount, totalFiles);
                    updateMessage("Scanning file " + fileCount + " of " + totalFiles + ": " + file.getName() + "\nPages scanned: " + pageCount + " / " + totalOverallPages);

                    try (PDDocument doc = Loader.loadPDF(file, IOUtils.createMemoryOnlyStreamCache())) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        stripper.setSortByPosition(false);
                        int totalPages = doc.getNumberOfPages();

                        for (int i = 1; i <= totalPages; i++) {
                            try {
                                updateMessage("Scanning file " + fileCount + " of " + totalFiles + ": " + file.getName() + "\nPages scanned: " + pageCount + " / " + totalOverallPages);
                                if (isCancelled()) break;
                                pageCount++;
                                stripper.setStartPage(i);
                                stripper.setEndPage(i);
                                String pageText = stripper.getText(doc);

                                if (pageText.toLowerCase().contains(searchQuery.toLowerCase())) {
                                    final int page;
                                    if (i > 0) {
                                        page = i - 1; // Convert to zero-based index
                                    } else {
                                        page = i;
                                    }

                                    javafx.application.Platform.runLater(() -> {
                                        call_back_function_for_matches.accept(new SearchResult(file, page));
                                        // System.out.println("Found match on page " + page);

                                    });
                                }

                                double filePercent = (double) i / totalPages;
                                int pages = i;
                                javafx.application.Platform.runLater(() -> {
                                    statusLabel.setText("Page " + pages + " out of " + totalPages);
                                    myProgressBar.setProgress(filePercent);
                                });
                            } catch (java.io.EOFException e) {
                                System.err.println("\n\nReached unexpected end of file while reading page " + i + " of file: " + file.getAbsolutePath());
                                // e.printStackTrace();
                            } catch (Exception e) {
                                System.err.println("\n\nError processing page " + i + " of file: " + file.getAbsolutePath());
                                // e.printStackTrace();
                            }
                        }
                        updateMessage("Scanning file " + fileCount + " of " + totalFiles + ": " + file.getName() + "\nPages scanned: " + pageCount + " / " + totalOverallPages);
                        doc.close();
                    } catch (java.io.EOFException e) {
                        System.err.println("\n\n");
                        System.err.println("\n\nError: Reached unexpected end of file while reading: " + file.getAbsolutePath());
                        // e.printStackTrace();
                    } catch (IOException e) {
                        // This way we can try and figure out what broke
                        System.err.println("\n\nError reading file: " + file.getAbsolutePath());
                        // e.printStackTrace();
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
            unbindProgress();
        statusLabel.setText("Search Complete!");
        });

        activeSearchTask.setOnFailed(e -> {
            unbindProgress();
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
