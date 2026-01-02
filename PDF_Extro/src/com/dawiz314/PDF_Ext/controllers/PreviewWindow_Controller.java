package PDF_Ext.controllers;

import PDF_Ext.classes.TextLocationFinder;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class PreviewWindow_Controller {
    
    private File currentFile;
    private int currentPage = 0;
    private int totalPages = 0;
    private String searchTerm = ""; // Store the term to highlight

    @FXML private Label TitleText;
    @FXML private Label pageLabel;
    @FXML private Label zoomLabel;
    @FXML private Slider zoomSlider;
    @FXML private ScrollPane scrollableArea;
    @FXML private ImageView pdfImageView;

    public void initialize() {
        TitleText.setText("Please select a file!");
        scrollableArea.setVisible(false);
        pdfImageView.setVisible(false);

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLabel.setText(newVal.intValue() + " DPI");
        });

        zoomSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) { 
                renderPage(currentPage);
            }
        });

        javafx.application.Platform.runLater(() -> {
            if (pdfImageView.getScene() != null) {
                pdfImageView.getScene().setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case RIGHT: case DOWN: handleNextPage(); break;
                        case LEFT: case UP: handlePreviousPage(); break;
                        default: break;
                    }
                });
            }
        });
    }

    // New method to set the search term from the Index_Controller
    public void setHighlightTerm(String term) {
        this.searchTerm = term;
        renderPage(currentPage);
    }

    public void setPdfFile(File file) {
        setPdfFile(file, 0);
    }

    public void setPdfFile(File file, int page) {
        this.currentFile = file;
        this.currentPage = page; 
        
        TitleText.setText("Viewing: " + file.getName());
        scrollableArea.setVisible(true);
        pdfImageView.setVisible(true);

        try (PDDocument document = Loader.loadPDF(file)) {
            this.totalPages = document.getNumberOfPages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        renderPage(currentPage); 
        javafx.application.Platform.runLater(this::handleFitToWidth);
    }

    private void renderPage(int pageIndex) {
        if (currentFile == null) return;

        javafx.concurrent.Task<WritableImage> renderTask = new javafx.concurrent.Task<>() {
            @Override
            protected WritableImage call() throws Exception {
                try (PDDocument document = Loader.loadPDF(currentFile)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    float dpi = (float) zoomSlider.getValue();
                    float scale = dpi / 72f; // Convert PDF points to pixels

                    // 1. Render the base page image
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, dpi);

                    // 2. If there is a search term, find and draw highlights
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        Graphics2D g2d = bim.createGraphics();
                        g2d.setColor(new Color(255, 255, 0, 100)); // Transparent Yellow

                        TextLocationFinder finder = new TextLocationFinder(searchTerm);
                        finder.setStartPage(pageIndex + 1);
                        finder.setEndPage(pageIndex + 1);
                        
                        // writeText triggers the coordinate finding logic
                        java.io.Writer dummy = new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream());
                        finder.writeText(document, dummy);

                        for (Rectangle2D rect : finder.getRegions()) {
                            g2d.fillRect(
                                (int) (rect.getX() * scale),
                                (int) (rect.getY() * scale),
                                (int) (rect.getWidth() * scale),
                                (int) (rect.getHeight() * scale)
                            );
                        }
                        g2d.dispose();
                    }

                    return SwingFXUtils.toFXImage(bim, null);
                }
            }
        };

        renderTask.setOnSucceeded(e -> {
            pdfImageView.setImage(renderTask.getValue());
            pageLabel.setText(String.format("Page: %d / %d", pageIndex + 1, totalPages));
        });

        new Thread(renderTask).start();
    }

    @FXML 
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            renderPage(currentPage);
        }
    }

    @FXML 
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            renderPage(currentPage);
        }
    }

    @FXML private void handleFitToWidth() {
        if (currentFile == null) return;

        try (PDDocument document = Loader.loadPDF(currentFile)) {
            // Get the dimensions of the current page
            var page = document.getPage(currentPage);
            float pageWidthPoints = page.getMediaBox().getWidth();

            // Get the width of the ScrollPane (the visible area)
            // We subtract a little (20-30px) to account for the vertical scrollbar
            double availableWidth = scrollableArea.getWidth() - 30;

            // Formula: (Available Pixels / Page Points) * 72 DPI
            float targetDpi = (float) (availableWidth / pageWidthPoints) * 72;

            // Safety caps so we don't go too small or too large
            if (targetDpi < 50) targetDpi = 50;
            if (targetDpi > 300) targetDpi = 300;

            // Update the slider (this will trigger our listener and re-render)
            zoomSlider.setValue(targetDpi);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}