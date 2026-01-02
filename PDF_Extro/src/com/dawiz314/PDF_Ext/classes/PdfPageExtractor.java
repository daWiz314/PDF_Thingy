package PDF_Ext.classes;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;

public class PdfPageExtractor {

    /**
     * Extracts a specific page from a source PDF and saves it to a destination.
     *
     * @param sourceFile      The original PDF file.
     * @param destinationFile The file where the single page PDF will be saved.
     * @param pageNumber      The page number to extract (1-based index).
     * @throws IOException    If file reading/writing fails.
     */
    public void extractPage(File sourceFile, File destinationFile, int pageNumber) throws IOException {
        try (PDDocument sourceDocument = Loader.loadPDF(sourceFile)) {
            
            // Validate page number
            int totalPages = sourceDocument.getNumberOfPages();
            if (pageNumber < 1 || pageNumber > totalPages) {
                throw new IllegalArgumentException("Invalid page number. Document has " + totalPages + " pages.");
            }

            // Create a new document for the extracted page
            try (PDDocument newDocument = new PDDocument()) {
                PDPage page = sourceDocument.getPages().get(pageNumber - 1);
                
                newDocument.addPage(page);

                newDocument.save(destinationFile);
            }
        }
    }
}
