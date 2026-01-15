package PDF_Ext.controllers;

import PDF_Ext.classes.ListFile;
import PDF_Ext.classes.PDFPageExtractor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
        listViewResults.setCellFactory(new Callback<ListView<ListFile>, ListCell<ListFile>>() {
            @Override
            public ListCell<ListFile> call(ListView<ListFile> lv) {
                return new CheckBoxListCell<ListFile>(item -> item == null ? null : item.selectedProperty()) {
                    @Override
                    public void updateItem(ListFile item, boolean empty) {
                        super.updateItem(item, empty);
                        if (getGraphic() instanceof CheckBox) {
                            CheckBox cb = (CheckBox) getGraphic();
                            cb.setFocusTraversable(false);
                        }
                    }
                };
            }
        });

        // Set up checked items tracking
        // listViewResults.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
        //     if (newFile != null && PreviewWindowCallBack != null) {
        //         PreviewWindowCallBack.accept(newFile);
        //     }
        // });

        // Attach listeners so checkbox changes update counts immediately
        for (ListFile lf : results) {
            lf.selectedProperty().addListener((obs, oldV, newV) -> onCheckedFilesChanged());
        }
        results.addListener((ListChangeListener<ListFile>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (ListFile lf : change.getAddedSubList()) {
                        lf.selectedProperty().addListener((obs, oldV, newV) -> onCheckedFilesChanged());
                    }
                }
            }
            onCheckedFilesChanged();
        });

        // Handle mouse clicks: ignore checkbox-originating clicks, support single & double click preview
        listViewResults.setOnMouseClicked(event -> {
            javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
            while (target != null && target != listViewResults) {
                if (target instanceof CheckBox) return;
                target = target.getParent();
            }

            if (event.getClickCount() == 2 && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                ListFile selected = listViewResults.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                safeNotifyPreview(selected);
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
