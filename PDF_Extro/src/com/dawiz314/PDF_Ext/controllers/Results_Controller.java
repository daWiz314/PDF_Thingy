package PDF_Ext.controllers;
import PDF_Ext.classes.SearchResult;
import PDF_Ext.classes.SearchResultsCheckBoxCell;
import PDF_Ext.classes.PdfPageExtractor;


import java.util.HashSet;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;

public class Results_Controller {
    @FXML private Label resultCountLabel;       // "Selected 0 / 0 Matches"
    @FXML private Button buttonExportSelected;  // Export Button
    @FXML private Button buttonDeleteSelected;  // Delete Button

    @FXML private ListView<SearchResult> listViewResults; // Actual list view to display results
    public ObservableList<SearchResult> results = FXCollections.observableArrayList(); // List that when changes, updates ListView 
    private final ObservableSet<SearchResult> checkedResults = FXCollections.observableSet(new HashSet<>()); // Container for the check boxes in the list view

    public Consumer<SearchResult> PreviewWindowCallBack;

    public void initialize() {
        listViewResults.setItems(results);

        listViewResults.setCellFactory(lv -> new SearchResultsCheckBoxCell(checkedResults));

        checkedResults.addListener((SetChangeListener<SearchResult>) change -> {
            int count = checkedResults.size();
            int total = results.size();

            resultCountLabel.setText(String.format("Selected %d / %d Matches", count, total));

            buttonExportSelected.setDisable(count == 0);
            buttonDeleteSelected.setDisable(count == 0);

        });

        listViewResults.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                safeNotifyPreview(newVal);
            }
        });

        listViewResults.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                SearchResult selected = listViewResults.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    safeNotifyPreview(selected);
                }
            }
        });
    }

private void safeNotifyPreview(SearchResult result) {
    if (PreviewWindowCallBack != null) {
        try {
            PreviewWindowCallBack.accept(result);
        } catch (Exception e) {
            System.err.println("Preview callback failed: " + e.getMessage());
            // This happens if the window was JUST closed and the reference is mid-cleanup
        }
    }
}


    @FXML private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
    
    // File saveFile = fileChooser.showSaveDialog(pdfImageView.getScene().getWindow());
    
    // if (saveFile != null) {
    //     saveDataToFile(saveFile);
    // }
    }

    @FXML private void handleDelete(ActionEvent event) {
        results.removeAll(checkedResults);
        checkedResults.clear();
        listViewResults.refresh();
    }

    @FXML private void handleResultsCheckAll(ActionEvent event) {
        checkedResults.addAll(results);
        listViewResults.refresh();
    }

    @FXML private void handleResultsUncheckAll(ActionEvent event) {
        checkedResults.clear();
        listViewResults.refresh();
    }

    public void updateResults(SearchResult result) {
        results.add(result);
        listViewResults.setItems(results);
    }

    // private void saveDataToFile(File file) {
    //     try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
    //         writer.println("File Name, Page, Content");
    //         writer.println(currentFile.getName() + "," + (currentPage + 1) + ", Confirmed Data");
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
    
}
