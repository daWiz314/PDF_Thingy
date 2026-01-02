package PDF_Ext.classes;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextLocationFinder extends PDFTextStripper {
    private String searchTerm;
    private List<Rectangle2D> regions = new ArrayList<>();

    public TextLocationFinder(String searchTerm) throws IOException {
        this.searchTerm = searchTerm.toLowerCase();
    }

    public List<Rectangle2D> getRegions() { return regions; }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        String content = string.toLowerCase();
        if (content.contains(searchTerm)) {
            // Get the position of the first character of the match
            TextPosition first = textPositions.get(0);
            TextPosition last = textPositions.get(textPositions.size() - 1);
            
            // Create a rectangle covering the text
            regions.add(new Rectangle2D.Float(
                first.getXDirAdj(), 
                first.getYDirAdj() - first.getHeightDir(), 
                last.getXDirAdj() + last.getWidthDirAdj() - first.getXDirAdj(), 
                first.getHeightDir() + 5 // extra padding
            ));
        }
    }
}