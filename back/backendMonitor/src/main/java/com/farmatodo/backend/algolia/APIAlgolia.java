package com.farmatodo.backend.algolia;

import com.algolia.search.APIClient;
import com.algolia.search.AppEngineAPIClientBuilder;
import com.algolia.search.Index;
import com.algolia.search.exceptions.AlgoliaException;
import com.algolia.search.iterators.IndexIterable;
import com.algolia.search.objects.Query;
import com.algolia.search.responses.SearchResult;
import com.google.common.collect.Lists;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.home.HomeConfigAlgolia;
import com.imaginamos.farmatodo.model.product.ItemUpdateHighlightSuggested;
import com.imaginamos.farmatodo.model.product.ProductSortGroup;
import com.imaginamos.farmatodo.model.product.RequestUpdateHighlightSuggested;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//import com.algolia.search.ApacheAPIClientBuilder;

/**
 * Created by JPuentes on 30/10/2018.
 */
public class APIAlgolia {

    private static final Logger LOG = Logger.getLogger(APIAlgolia.class.getName());
    public static APIClient algoliaClient;

    static{
        algoliaClient = new AppEngineAPIClientBuilder(URLConnections.ALGOLIA_APP_ID,URLConnections.ALGOLIA_API_KEY).build();
    }

    private static Boolean isTodayHoliday(Date date){
        Boolean isTodayHoliday = false;
        try {
            List<Holiday> result;
            Holiday holiday = null;

            Index<Holiday> index = algoliaClient.initIndex(URLConnections.ALGOLIA_HOLIDAYS, Holiday.class);
            //LOG.warning("(index!=null)=>"+(index!=null));

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy.MM.dd");
            int year = cal.get(Calendar.YEAR);

            //LOG.warning("(format1.format(cal.getTime()))=>"+format1.format(cal.getTime()));
            Query query = new Query().setFilters("objectID:" + format1.format(cal.getTime()));
            SearchResult<Holiday> searchResult = index.search(query);

            //LOG.warning("(searchResult!=null))=>"+(searchResult!=null));
            if(searchResult!=null) {

                result = searchResult.getHits();

                //LOG.warning("(result!=null)=>" + (result != null));
                if (result != null) {
                    for(Holiday h : result){
                        if(h.getObjectID().equals(format1.format(cal.getTime()))){
                            isTodayHoliday = true;
                            break;
                        }
                    }
                }
            }
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.isTodayHoliday. Message:"+ e.getMessage());
        }
        return isTodayHoliday;
    }


    public static ActiveCourierSocket getActiveCourierTrackingSocket(){
        try {
            Index<ActiveCourierSocket> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,ActiveCourierSocket.class);

            return index.getObject(URLConnections.ALGOLIA_COURIER_SOCKET_ACTIVE).get();

        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public static WebSocketProperties getHttpsWebSocketUrl(){
        try {
            Index<WebSocketProperties> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,WebSocketProperties.class);
            return index.getObject(URLConnections.ALGOLIA_HTTPS_WEBSOCKET_URL).get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getHttpsWebSocketUrl. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }


    public static WebSocketProperties getHttpWebSocketUrl(){
        try {
            Index<WebSocketProperties> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,WebSocketProperties.class);
            return index.getObject(URLConnections.ALGOLIA_HTTP_WEBSOCKET_URL).get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getHttpWebSocketUrl. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }


}
