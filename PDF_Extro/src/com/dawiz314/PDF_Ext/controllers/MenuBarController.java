package PDF_Ext.controllers;

import java.io.File;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;

/**
 * A Controller for the Manu Bar
 * <p>
 * Methods:
 * <p>
 * <pre>
 * {@link #initialize()}
 * {@link #set_menu_bar(boolean)}
 * {@link #quit(ActionEvent)}
 * </pre>
 * @author Harry F. Martin III
 * 
 */
public class MenuBarController {

    @FXML private MenuBar menuBar;
    @FXML private MenuItem menuButton_Quit;
    @FXML private MenuItem menuButton_SelectSrcDir;

    private Consumer<File> call_back_function;
    public Runnable openPreviewWindow;

    public void initialize() {
        
        KeyCombination quitCombo = new KeyCodeCombination(
            KeyCode.W,
            KeyCombination.ALT_DOWN
        );
        menuButton_Quit.setAccelerator(quitCombo);

        Platform.runLater(() -> {
            if (menuBar.getScene() != null) {
                menuBar.getScene().getAccelerators().put(quitCombo, () -> {
                    quit(new ActionEvent());
                });
            }
        });

        menuButton_SelectSrcDir.setDisable(true); // Disable till we get the update that tells us what to do with the data
    }
    /**
     * Sets the menu bar to actually use the menu bar of OSX
     * @param useSystemMenuBarProperty A Boolean that sets it to the appropriate value
     */
    public void set_menu_bar(boolean useSystemMenuBarProperty) {
        menuBar.useSystemMenuBarProperty().set(useSystemMenuBarProperty);
    }

    public void set_call_back_function_src_dir(Consumer<File> call_back) {
        call_back_function = call_back;
        menuButton_SelectSrcDir.setDisable(false);
    }

    /** 
     * Quits the program and outputs the source of the quit
     * <p>
     * Exposed to FXML
     * @param event The actual {@code event} that fired the method
     */
    @FXML private void quit(ActionEvent event) {
        System.out.println("\n\n");
        System.out.printf("Exit called via: %s", event);
        System.out.println("\n\n");
        Platform.exit();
    }

    /**
     * Opens the dialog box, allowing the user to select the source directory for the PDFs to read/scan
     * <p>
     * Exposed to FXML
     * @param event Unused
     */
    @FXML private void selectSrcDir(ActionEvent event) {
        DirectoryChooser DC = new DirectoryChooser();
        File dir = DC.showDialog(menuBar.getScene().getWindow());
        System.out.println(dir);

        call_back_function.accept(dir);
    }

    @FXML private void openPreviewWindow(ActionEvent event) {
        openPreviewWindow.run();
    }
}
