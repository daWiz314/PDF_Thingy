package PDF_Ext.classes;

import java.io.File;


/**
 * Class to represent a file in the ListView, optionally with page number and result status.
 */
public class ListFile {
    private File file;
    private final int pageNumber;
    private final Boolean result;

    public ListFile(File file) {
        this.file = file;
        this.pageNumber = -1;
        this.result = false;
    }

    public ListFile(File file, int pageNumber, Boolean result) {
        this.file = file;
        this.pageNumber = pageNumber;
        this.result = result;
    }
    // Getters -------------------------------------------------------------------------------
    /**
     * Gets the file
     * @return the file as type File
     */
    public File getFile() {
        return file;
    }
    /**
     * Gets the page number
     * @return the page number as int
     */
    public int getPageNumber() {
        return pageNumber;
    }
    /**
     * Gets the result status
     * @return the result as Boolean
     */
    public Boolean getResult() {
        return result;
    }
    // Override the default so we can display it in the ListView
    @Override public String toString() {
        if (this.result == null || this.result == false) {
            return String.format("%s", file.getName());
        } else {
            return String.format("%s (Page %d)", file.getName(), pageNumber + 1);
        }
    }
}