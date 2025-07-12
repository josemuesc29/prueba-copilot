package com.imaginamos.farmatodo.backend.cms;

/**
 * Created by Sebastian on 13/01/2017.
 */

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.util.Constants;

import java.util.List;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;


/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "outstandingProductsEndpoint",
    version = "v1",
    apiKeyRequired = AnnotationBoolean.TRUE,
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Stores, deletes, edits and queries outstanding products for all pages.")
public class OutstandingProductsEndpoint {

  private Authenticate authenticate;

  public OutstandingProductsEndpoint() {
    authenticate = new Authenticate();
  }

  /**
   * Upgrade state outstanding of a target product
   *
   * @param token              User's token.
   * @param tokenIdWebSafe     Identification of the User's token.
   * @param idProduct          Identification of a productInformation.
   * @param outstandingProduct State outstanding.
   * @throws ConflictException
   */
  @ApiMethod(name = "updateOutstanding", path = "/outstandingProductsEndpoint/updateOutstanding", httpMethod = ApiMethod.HttpMethod.PUT)
  public void updateOutstanding(@Named("token") final String token,
                                @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                @Named("idProduct") final String idProduct,
                                @Named("outstandingProduct") final boolean outstandingProduct) throws ConflictException, BadRequestException {

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    final Key<Item> productInformationKey = Key.create(Item.class, idProduct);
    Item item = ofy().load().key(productInformationKey).now();

    if (item == null)
      throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);

    item.setOutstanding(outstandingProduct);
    ofy().save().entity(item).now();
  }

  /**
   * Get all outstanding productInformation
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @return List of Object "Item" class
   * @throws ConflictException
   */
  @ApiMethod(name = "getAllOutstandingProduct", path = "/outstandingProductsEndpoint/getOutstandingProduct", httpMethod = ApiMethod.HttpMethod.GET)
  public List<Item> getOutstandingProduct(@Named("token") final String token,
                                          @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    List<Item> productsInformation = ofy().load().type(Item.class).filter("outstanding", true).list();

    if (productsInformation.size() == 0)
      throw new ConflictException(Constants.OUTSTANDING_PRODUCT_NOT_EXISTS);

    return productsInformation;
  }

}