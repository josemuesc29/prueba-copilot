package migration.algolia.models;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;

import java.util.List;

public class ProductSearchResponse {

    private List<ItemAlgolia> results;

    public List<ItemAlgolia> getResults() {
        return results;
    }

    public void setResults(List<ItemAlgolia> results) {
        this.results = results;
    }
}
