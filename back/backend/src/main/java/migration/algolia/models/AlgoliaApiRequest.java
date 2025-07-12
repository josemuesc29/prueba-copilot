package migration.algolia.models;

import java.util.List;

public class AlgoliaApiRequest {
    private List<ProductSearchReq> requests;

    public List<ProductSearchReq> getRequests() {
        return requests;
    }

    public void setRequests(List<ProductSearchReq> requests) {
        this.requests = requests;
    }
}
