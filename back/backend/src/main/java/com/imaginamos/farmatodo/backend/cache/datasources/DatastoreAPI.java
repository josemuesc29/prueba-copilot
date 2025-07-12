package com.imaginamos.farmatodo.backend.cache.datasources;

import com.google.appengine.api.datastore.Query;
import com.imaginamos.farmatodo.model.algolia.CategorySeo;
import com.imaginamos.farmatodo.model.categories.Category;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.categories.SubCategory;
import com.imaginamos.farmatodo.model.cms.CategoryPhoto;
import com.imaginamos.farmatodo.model.copyright.Copyright;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.util.CollectionResponseModel;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class DatastoreAPI {

    private static final Logger LOG = Logger.getLogger(DatastoreAPI.class.getName());

    /**
     * Consultar todas las ciudades.
     * */
    public static List<City> findAllSortedCities() {
        List<City> cityList = ofy().load().type(City.class).list();
        return cityList.stream().sorted(Comparator.comparing(City::getName)).collect(Collectors.toList());
    }

    public static List<Copyright> findCopyRight(final String token, final String tokenIdWebSafe, final String deliveryType, final Boolean provider) {

        Query.Filter filterActive = new Query.FilterPredicate("active", Query.FilterOperator.EQUAL, true);
        List<Copyright> copyrights = ofy().load().type(Copyright.class).filter("deliveryType", deliveryType).filter(filterActive).order("id").list();
        if(provider) {
            Query.Filter filterProvider = new Query.FilterPredicate("provider", Query.FilterOperator.EQUAL, provider);
            List<Copyright> providers = ofy().load().type(Copyright.class).filter(filterProvider).filter(filterActive).order("id").list();
            if(Objects.nonNull(providers) && !providers.isEmpty()) {
                copyrights.addAll(providers);
            }
        }
        return copyrights;
    }

    public static CollectionResponseModel getCategoriesAndSubcategories() {

        List<Department> departmentList = ofy().load().type(Department.class).order("priority").list();
        List<String> ids = departmentList.stream()
                .map(Department::getId)
                .filter(id -> id > 0)
                .map(Object::toString)
                .collect(Collectors.toList());

        for (int i = 0; i < departmentList.size(); i++) {
            Department department = departmentList.get(i);
            List<CategoryPhoto> categoryPhotos = getCategoryPhotoByDepartment(department.getId());
            List<Category> categoryList = ofy().load().type(Category.class).ancestor(department).order("priority").list();
            categoryList = categoryPhotos(categoryList, categoryPhotos);
            department.setChildren(categoryList);
            ids = Stream.concat(ids.stream(), categoryList.stream()
                    .map(Category::getId)
                    .filter(id -> id > 0)
                    .map(Object::toString))
                    .collect(Collectors.toList());

            for (int j = 0; j< categoryList.size(); j++) {
                Category category = categoryList.get(j);
                List<SubCategory> subCategoryList = ofy().load().type(SubCategory.class).ancestor(category).order("priority").list();
                category.setChildren(subCategoryList);
                ids = Stream.concat(ids.stream(), subCategoryList.stream()
                        .map(SubCategory::getId)
                        .filter(id -> id > 0)
                        .map(Object::toString))
                        .collect(Collectors.toList());
            }
        }
        // LOG.info("ALL IDS ==> " + ids.toString());
        CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
        // SEO
        departmentList = getCategoriesSeoData(departmentList, ids);
        collectionResponseModel.setDepartmentList(departmentList);
        collectionResponseModel.setTimeStamp(new Date().getTime());
        return collectionResponseModel;
    }

    private static List<CategoryPhoto> getCategoryPhotoByDepartment(Long idDepartment){
        return Collections.emptyList();
    }

    private static List<Category> categoryPhotos(List<Category> categoryList, List<CategoryPhoto> categoryPhotos) {
        for (int i = 0; i < categoryPhotos.size(); i++){
            Category category = new Category();
            category.setIdClassification(UUID.randomUUID().toString());
            category.setImage(true);
            category.setCategoryPhoto(categoryPhotos.get(i));
            categoryList.add(category);
        }
        return categoryList;
    }

    private static List<Department> getCategoriesSeoData(List<Department> departments, List<String> ids){
        List<CategorySeo> categorySEOList = APIAlgolia.getCategoriesSeo(ids);

        if (categorySEOList != null && !categorySEOList.isEmpty()) {
            return departments.stream().peek(department -> {
                department.setChildren(department.getChildren()
                                .stream()
                                .peek(category -> {

                    category.setChildren(category
                            .getChildren()
                            .stream()
                            .peek(subCategory -> subCategory
                                    .setTextoSEO(getTextoSeo(subCategory.getId(), categorySEOList))).collect(Collectors.toList()));
                    category.setTextoSEO(getTextoSeo(category.getId(), categorySEOList));
                }).collect(Collectors.toList())
                );
                department.setTextoSEO(getTextoSeo(department.getId(), categorySEOList));
            }).collect(Collectors.toList());
        }
        return departments;
    }

    private static String getTextoSeo(Long id, List<CategorySeo> categorySEOList) {
        String text = "";
        List<CategorySeo> categorySeo = categorySEOList
                .stream()
                .filter(cat -> cat.getObjectID().equals(String.valueOf(id)))
                .collect(Collectors.toList());
        if (!categorySeo.isEmpty() && categorySeo.get(0) != null) {
            text = categorySeo.get(0).getTextoSEO();
            //LOG.info("FOUND TEXTO SEO FROM ALGOLIA ID: " + id + " TEXT: " + text);
        }
        return text;
    }

    public static List<DeliveryOrderItem> getDeliveryOrderItemsByDeliveryOrder(DeliveryOrder deliveryOrder) {
        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load()
                .type(DeliveryOrderItem.class)
                .ancestor(deliveryOrder)
                .list();
        return deliveryOrderItemList;
    }
}
