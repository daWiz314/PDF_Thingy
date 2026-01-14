package PDF_Ext.controllers;

import java.io.File;
import java.io.IOException;

import PDF_Ext.classes.ListFile;
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


/**
 * Main controller for the application, managing the overall layout and interactions between components
 */
public class Index_Controller {

    @FXML private BorderPane MainLayoutBP;                          // So we can set the standards

    // Sub-Controllers ------------------------------------------------------------------------
    @FXML private MenuBarController MENU_BARController;             // Sub-Controller for Menu Bar
    @FXML private ListViewSrcDir_Controller LIST_VIEWController;    // Sub-Controller for List View
    @FXML private Results_Controller RESULTSController;             // Sub-Controller for Results View

    private PreviewWindow_Controller activePreviewController;       // Sub-Controller for Preview Window
    // UI Components ---------------------------------------------------------------------------
    @FXML private TextField searchTerm;

    @FXML private ProgressBar myProgressBar;
    @FXML private ProgressBar overallProgress;
    @FXML private Label statusLabel;
    @FXML private Label overallStatusLabel;
    @FXML private Button searchSubmit;
    @FXML private Button searchStop;
    // Methods -------------------------------------------------------------------------------

    /** Initialize the main controller and set up initial configurations 
     * <p>
     * This method sets the maximum size for the main layout, configures the menu bar for Mac OS systems, and sets up callback functions for directory selection and preview window management.
    */
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
        // Grab submitSearch button for enabling/disabling during search
        LIST_VIEWController.searchSubmit = searchSubmit;
        searchSubmit.setDisable(true);
        searchStop.setDisable(true);

        // Set up call back function for matches
        // Side note for myself, didn't realize I could put the type in the lambda like this
        PDF_Tool.Main.call_back_function_for_matches = (ListFile file) -> { RESULTSController.updateResults(file);};

        // Set up call back function to open preview window
        LIST_VIEWController.PreviewWindowCallBack = (ListFile _lambda_file) -> {
            openPreviewWindow(_lambda_file);
            };
        RESULTSController.PreviewWindowCallBack = (ListFile file) -> {
            openPreviewWindow(file);
        };
    }
    /**
     * Handle the search submission event
     * It sets the {@code PDF_Ext.controllers.Index_Controller} progress bars and status labels to the corresponding UI components, updates the search query, and attempts to start the search process.
     * @param event - The action event triggered by the search submission (UNUSED)
     */
    @FXML private void submitSearch(ActionEvent event) {
        PDF_Tool.Main.myProgressBar = myProgressBar;
        PDF_Tool.Main.overallProgress = overallProgress;
        PDF_Tool.Main.statusLabel = statusLabel;
        PDF_Tool.Main.overallStatusLabel = overallStatusLabel;
        PDF_Tool.Main.searchStop = searchStop;

        PDF_Tool.Main.updateSearchQuery(searchTerm.getText());
        PDF_Tool.Main.attemptSearch();
    }

    /**
     * Handle the stop search event, not a whole lot else to say here
     */
    @FXML private void handleStop() {
        PDF_Tool.Main.stop();
    }

    /**
     * Open the preview window without a specific file
     */
    public void openPreviewWindow() {
        System.out.println("Opening preview window with no file.");
        openPreviewWindow(null);
    }

    /**
     * Open the preview window for a specific ListFile
     * @param LFile - The ListFile to preview, can be null
     */
    public void openPreviewWindow(ListFile LFile) {
        if (LFile != null) {
            System.out.println("Opening preview window for file: " + LFile.getFile().getAbsolutePath());
        } else {
            System.out.println("Opening preview window with no file.");
        }
        if (activePreviewController != null) {
            LIST_VIEWController.PreviewWindowCallBack = (_lambda_file) -> {
                activePreviewController.setPdfFile(_lambda_file.getFile());
            };
            RESULTSController.PreviewWindowCallBack = (_lambda_LFile) -> {
                activePreviewController.setHighlightTerm(searchTerm.getText());
                activePreviewController.setPdfFile(_lambda_LFile.getFile(), _lambda_LFile.getPageNumber());
            };
            return; // Preview window already open
        } else {
            System.out.println("No active preview window, creating a new one.");
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
            stage.setOnHidden(e -> {
                activePreviewController.clearAssets();
                activePreviewController = null;
                System.out.println("Preview window closed, reference cleared.");

                // Restore safe callbacks so double-clicks or selections will re-open the preview
                LIST_VIEWController.PreviewWindowCallBack = (file) -> { openPreviewWindow(file); };
                RESULTSController.PreviewWindowCallBack = (lf) -> { openPreviewWindow(lf); };
            });
            stage.show();
            // Show the file if we have one
            if (LFile != null && activePreviewController != null) {
                activePreviewController.setHighlightTerm(searchTerm.getText());
                if (LFile.getPageNumber() >= 0) {
                    activePreviewController.setPdfFile(LFile.getFile(), LFile.getPageNumber());
                } else {
                    activePreviewController.setPdfFile(LFile.getFile());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LIST_VIEWController.PreviewWindowCallBack = (file) -> {
            if (activePreviewController == null) {
                openPreviewWindow(file);
            } else {
                activePreviewController.setPdfFile(file.getFile());
            }
        };

        RESULTSController.PreviewWindowCallBack = (_lambda_LFile) -> {
            if (activePreviewController == null) {
                openPreviewWindow(_lambda_LFile);
            } else {
                activePreviewController.setHighlightTerm(searchTerm.getText());
                activePreviewController.setPdfFile(_lambda_LFile.getFile(), _lambda_LFile.getPageNumber());
            }
        };
    }
}
