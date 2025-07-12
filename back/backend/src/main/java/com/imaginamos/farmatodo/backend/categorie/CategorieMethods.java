package com.imaginamos.farmatodo.backend.categorie;

import com.algolia.search.exceptions.AlgoliaException;
import com.imaginamos.farmatodo.model.categories.Category;
import com.imaginamos.farmatodo.model.categories.CategoryUpdateJson;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.model.algolia.CategoriaAlgolia;
import com.imaginamos.farmatodo.model.algolia.HitsItemsAlgolia;
import com.imaginamos.farmatodo.model.product.Item;
import static com.imaginamos.farmatodo.backend.OfyService.ofy;

import java.util.*;
import java.util.logging.Logger;

public class CategorieMethods {
    private static final Logger log = Logger.getLogger(Item.class.getName());

    public HitsItemsAlgolia getItemsByCategory(int categoryId, int idStoreGroup , int hitsPerPage, int page) throws AlgoliaException {
        return getItemsByCategory(categoryId, idStoreGroup, hitsPerPage, page, null);
    }

    public HitsItemsAlgolia getItemsByCategory(int categoryId, int idStoreGroup , int hitsPerPage, int page,
                                               Boolean subscribeAndSaveSave) throws AlgoliaException {

        CategoriaAlgolia categoriaAlgolia = APIAlgolia.getCategoryNameById(categoryId);

        //log.warning(" Categoria para el id:  " + categoryId + " ->  " + categoriaAlgolia);
        HitsItemsAlgolia hitsItemsAlgolia = APIAlgolia.getItemsBySubCategorie(categoriaAlgolia.getName(),idStoreGroup ,
                hitsPerPage, page,categoriaAlgolia.getType(), subscribeAndSaveSave);
        if (hitsItemsAlgolia != null) {
            hitsItemsAlgolia.setCategoryCode(categoriaAlgolia.getCode());
        }
        return hitsItemsAlgolia;
    }

    public HitsItemsAlgolia getAllItemsByCategory(int categoryId, int idStoreGroup, Boolean subscribeAndSaveSave) throws AlgoliaException {
        CategoriaAlgolia categoriaAlgolia = APIAlgolia.getCategoryNameById(categoryId);
        //log.warning(" Categoria para el id:  " + categoryId + " ->  " + categoriaAlgolia);
        if(Objects.nonNull(categoriaAlgolia)) {
//            HitsItemsAlgolia hitsItemsAlgolia = APIAlgolia.getAllItemsBySubCategorie(categoriaAlgolia.getName(), idStoreGroup, categoriaAlgolia.getType(), subscribeAndSaveSave);
            HitsItemsAlgolia hitsItemsAlgolia = APIAlgolia.getAllItemsBySubCategorieId(categoryId, categoriaAlgolia.getName(), idStoreGroup, categoriaAlgolia.getType(), subscribeAndSaveSave);
            if (hitsItemsAlgolia != null) {
                hitsItemsAlgolia.setCategoryCode(categoriaAlgolia.getCode());
            }
            return hitsItemsAlgolia;
        }
        return null;
    }

    public List<Item> splitAndGetPage(List<Item> data, int hitsPerPage, int page){
        //log.info("Esrtos son los items: {}"+data);
        final int totalPages = Math.round(data.size()/hitsPerPage);
        //log.info("total pages: "+totalPages+" and round: "+ data.size()/hitsPerPage);

        if (page > totalPages) {
            //log.warning("Esta pagina no existe = "+ page);
            return new ArrayList<>();
        }

        if (data.size() <= hitsPerPage){
            return data;
        }
        int index = 0;
        int countPages = 0;
        Map<Integer, List<Item>> listPage = new HashMap<>();

        for (Item item : data) {
            if ( index < hitsPerPage ){
                validateListItems(listPage,countPages,item);
                index++;
            }else{
                countPages++;
                validateListItems(listPage,countPages,item);
                index=1;
            }
        }
        return listPage.get(page);
    }

    private void validateListItems(Map<Integer, List<Item>> listPage, int countPages, Item item){
        if(listPage.isEmpty() || Objects.isNull(listPage.get(countPages))){
            listPage.put(countPages, new ArrayList<>());
        }
        listPage.get(countPages).add(item);
    }

    /**
     * Update a category and return true if it was successful
     * @param category
     * @return
     */
    public boolean updateCategory(CategoryUpdateJson category){

        try {
            Category categorySaved = ofy().load().type(Category.class).filter("id", category.getId()).first().now();

            if (category.getUrl() != null) {
                categorySaved.setUrl(category.getUrl());
            }

            if (category.getPriority() != null) {
                categorySaved.setPriority(category.getPriority());
            }

            if (category.getName() != null) {
                categorySaved.setName(category.getName());
            }

            if (category.getStatus() != null) {
                categorySaved.setStatus(category.getStatus());
            }

            ofy().save().entity(categorySaved).now();

            return true;
        } catch (Exception e){
            log.warning("error updateCategory -> " + e.getMessage() + " " + e.getLocalizedMessage());
            return false;
        }
    }

}
