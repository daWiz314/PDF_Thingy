package PDF_Ext.controllers;

import PDF_Ext.classes.FileCheckBoxCell;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class ListViewSrcDir_Controller {
    // Actual UI Components -------------------------------------------------------------------
    @FXML private ListView<File> listViewSrcDir; // ListView to show files in source directory
    @FXML private Label selectionCountLabel; // Label to show count of selected files

    public Button searchSubmit; // Button to start search - passed from Index_Controller
    // Variables -------------------------------------------------------------------------------
    ObservableList<File> srcDirContents = FXCollections.observableArrayList(); // Contains the items that actually go in the ListView

    private final Set<File> checkedFiles = new HashSet<>(); // Plain set to track checked files

    public Consumer<File> PreviewWindowCallBack;

    public void initialize() {

        listViewSrcDir.setItems(srcDirContents);

        listViewSrcDir.setOnMouseMoved(event -> {
        if (event.getX() > listViewSrcDir.getWidth() - 20 && event.getY() > listViewSrcDir.getHeight() - 20) {
            listViewSrcDir.setCursor(Cursor.NW_RESIZE); // Diagonal resize arrow
        } else {
            listViewSrcDir.setCursor(Cursor.DEFAULT);  // Normal pointer
        }
    });

        listViewSrcDir.setOnMouseDragged(event -> {
        // If the user is dragging near the bottom-right corner
        if (event.getX() > listViewSrcDir.getWidth() - 20 && event.getY() > listViewSrcDir.getHeight() - 20) {
            listViewSrcDir.setCursor(Cursor.CLOSED_HAND);
            listViewSrcDir.setPrefWidth(event.getX());
            listViewSrcDir.setPrefHeight(event.getY());
        }
    });

        listViewSrcDir.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null) {
                System.out.println("Selected file: " + newFile.getName());
                if (PreviewWindowCallBack != null) {
                    PreviewWindowCallBack.accept(newFile);
                }
            }
        });

        // Use the simple (non-observable) cell and notify on changes
        listViewSrcDir.setCellFactory(lv -> new PDF_Ext.classes.FileCheckBoxCellSimple(checkedFiles, this::onCheckedFilesChanged));

        selectionCountLabel.setText("Select a directory to get started!");
    }

    // Called by cells when their check state changes
    private void onCheckedFilesChanged() {
        int count = checkedFiles.size();
        int total = srcDirContents.size();
        selectionCountLabel.setText(String.format("Selected: %d / %d files", count, total));
        if (searchSubmit != null) searchSubmit.setDisable(count == 0);
        PDF_Tool.Main.updateSelectedFile(checkedFiles);
    }

    public static String getFileExtension(String filename) {
        if (filename == null) return null;
            int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Update the source directory listing
     * <p>
     * This will refresh the ListView to show all PDF files in the given directory
     * @param dir - The new source directory
     */
    public void updateSrcDir(File dir) {
        srcDirContents.clear();             // Clean slate for displaying the data
        listViewSrcDir.getItems().clear();  // Clear existing items in the ListView

         // Get just PDFs
        for (File item : dir.listFiles()) {
            if ("pdf".equalsIgnoreCase(getFileExtension(item.getName()))) {
                srcDirContents.add(item);
            }
            continue;
        }
        // Clear any prior checks when a new directory is selected
        checkedFiles.clear();

        // Sort the results by filename (case-insensitive)
        srcDirContents.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        listViewSrcDir.setItems(srcDirContents);
        listViewSrcDir.refresh();
        onCheckedFilesChanged();
    }

    // public void updateSrcDir(File dir) {
    //     srcDirContents.clear(); // Clean slate for displaying the data
    //     checkedFiles.clear();   // Clean slate if the user has done multiple actions
    //     listViewSrcDir.setItems(srcDirContents);

    //     for (File item : dir.listFiles()) {
    //         if ("pdf".equalsIgnoreCase(getFileExtension(item.getName()))) {
    //             srcDirContents.add(item);
    //         }
    //         continue;
    //     }

    //     srcDirContents.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
    //     listViewSrcDir.setItems(srcDirContents);
    // }

    @FXML public void handleCheckAll() {
        checkedFiles.addAll(srcDirContents);
        listViewSrcDir.refresh();
        onCheckedFilesChanged();
    }

    @FXML public void handleUncheckAll() {
        checkedFiles.clear();
        listViewSrcDir.refresh();
        onCheckedFilesChanged();
    }

    @FXML public void selectedFile(MouseEvent event) {
        System.out.println("Clicked! Setting picture to: " + listViewSrcDir.getSelectionModel().getSelectedItem());
        PreviewWindowCallBack.accept(listViewSrcDir.getSelectionModel().getSelectedItem());
    }

}

