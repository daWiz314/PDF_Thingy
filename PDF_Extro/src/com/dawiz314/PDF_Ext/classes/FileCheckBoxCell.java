package PDF_Ext.classes;

import java.io.File;

import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;

public class FileCheckBoxCell extends ListCell<File> {
    private final CheckBox checkBox = new CheckBox();
    private final ObservableSet<File> checkedFiles;
    
    public FileCheckBoxCell(ObservableSet<File> checkedFiles) {
        this.checkedFiles = checkedFiles;
        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) checkedFiles.add(getItem());
            else checkedFiles.remove(getItem());
        });
    }

    @Override protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);
        if (empty || file == null) {
            setGraphic(null);
            setText(null);
        } else {
            checkBox.setSelected(checkedFiles.contains(file));
            setGraphic(checkBox);
            setText(file.getName());
        }
    }
}