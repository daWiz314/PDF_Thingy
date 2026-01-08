package PDF_Ext.controllers;

import PDF_Ext.classes.FileCheckBoxCell;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class ListViewSrcDir_Controller {
    @FXML private ListView<File> listViewSrcDir;
    ObservableList<File> srcDirContents = FXCollections.observableArrayList();

    private final ObservableSet<File> checkedFiles = FXCollections.observableSet(new HashSet<>());
    @FXML private Label selectionCountLabel;
    public Button startSearchButton;
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

        checkedFiles.addListener((SetChangeListener<File>) change -> {
            PDF_Tool.Main.updateSelectedFile(checkedFiles);
        });

        listViewSrcDir.setCellFactory(lv -> new FileCheckBoxCell(checkedFiles));

        selectionCountLabel.setText("Select a directory to get started!");

        checkedFiles.addListener((SetChangeListener<File>) change -> {
            int count = checkedFiles.size();
            int total = srcDirContents.size();
            
            selectionCountLabel.setText(String.format("Selected: %d / %d files", count, total));
            
            startSearchButton.setDisable(count == 0);
        });
    }

    public static String getFileExtension(String filename) {
        if (filename == null) return null;
            int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public void updateSrcDir(File dir) {
        srcDirContents.clear(); // Clean slate for displaying the data
        checkedFiles.clear();   // Clean slate if the user has done multiple actions
        listViewSrcDir.setItems(srcDirContents);

        for (File item : dir.listFiles()) {
            if ("pdf".equalsIgnoreCase(getFileExtension(item.getName()))) {
                srcDirContents.add(item);
            }
            continue;
        }

        srcDirContents.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        listViewSrcDir.setItems(srcDirContents);
    }

    @FXML public void handleCheckAll() {
        checkedFiles.addAll(srcDirContents);
        listViewSrcDir.refresh();
    }

    @FXML public void handleUncheckAll() {
        checkedFiles.clear();
        listViewSrcDir.refresh();
    }

    @FXML public void selectedFile(MouseEvent event) {
        System.out.println("Clicked! Setting picture to: " + listViewSrcDir.getSelectionModel().getSelectedItem());
        PreviewWindowCallBack.accept(listViewSrcDir.getSelectionModel().getSelectedItem());
    }

}

