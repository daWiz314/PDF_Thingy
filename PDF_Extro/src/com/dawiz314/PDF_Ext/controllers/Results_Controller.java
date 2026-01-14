package PDF_Ext.controllers;

import PDF_Ext.classes.ListFile;
import PDF_Ext.classes.PDFPageExtractor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

public class Results_Controller {
    // Actual UI Components --------------------------------------------------------------------
    @FXML private Label resultCountLabel;                                           // "Selected 0 / 0 Matches"
    @FXML private Button buttonExportSelected;                                      // Export Button
    @FXML private Button buttonDeleteSelected;                                      // Delete Button

    @FXML private ListView<ListFile> listViewResults;                               // Actual list view to display results

    // Variables -------------------------------------------------------------------------------
    public ObservableList<ListFile> results = FXCollections.observableArrayList();  // List that when changes, updates ListView 
    private final Set<ListFile> checkedFiles = new HashSet<>();                     // Plain set to track checked files
    public Consumer<ListFile> PreviewWindowCallBack;                                // Callback to open preview window  

    // Methods ---------------------------------------------------------------------------------
    public void initialize() {
        // Set up ListView to show results -----------------------------------------------------
        listViewResults.setItems(results); // Set the items to the results list, which should be nothing atm

        // Set up cell factory to use checkboxes
        listViewResults.setCellFactory(CheckBoxListCell.forListView(new Callback<ListFile, ObservableValue<Boolean>>() {
            @Override public ObservableValue<Boolean> call(ListFile item) {
                return item == null ? null : item.selectedProperty();
            }
        }));

        // Set up checked items tracking
        listViewResults.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null && PreviewWindowCallBack != null) {
                PreviewWindowCallBack.accept(newFile);
            }
        });
        // Commented out for now, as it double fires with the selectedItemProperty event
        
        // Send file to preview on double-click
        // listViewResults.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        //     if (newVal != null) {
        //         safeNotifyPreview(newVal);
        //     }
        // });

        listViewResults.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                ListFile selected = listViewResults.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    safeNotifyPreview(selected);
                }
            }
        });
    }

private void onCheckedFilesChanged() {
    int count = (int) results.stream().filter(ListFile::isSelected).count();
    int total = results.size();
    resultCountLabel.setText(String.format("Selected: %d / %d Matches", count, total));
    buttonExportSelected.setDisable(count == 0);
    buttonDeleteSelected.setDisable(count == 0);
}

private void safeNotifyPreview(ListFile _lambda_LFile) {
    if (PreviewWindowCallBack != null) {
        try {
            PreviewWindowCallBack.accept(_lambda_LFile);
        } catch (Exception e) {
            System.err.println("Preview callback failed: " + e.getMessage());
            // This happens if the window was JUST closed and the reference is mid-cleanup
        }
    }
}


    @FXML private void handleExport() {
        DirectoryChooser DC = new DirectoryChooser();
        DC.setTitle("Save Results");
        File saveDir = DC.showDialog(resultCountLabel.getScene().getWindow());
        System.out.println(saveDir.getAbsolutePath());
    }

    @FXML private void handleDelete(ActionEvent event) {
        results.removeAll(results.filtered(file -> checkedFiles.contains(file)));
        checkedFiles.clear();
        listViewResults.refresh();
        onCheckedFilesChanged();
    }

    @FXML private void handleResultsCheckAll(ActionEvent event) {
        checkedFiles.addAll(results);
        listViewResults.refresh();
        onCheckedFilesChanged();
    }

    @FXML private void handleResultsUncheckAll(ActionEvent event) {
        checkedFiles.clear();
        listViewResults.refresh();
        onCheckedFilesChanged();
    }

    public void updateResults(ListFile result) {
        results.add(result);
        listViewResults.setItems(results);
    }
    
}
