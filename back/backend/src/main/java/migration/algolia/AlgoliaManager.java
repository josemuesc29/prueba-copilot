package migration.algolia;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;

import java.util.List;

public interface AlgoliaManager {
    List<ItemAlgolia> getItemListAlgoliaFromStringList(List<String> objectIds);
}
