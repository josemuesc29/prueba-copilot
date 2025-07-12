package com.farmatodo.backend.task;

import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.categories.Category;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.categories.SubCategory;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemMostSales;
import com.imaginamos.farmatodo.model.product.Suggested;
import com.imaginamos.farmatodo.model.store.Store;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by Admin on 30/05/2017.
 */

public class MostSalesByCategory extends HttpServlet {
  private static final Logger log = Logger.getLogger(MostSalesByCategory.class.getName());
  private List<ItemMostSales> itemMostSales = new ArrayList<>();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    final String METHOD = "[MostSalesByCategory.doPost]";
    log.info("[INI]-"+METHOD);

    List<Item> itemList;
    List<Item> items;
    List<Suggested> itemsId;
    List<Long> subcategoryIds;

    log.info("Consultando Items mas vendidos del Datastore para ELIMINAR...");
    List<ItemMostSales> mostSalesToDelete = ofy().load().type(ItemMostSales.class).list();
    ofy().delete().entities(mostSalesToDelete).now();
    log.info("Items mas vendidos ELIMINADOS.");

    log.info("Consultando departamentos en el Datastore...");
    List<Department> departmentList = ofy().load().type(Department.class).list();

    log.info("FOR (Department department : departmentList)");
    for (Department department : departmentList) {
      itemsId = new ArrayList<>();
      items = new ArrayList<>();
      subcategoryIds = new ArrayList<>();
      List<Category> categoryList = ofy().load().type(Category.class).ancestor(department).list();
      for (Category category : categoryList) {
        List<SubCategory> subCategoryList = ofy().load().type(SubCategory.class).ancestor(category).list();
        for (SubCategory subCategory : subCategoryList) {
          subcategoryIds.add(subCategory.getId());
        }
      }

      Query.Filter filterFilter;

      for (Long integer : subcategoryIds) {
        filterFilter = new Query.FilterPredicate("subCategories", Query.FilterOperator.EQUAL, integer);
        itemList = ofy().load().type(Item.class).filter(filterFilter).order("sales").list();
        items.addAll(itemList);


      }
      Collections.sort(items, Collections.reverseOrder());
      if (!items.isEmpty()) {
        for (int i = 0; i < 10; i++) {
          Item item = items.get(i);
          Suggested suggested = new Suggested();
          suggested.setItem(item.getId());
          itemsId.add(suggested);
        }

        ItemMostSales itemMostSale = saveMostSales(itemsId, Key.create(Department.class, department.getIdClassification()));
        this.itemMostSales.add(itemMostSale);
      }
    }
    log.info("Guardando los nuevos items mas vendidos en el Datastore...");
    ofy().save().entities(this.itemMostSales).now();

    log.info("[FIN]-"+METHOD);
  }

  private ItemMostSales saveMostSales(List<Suggested> itemId, Key<Department> departmentKey) {
    final String METHOD = "[MostSalesByCategory.saveMostSales]";
    log.info("[INI]-"+METHOD);

    ItemMostSales itemMostSales = new ItemMostSales();
    itemMostSales.setItemMostSalesId(UUID.randomUUID().toString());
    itemMostSales.setDepartmentRef(Ref.create(departmentKey));
    itemMostSales.setSuggested(itemId);

    log.info("[FIN]-"+METHOD);

    return itemMostSales;
  }


}
