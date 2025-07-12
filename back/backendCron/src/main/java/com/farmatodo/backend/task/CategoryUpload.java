package com.farmatodo.backend.task;

import com.google.appengine.api.datastore.Query;
//import com.google.appengine.repackaged.org.codehaus.jackson.map.type.TypeFactory;
//import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.categories.*;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;


/**
 * Created by mileniopc on 12/13/16.
 * Property of Imaginamos.
 */

public class CategoryUpload extends HttpServlet {
  private static final Logger log = Logger.getLogger(CategoryUpload.class.getName());

  /**
   * Brings the categories of the database of "Farmatodo"
   *
   * @param request  Object of class "HttpServletRequest"
   * @param response Object of class "HttpServletResponse"
   * @throws IOException
   * @throws InvalidParameterException
   * @throws ServletException
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[CategoryUpload.doPost]";
    log.info("[INI]-"+METHOD);


    JsonObject jsonObject = new JsonObject();
    List<Department> departmentList = ApiGatewayService.get().getCategoryActive();
    if(Objects.nonNull(departmentList) && !departmentList.isEmpty()) {
      this.saveCategoriesLevel1(departmentList);
      response.setStatus(HttpServletResponse.SC_OK);
      jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
      jsonObject.addProperty("Code", HttpServletResponse.SC_OK);
    }else {
      jsonObject.addProperty("Message", URLConnections.DEFAULT);
      jsonObject.addProperty("Code", HttpServletResponse.SC_CONFLICT);
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }


    /*

    log.info("Inicia conexion a URL : ["+URLConnections.URL_CATEGORIES+"]");
    URL url = new URL(URLConnections.URL_CATEGORIES);
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod(URLConnections.GET);
    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
    httpURLConnection.setReadTimeout(150000);

    response.setContentType(URLConnections.CONTENT_TYPE_JSON);
    response.addHeader("Access-Control-Allow-Origin", "*");


    int responseCode = httpURLConnection.getResponseCode();

    log.info("ResponseCode : ["+responseCode+"]");

    switch (responseCode) {
      case 200:
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder responseJson = new StringBuilder();

        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();

        ObjectMapper objectMapper = new ObjectMapper();
        List<Department> departmentList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Department.class));
        //List<Department> departmentList = ApiBackend30Service.get().getCategoryActive();
        this.saveCategoriesLevel1(departmentList);
        response.setStatus(HttpServletResponse.SC_OK);
        jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
        jsonObject.addProperty("Code", responseCode);
        break;
      case 204:
        jsonObject.addProperty("Message", URLConnections.NO_CONTENT);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        break;
      case 400:
        jsonObject.addProperty("Message", URLConnections.BAD_REQUEST);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        break;
      case 500:
        jsonObject.addProperty("Message", URLConnections.SERVER_ERROR);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        jsonObject.addProperty("Message", URLConnections.DEFAULT);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        break;
    }
    */

    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();
    log.info("[FIN]-"+METHOD);
  }

  private String searchClassification(Long idClassification, String classificationLevel) {
    final String METHOD = "[CategoryUpload.searchClassification]";
    log.info("[INI]-"+METHOD);

    Query.Filter filterId = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, idClassification);

    log.info("SWITCH (classificationLevel) : SWITCH("+classificationLevel+")");

    switch (classificationLevel) {
      case "department":
        Department department = ofy().load().type(Department.class).filter(filterId).first().now();
        if (department == null) {
          return UUID.randomUUID().toString();
        } else {
          return department.getIdClassification();
        }
      case "category":
        Category category = ofy().load().type(Category.class).filter(filterId).first().now();
        if (category == null) {
          return UUID.randomUUID().toString();
        } else {
          return category.getIdClassification();
        }
      case "subcategory":
        SubCategory subcategory = ofy().load().type(SubCategory.class).filter(filterId).first().now();
        if (subcategory == null) {
          return UUID.randomUUID().toString();
        } else {
          return subcategory.getIdClassification();
        }
      case "filtername":
        FilterName filterName = ofy().load().type(FilterName.class).filter(filterId).first().now();
        if (filterName == null) {
          return UUID.randomUUID().toString();
        } else {
          return filterName.getIdClassification();
        }

      case "filter":
        Filter filter = ofy().load().type(Filter.class).filter(filterId).first().now();
        if (filter == null) {
          return UUID.randomUUID().toString();
        } else {
          return filter.getIdClassification();
        }
      default:
        return null;

    }
  }

  private void saveCategoriesLevel1(List<Department> departmentList) {
    final String METHOD = "[CategoryUpload.saveCategoriesLevel1]";
    log.info("[INI]-"+METHOD);
    log.info("departmentList.size() : ["+departmentList.size()+"]");

    log.info("FOR (Department department : departmentList)");
    for (Department department : departmentList) {
      department.setIdClassification(this.searchClassification(department.getId(), "department"));
      Key<Department> classificationLevel1Key = ofy().save().entity(department).now();
      if (department.getChildren() != null) {
        this.saveCategoriesLevel2(classificationLevel1Key, department.getChildren());
      } else if (department.getFilters() != null) {
        this.saveFilterName(department.getId(), department.getFilters());
      }
    }
    log.info("[FIN]-"+METHOD);
  }

  private void saveCategoriesLevel2(Key<Department> classificationLevel1Key, List<Category> categoryList) {
    final String METHOD = "[CategoryUpload.saveCategoriesLevel2]";
    log.info("[INI]-"+METHOD);
    log.info("categoryList.size() : ["+categoryList.size()+"]");

    log.info("FOR (Category category : categoryList)");
    for (Category category : categoryList) {
      category.setIdClassification(this.searchClassification(category.getId(), "category"));
      category.setIdClassificationLevel1(Ref.create(classificationLevel1Key));
      Key<Category> classificationLevel2Key = ofy().save().entity(category).now();
      if (category.getChildren() != null) {
        this.saveCategoriesLevel3(classificationLevel2Key, category.getChildren());
      } else if (category.getFilters() != null) {
        this.saveFilterName(category.getId(), category.getFilters());
      }
    }
    log.info("[FIN]-"+METHOD);
  }

  private void saveCategoriesLevel3(Key<Category> classificationLevel2Key, List<SubCategory> subCategoryList) {
    final String METHOD = "[CategoryUpload.saveCategoriesLevel3]";
    log.info("[INI]-"+METHOD);
    log.info("subCategoryList.size() : ["+subCategoryList.size()+"]");

    log.info("FOR (SubCategory subCategory : subCategoryList)");
    for (SubCategory subCategory : subCategoryList) {
      subCategory.setIdClassification(this.searchClassification(subCategory.getId(), "subcategory"));
      subCategory.setIdClassificationLevel2(Ref.create(classificationLevel2Key));
      Key<SubCategory> classificationLevel3Key = ofy().save().entity(subCategory).now();
      if (subCategory.getFilters() != null) {
        this.saveFilterName(subCategory.getId(), subCategory.getFilters());
      }
    }
    log.info("[FIN]-"+METHOD);
  }

  private void saveFilterName(Long idCategory, List<FilterName> classificationLevel4List) {
    final String METHOD = "[CategoryUpload.saveFilterName]";
    log.info("[INI]-"+METHOD);
    log.info("classificationLevel4List.size() : [" + classificationLevel4List.size() +"]");

    log.info("FOR (FilterName classificationLevel4 : classificationLevel4List)");
    for (FilterName classificationLevel4 : classificationLevel4List) {
      classificationLevel4.setIdClassification(this.searchClassification(classificationLevel4.getId(), "filtername"));
      classificationLevel4.setIdCategory(idCategory);
      Key<FilterName> classificationLevel4Key = ofy().save().entity(classificationLevel4).now();
      if (classificationLevel4.getValues() != null) {
        this.saveFilter(classificationLevel4Key, classificationLevel4.getValues());
      }
    }
    log.info("[FIN]-"+METHOD);
  }

  private void saveFilter(Key<FilterName> classificationLevel4Key, List<Filter> classificationLevel5List) {
    final String METHOD = "[CategoryUpload.saveFilter]";
    log.info("[INI]-"+METHOD);
    log.info("classificationLevel5List.size() : ["+classificationLevel5List.size()+"]");

    log.info("FOR (Filter classificationLevel5 : classificationLevel5List)");
    for (Filter classificationLevel5 : classificationLevel5List) {
      classificationLevel5.setIdClassification(this.searchClassification(classificationLevel5.getId(), "filter"));
      classificationLevel5.setIdFilterName(Ref.create(classificationLevel4Key));
      ofy().save().entity(classificationLevel5).now();
    }
    log.info("[FIN]-"+METHOD);
  }
}
