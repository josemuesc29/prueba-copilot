package migration.algolia.infrastructure.services;

public interface AlgoliaRecommendManager {

    String getFavoriteDepartmentByLabel(String userToken, String label);

}
