package migration.algolia.domain.services;


import com.imaginamos.farmatodo.model.algolia.AlgoliaPersonalizationResponse;
import migration.algolia.infrastructure.services.AlgoliaRecommendManager;

import java.util.*;
import java.util.stream.Collectors;

public class AlgoliaRecommendManagerHandler implements AlgoliaRecommendManager {

    private AlgoliaConnector algoliaConnector;

    public AlgoliaRecommendManagerHandler() {
        algoliaConnector = new AlgoliaConnector();
    }

    private List<String> getFavoriteDepartment(String userTokenAnalytics) {
        List<String> response = new ArrayList<>();
        Optional<AlgoliaPersonalizationResponse> recommendationPreferencesByToken = algoliaConnector.getRecommendationPreferencesByToken(userTokenAnalytics);
        if(recommendationPreferencesByToken.isPresent()) {
            HashMap<String,Integer> departments = recommendationPreferencesByToken.get().getScores().getDepartments();
            response = orderFavoriteDepartments(departments);
            Collections.reverse(response);
            response = response.stream().limit(2).collect(Collectors.toList());
        }
        return response;
    }

    private List<String> orderFavoriteDepartments(HashMap<String,Integer> departmentsMap) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(departmentsMap.entrySet());
        list.sort(Map.Entry.comparingByValue());
        return list.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }



    @Override
    public String getFavoriteDepartmentByLabel(String userToken, String label) {
        try {
            List<String> favoriteDepartments = getFavoriteDepartment(userToken);
            if(favoriteDepartments.size() == 1 && (favoriteDepartments.get(0).contains("Belleza") || favoriteDepartments.get(0).contains("Salud"))) {
                return label;
            }
            if(favoriteDepartments.size() == 2 && (favoriteDepartments.get(0).contains("Belleza") || favoriteDepartments.get(1).contains("Belleza"))) {
                favoriteDepartments.remove("Belleza");
                favoriteDepartments.add(0,"Belleza");
            }
            return label.contains("Belleza") ? favoriteDepartments.get(0) : favoriteDepartments.get(1);
        } catch (Exception e) {
            return label;
        }

    }







}
