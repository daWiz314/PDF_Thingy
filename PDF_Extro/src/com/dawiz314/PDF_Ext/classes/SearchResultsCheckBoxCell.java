package PDF_Ext.classes;

import PDF_Ext.classes.SearchResult;

import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;

// Should have just inherited and created a decent base class, instead of copying FileCheckBoxCell but using it for search results

public class SearchResultsCheckBoxCell extends ListCell<SearchResult> {
    private final CheckBox checkBox = new CheckBox();
    private ObservableSet<SearchResult> checkedResults;

    public SearchResultsCheckBoxCell(ObservableSet<SearchResult> checkedResults) {
        this.checkedResults = checkedResults;
        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) checkedResults.add(getItem());
            else checkedResults.remove(getItem());
        });
    }

    @Override protected void updateItem(SearchResult result, boolean empty) {
        super.updateItem(result, empty);
        if (empty || result == null) {
            setGraphic(null);
            setText(null);
        } else {
            checkBox.setSelected(checkedResults.contains(result));
            setGraphic(checkBox);
            setText(result.toString());
        }
    }
}
