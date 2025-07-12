package com.imaginamos.farmatodo.backend.images;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.BlobAttributes;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.HttpStatusCode;
import okhttp3.*;

import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import javax.inject.Named;
import java.io.IOException;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by Imaginamos on 12/31/14.
 */
@Api(name = "blobEndpoint", version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    apiKeyRequired = AnnotationBoolean.TRUE,
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "")

public class BlobEndpoint {
  private static final Logger log = Logger.getLogger(BlobUpload.class.getName());

  private Authenticate authenticate;

  public BlobEndpoint() {
    authenticate = new Authenticate();
  }

  /**
   * @return
   * @throws UnauthorizedException
   * @throws ConflictException
   */
  @ApiMethod(name = "getBlobURL")
  public BlobAttributes getBlobURL(@Named("token") final String token, @Named("tokenIdWebSafe") final String tokenIdWebSafe)
      throws UnauthorizedException, ConflictException, BadRequestException {
    //If if is not null, then check if it exists. If yes, throw an Exception
    //that it is already present

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String blobUploadUrl = blobstoreService.createUploadUrl("/blobupload");
    BlobAttributes ba = new BlobAttributes();
    ba.setBlobURL(blobUploadUrl);
    //log.warning(blobUploadUrl);
    return ba;
  }

  /**
   * Api para cargar una imagen al storgae de google y asociarlo a un item.
   * Request:
   *
   * {
   *   "fileName": "miImagen.png",
   *   "imageBase64": "dgfdfgfdgfdgfdfdggfgsddgsdfgfdgsdfg1fg4fdgf",
   *   "item": 1234567
   * }
   *
   * Response:
   *
   * {
   *  "item": 1234567,
   *  "confirmation": true,
   *  "status": "OK",
   *  "statusCode": 200,
   *  "message": "Image successfully uploaded."
   * }
   *
   * */
  @ApiMethod(name = "uploadImage",  path = "/blobEndpoint/uploadImage", httpMethod = ApiMethod.HttpMethod.POST)
  public AnswerUploadImage uploadImage(final UploadImageRequest request) {
    try {
      if (request == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Request is null.");

      if (request.getItem() == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Item is null.");

      if (request.getItem() == 0)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Item can not be zero.");

      if (request.getItem() < 1)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Item must be greater than zero.");

      // TODO Se elimina la validación de que el item exista en Data Store
      //if (!itemExists(request.getItem()))
      //  return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Item with id [" + request.getItem() + "] does not exist.");

      //log.warning("Item to load image -> " + request.getItem());

      if (request.getImageBase64() == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Image in base64 is null.");

      if (request.getImageBase64().isEmpty())
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Image in base64 is empty.");

      if (!request.getImageBase64().matches(Constants.PATTERN_FOR_VALIDATE_BASE64))
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Image in base64 is not a valid base64");

      if (request.getFileName() == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "FileName is null.");

      if (request.getFileName().isEmpty())
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "FileName is empty.");

      final String fileName = request.getFileName();
      final Set<String> allowedExtensions = Set.of("jpg", "jpeg", "png", "gif", "avif", "webp");

      String extension = Optional.ofNullable(fileName)
              .filter(name -> name.contains("."))
              .map(name -> name.substring(name.lastIndexOf('.') + 1).toLowerCase())
              .orElse("");

      if (!allowedExtensions.contains(extension))
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "FileName extension must be [png,jpg,jpeg,gif,avif,webp]");

      AnswerUploadImage answerUploadImage = uploadAndGetImageUrl(request);
      if (answerUploadImage.getStatusCode() == 200) {
//        log.info("save url to item");
        return saveUrlInItemAlgolia(answerUploadImage, false);
      }
      return answerUploadImage;

    } catch (Exception e) {
      return new AnswerUploadImage(0, null, false, "Internal Server Error", 500, "Unexpected error. Message:" + e.getMessage());
    }
  }

  @ApiMethod(name = "replaceImage",  path = "/blobEndpoint/replaceImage", httpMethod = ApiMethod.HttpMethod.POST)
  public AnswerUploadImage replaceImage(final UploadImageRequest request) {
    try {
      if (request == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Request is null.");

      if (request.getItem() == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Item is null.");

      if (request.getItem() == 0)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Item can not be zero.");

      if (request.getItem() < 1)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Item must be greater than zero.");

//      log.info("Item to load image -> " + request.getItem());

      if (request.getImageBase64() == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Image in base64 is null.");

      if (request.getImageBase64().isEmpty())
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Image in base64 is empty.");

      if (!request.getImageBase64().matches(Constants.PATTERN_FOR_VALIDATE_BASE64))
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "Image in base64 is not a valid base64");

      if (request.getFileName() == null)
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "FileName is null.");

      if (request.getFileName().isEmpty())
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "FileName is empty.");

      final String fileName = request.getFileName();
      final Set<String> allowedExtensions = Set.of("jpg", "jpeg", "png", "gif", "avif", "webp");

      String extension = Optional.ofNullable(fileName)
              .filter(name -> name.contains("."))
              .map(name -> name.substring(name.lastIndexOf('.') + 1).toLowerCase())
              .orElse("");

      if (!allowedExtensions.contains(extension))
        return new AnswerUploadImage(0, null, false, "Bad Request", 400, "FileName extension must be [png,jpg,jpeg,gif,avif,webp]");
      return uploadAndGetImageUrl(request);
    } catch (Exception e) {
      return new AnswerUploadImage(0, null, false, "Internal Server Error", 500, "Unexpected error. Message:" + e.getMessage());
    }
  }

  /**
   * Valida si el item existe en el datastore.
   * */
  private Boolean itemExists(final Integer itemId){
    if(itemId==null)
      return false;

    Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
    Item item = ofy().load().key(Key.create(itemGroupKey, Item.class, Long.toString(itemId))).now();

    return item != null;
  }


    private AnswerUploadImage saveUrlInItemAlgolia(final AnswerUploadImage answerUploadImage, final boolean updateItem) {
        try {
            if (answerUploadImage.getItem() == null)
                return new AnswerUploadImage(0, null, 0, false, HttpStatusCode.BAD_REQUEST.getStatusName(), 400, "ItemId null.", answerUploadImage.getPrincipal());
            // validar si el item existe el item
            if(updateItem) {
              boolean itemExists = APIAlgolia.existsInAlgolia(answerUploadImage.getItem());
              if (itemExists) {
                boolean res = APIAlgolia.addImageItemAlgolia(String.valueOf(answerUploadImage.getItem()), answerUploadImage.getUrlImage(), answerUploadImage.getPrincipal());
                if (!res) {
                  return new AnswerUploadImage(0, null, 0, false, HttpStatusCode.NOT_FOUND.getStatusName(), 404, "Quantity from item is not valid", answerUploadImage.getPrincipal());
                }
              } else {
                return new AnswerUploadImage(0, null, 0, false, HttpStatusCode.NOT_FOUND.getStatusName(), 404, "Item not found.", answerUploadImage.getPrincipal());
              }
            }
        } catch (Exception e) {
            log.warning("Error -> " + e);
            return new AnswerUploadImage(0, null, 0, false, HttpStatusCode.NOT_FOUND.getStatusName(), 404, e.getMessage(), answerUploadImage.getPrincipal());
        }
        return new AnswerUploadImage(answerUploadImage.getItem(), answerUploadImage.getUrlImage(), answerUploadImage.getPosition(), true, HttpStatusCode.OK.getStatusName(), 200, "Image successfully uploaded into item.", answerUploadImage.getPrincipal());
    }

  /**
   * Carga imagen al google storage y genera la url de imagen.
   * */
  private AnswerUploadImage uploadAndGetImageUrl(final UploadImageRequest clientRequest){
    try {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

      if(Objects.isNull(blobstoreService)) {
        return new AnswerUploadImage(0, null, false, "Service Unavailable", 503, "Google BlobStoreService Unavailable");
      }
      final String blobUploadUrl = blobstoreService.createUploadUrl("/blobupload");

      if (blobUploadUrl.isEmpty() || blobUploadUrl.isBlank()) {
        return new AnswerUploadImage(0, null, false, "Service Unavailable", 503, "Google BlobUploadService Unavailable");
      }
      OkHttpClient client = new OkHttpClient();

      MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

      builder.addFormDataPart(Constants.IMAGES_API_KEY_NAME, Constants.IMAGES_API_KEY);
      builder.addFormDataPart(Constants.IMAGES_ID_API_KEY_NAME, Constants.IMAGES_ID_API_KEY);

      byte[] decodeBytes = null;

      // Decoding base64 image
      try {
        decodeBytes = Base64.getDecoder().decode(clientRequest.getImageBase64());

      } catch (Exception e) {
        return new AnswerUploadImage(0, null, false, "Conflict", 409, "Error to decode Base64. Message:" + e.getMessage());
      }

      if (Objects.isNull(decodeBytes)) {
        return new AnswerUploadImage(0, null, false, "Conflict", 409, "Base64 decoding resulted in null. Check your base64.");
      }
      RequestBody requestBody = RequestBody.create(
              MediaType.parse("application/octet-stream"),
              decodeBytes
            );
      builder.addFormDataPart(
              Constants.IMAGES_PHOTO_NAME,
              clientRequest.getFileName(),
              requestBody
      );

      MultipartBody multipartBody = builder.build();

      Request request = new Request.Builder()
              .url(blobUploadUrl)
              .post(multipartBody)
              .build();

      try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          return new AnswerUploadImage(0, null, false, "Service Unavailable", 503, "Google /blobupload service Unavailable.");
        }

        String responseString = response.body().string();
        JSONObject jObject = new JSONObject(responseString);
        Object servingUrl = jObject.get(Constants.IMAGES_SERVING_URL);

        if (Objects.isNull(servingUrl)) {
          return new AnswerUploadImage(0, null, false, "Conflict", 409, "Google /blobupload service returned a null url for your image.");
        }
        final String finalServingUrl = String.valueOf(servingUrl);

        return new AnswerUploadImage(clientRequest.getItem(), finalServingUrl, clientRequest.getPosition() == null ? 0 : clientRequest.getPosition(),
                true, "OK", 200, "Image successfully uploaded.", clientRequest.getPrincipal());

      } catch (IOException e) {
        return new AnswerUploadImage(0, null, false, "Internal Server Error", 500, "Unexpected error. Message:" + e.getMessage());
      }

    } catch (Exception e) {
      return new AnswerUploadImage(0, null, false, "Internal Server Error", 500, "Unexpected error. Message:" + e.getMessage());
    }
  }
    @ApiMethod(name = "deleteImages", path = "/blobEndpoint/deleteImages", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteImages(@Named("productId") final String productId) {
        if (productId == null || productId.isEmpty()) {
            return new Answer(false);
        }
        boolean result = APIAlgolia.deleteImagesItemAlgolia(productId);
        return new Answer(result);
    }


}
