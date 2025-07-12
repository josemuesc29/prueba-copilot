package migration.algolia;

import com.algolia.search.*;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import migration.algolia.models.AlgoliaApiRequest;
import migration.algolia.models.ProductSearchReq;
import migration.algolia.models.ProductSearchResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AlgoliaManagerHandler implements AlgoliaManager {

    private static final Logger LOG = Logger.getLogger(APIAlgolia.class.getName());

    private RestTemplate client;

    public AlgoliaManagerHandler() {
        client = new RestTemplate();
    }


    @Override
    public  List<ItemAlgolia> getItemListAlgoliaFromStringList(List<String> objectIds) {
        try {
            AlgoliaApiRequest request = new AlgoliaApiRequest();
            request.setRequests(generateSearchRequest(objectIds));
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Algolia-Application-Id", "VCOJEYD2PO");
            headers.add("X-Algolia-API-Key", "e6f5ccbcdea95ff5ccb6fda5e92eb25c");
            HttpEntity<AlgoliaApiRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<ProductSearchResponse> response = client.postForEntity("https://vcojeyd2po-dsn.algolia.net/1/indexes/*/objects", entity, ProductSearchResponse.class);
//            LOG.info(""+ response.getBody().getResults().size());
            return response.getBody().getResults();
        } catch (Exception e) {
            LOG.info("Error con algolia api");
        }
        return new ArrayList<>();
    }

    private List<ProductSearchReq> generateSearchRequest(List<String> objectIds) {
        return objectIds.stream().map(id -> new ProductSearchReq(id)).collect(Collectors.toList());
    }

}
