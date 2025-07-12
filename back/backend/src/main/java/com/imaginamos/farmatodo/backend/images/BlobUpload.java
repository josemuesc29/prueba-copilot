package com.imaginamos.farmatodo.backend.images;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.*;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.util.Constants;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Imaginamos on 12/30/14.
 */

@MultipartConfig
public class BlobUpload extends HttpServlet {
  BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  private static final Logger log = Logger.getLogger(BlobUpload.class.getName());
  private Authenticate authenticate;

  /**
   * @param req
   * @param resp
   * @throws IOException
   * @throws InvalidParameterException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, InvalidParameterException {
//    String token = req.getParameter("token");
//    String tokenIdWebSafe = req.getParameter("tokenIdWebSafe");
//    log.info("token 1 -> " + tokenIdWebSafe);
//    authenticate = new Authenticate();
//
//    try {
//      if (!authenticate.isValidToken(token, tokenIdWebSafe))
//        throw new ConflictException(Constants.INVALID_TOKEN);
//    } catch (ConflictException e) {
//      e.printStackTrace();
//    } catch (BadRequestException e) {
//      e.printStackTrace();
//    }


    List<BlobKey> blobs = blobstoreService.getUploads(req).get("photo");
    BlobKey blobKey = blobs.get(0);

    String flip = req.getParameter("flip");
    String lucky = req.getParameter("lucky");
    String rotate = req.getParameter("rotate");
//    String test = ("apiKey = " + token + "-- idApiKey=" + tokenIdWebSafe + "rotate = " + rotate + " -- flip =  " + flip + " -- lucky =  " + lucky + " ---- parametros enviados");

    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    Image oldImage = ImagesServiceFactory.makeImageFromBlob(blobKey);


    Collection<Transform> transformCollection = new ArrayList<>();
    if (flip != null) {
      if (flip.equals("horizontally")) {
//        log.info("entro a flip - 1");
        Transform transformFlip = ImagesServiceFactory.makeHorizontalFlip();
        transformCollection.add(transformFlip);
      } else if (flip.equals("vertically")) {
//        log.info("entro a flip - 1");
        Transform transformFlip = ImagesServiceFactory.makeVerticalFlip();
        transformCollection.add(transformFlip);
      } else
        throw new InvalidParameterException(flip + " is invalid parameter");

//      log.info("entro a flyp - 2");
    }

    if (lucky != null) {
      if (lucky.equals("1")) {
//        log.info("entro a lucky - 1");
        Transform transformLucky = ImagesServiceFactory.makeImFeelingLucky();
        transformCollection.add(transformLucky);
      }
//      log.info("entro a lucky - 2");
    }
    if (rotate != null) {
      int degrees;
      switch (rotate) {
        case "rotate_90":
          degrees = 90;
          break;
        case "rotate_270":
          degrees = 270;
          break;
        case "rotate_180":
          degrees = 180;
          break;
        default:
          throw new InvalidParameterException(rotate + " is invalid parameter");
      }
//      log.info("entro a rotate - 1");
      Transform transformRotate = ImagesServiceFactory.makeRotate(degrees);
      transformCollection.add(transformRotate);
//      log.info("entro a rotate - 2");
    }

    if (transformCollection.size() > 0) {
//      log.info("entro a transformacion");
      CompositeTransform compositeTransform = ImagesServiceFactory.makeCompositeTransform(transformCollection);
      imagesService.applyTransform(compositeTransform, oldImage);
//      log.info("ejecuto la transformacion");
    }

    ServingUrlOptions servingOptions = ServingUrlOptions.Builder.withBlobKey(blobKey);
    servingOptions.secureUrl(true);
    String servingUrl = imagesService.getServingUrl(servingOptions);

    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("application/json");
    resp.addHeader("Access-Control-Allow-Origin", "*");
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("servingUrl", servingUrl);
    jsonObject.addProperty("blobKey", blobKey.getKeyString());
//    jsonObject.addProperty("test", test);
    PrintWriter out = resp.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();
  }
}
