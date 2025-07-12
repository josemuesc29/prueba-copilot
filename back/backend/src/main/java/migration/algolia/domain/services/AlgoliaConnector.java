package migration.algolia.domain.services;

import com.imaginamos.farmatodo.model.algolia.AlgoliaPersonalizationResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.logging.Logger;

public class AlgoliaConnector {

    private static final Logger log = Logger.getLogger(AlgoliaConnector.class.getName());


    private RestTemplate restTemplate;

    public AlgoliaConnector() {
        restTemplate = new RestTemplate();
    }

    public Optional<AlgoliaPersonalizationResponse> getRecommendationPreferencesByToken(String token) {

        String urlBase = "https://personalization.us.algolia.com/1/profiles/personalization/{userToken}";
        urlBase = urlBase.replace("{userToken}",token);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Algolia-Api-Key", "527503b7bd1f7b9acbcdaff382aaf937");
        headers.set("X-Algolia-Application-ID", "VCOJEYD2PO");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<AlgoliaPersonalizationResponse> response = restTemplate.exchange(
                    urlBase, HttpMethod.GET, requestEntity, AlgoliaPersonalizationResponse.class);
            if(response.getStatusCode().is2xxSuccessful()) {
                return Optional.of(response.getBody());
            }
        } catch (Exception e) {
            log.info("No se pudo consultar recomendados");
        }
        return Optional.empty();
    }

}
