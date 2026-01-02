package PDF_Ext.controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Index_Controller {

    @FXML private BorderPane MainLayoutBP; // So we can set the standards

    @FXML private MenuBarController MENU_BARController;             // Sub-Controller for Menu Bar
    @FXML private ListViewSrcDir_Controller LIST_VIEWController;    // Sub-Controller for List View
    @FXML private Results_Controller RESULTSController;             // Sub-Controller for Results View

    private PreviewWindow_Controller activePreviewController;       // Sub-Controller for Preview Window

    @FXML private TextField searchTerm;

    @FXML private ProgressBar myProgressBar;
    @FXML private ProgressBar overallProgress;
    @FXML private Label statusLabel;
    @FXML private Label overallStatusLabel;
    @FXML private Button searchSubmit;
    @FXML private Button searchStop;

    public void initialize() {
        MainLayoutBP.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // If we are on Mac, use the system Menu Bar
        final String OS = System.getProperty("os.name");
        if (OS != null && OS.startsWith("Mac")) {
            if (MENU_BARController != null) {
                MENU_BARController.set_menu_bar(true);
            }
        }

        // Set up call back function for menubar selectSrcDir
        MENU_BARController.set_call_back_function_src_dir((dir) -> LIST_VIEWController.updateSrcDir(dir));
        MENU_BARController.openPreviewWindow = () -> {openPreviewWindow();};
        LIST_VIEWController.startSearchButton = searchSubmit;
        searchSubmit.setDisable(true);
        searchStop.setDisable(true);

        PDF_Tool.Main.call_back_function_for_matches = (result) -> { RESULTSController.updateResults(result);};

        RESULTSController.PreviewWindowCallBack = (result) -> {
            openPreviewWindow();
        };
    }

    @FXML private void submitSearch(ActionEvent event) {
        PDF_Tool.Main.myProgressBar = myProgressBar;
        PDF_Tool.Main.overallProgress = overallProgress;
        PDF_Tool.Main.statusLabel = statusLabel;
        PDF_Tool.Main.overallStatusLabel = overallStatusLabel;
        PDF_Tool.Main.searchStop = searchStop;


        PDF_Tool.Main.updateSearchQuery(searchTerm.getText());
        PDF_Tool.Main.attemptSearch();
    }

    @FXML private void handleStop() {
        PDF_Tool.Main.stop();
    }

    public void openPreviewWindow() {
        if (activePreviewController != null) {
            LIST_VIEWController.PreviewWindowCallBack = (file) -> {
                activePreviewController.setPdfFile(file);
            };
            RESULTSController.PreviewWindowCallBack = (result) -> {
                activePreviewController.setHighlightTerm(searchTerm.getText());
                activePreviewController.setPdfFile(result.getFile(), result.getPageNumber());
            };
            return;
        }
       java.net.URL fxmlLocation = getClass().getResource("/PDF_Ext/views/PreviewWindow.fxml");
    
        if (fxmlLocation == null) {
            System.err.println("ERROR: Could not find PreviewWindow.fxml! Check your file path.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            loader.setClassLoader(getClass().getClassLoader());
            Parent root = loader.load();
            activePreviewController = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("PDF Preview");
            stage.setScene(new Scene(root));
            stage.setAlwaysOnTop(true);
            // stage.setOnHidden(e -> {
            //     activePreviewController = null;
            //     System.out.println("Preview window closed, reference cleared.");
            // });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LIST_VIEWController.PreviewWindowCallBack = (file) -> {activePreviewController.setPdfFile(file);};
        RESULTSController.PreviewWindowCallBack = (result) -> {
            if (activePreviewController == null) {
                openPreviewWindow();
            }

            activePreviewController.setHighlightTerm(searchTerm.getText());
            activePreviewController.setPdfFile(result.getFile(), result.getPageNumber());
        };
    }
}
