package PDF_Ext.classes;

import java.io.File;

public class SearchResult {
    private final File file;
    private final int pageNumber;

    public SearchResult(File file, int pageNumber) {
        this.file = file;
        this.pageNumber = pageNumber;
    }

    // Getters
    public File getFile() { return file; }
    public int getPageNumber() { return pageNumber; }
    
    @Override
    public String toString() {
        return String.format("%s (Page %d)", file.getName(), pageNumber + 1);
    }
}