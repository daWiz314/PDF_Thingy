package PDF_Ext.classes;

import java.io.File;

import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;


/**
 * Class to represent a file in the ListView, optionally with page number and result status
 */
public class ListFile{

    private File file;
    private final int pageNumber;
    private final Boolean result;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    /**
     * Constructor for ListFile without page number and result
     * @param file - The file to represent
     */
    public ListFile(File file) {
        this.file = file;
        this.pageNumber = -1;
        this.result = false;
    }

    /**
     * Constructor for ListFile with page number and result
     * @param file - The file to represent
     * @param pageNumber - The page number associated with the file
     * @param result - The result status associated with the file, typically if calling this function, it is a match; so this will be true
     */
    public ListFile(File file, int pageNumber, Boolean result) {
        this.file = file;
        this.pageNumber = pageNumber;
        this.result = result;
    }
    // Getters -------------------------------------------------------------------------------
    public File getFile() { return file; }
    public int getPageNumber() { return pageNumber; }
    public Boolean getResult() { return result; }

    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean value) { selected.set(value); }

    // Override the default so we can display it in the ListView
    @Override public String toString() {
        if (this.result == null || this.result == false) {
            return String.format("%s", file.getName());
        } else {
            return String.format("%s (Page %d)", file.getName(), pageNumber + 1);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListFile)) return false;
        ListFile other = (ListFile) o;
        String thisPath = file == null ? null : file.getAbsolutePath();
        String otherPath = other.file == null ? null : other.file.getAbsolutePath();
        return Objects.equals(thisPath, otherPath) && this.pageNumber == other.pageNumber;
    }

    @Override public int hashCode() {
        return Objects.hash(file == null ? null : file.getAbsolutePath(), pageNumber);
    }
}