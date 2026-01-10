package PDF_Ext.classes;

import java.io.File;
import java.util.Set;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;

/**
 * Simple ListCell that shows a CheckBox and stores selection in a plain Set<File>.
 * Calls a Runnable callback when the selection changes so controllers can react.
 */
public class FileCheckBoxCellSimple extends ListCell<File> {
    private final CheckBox checkBox = new CheckBox();
    private final Set<File> checkedFiles;
    private final Runnable onChange;

    public FileCheckBoxCellSimple(Set<File> checkedFiles, Runnable onChange) {
        this.checkedFiles = checkedFiles;
        this.onChange = onChange;

        checkBox.setOnAction(e -> {
            File item = getItem();
            if (item != null) {
                if (checkBox.isSelected()) checkedFiles.add(item);
                else checkedFiles.remove(item);
                if (onChange != null) onChange.run();
            }
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
