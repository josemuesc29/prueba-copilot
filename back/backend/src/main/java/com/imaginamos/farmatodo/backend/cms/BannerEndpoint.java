package com.imaginamos.farmatodo.backend.cms;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.cms.Banner;
import com.imaginamos.farmatodo.model.cms.CategoryPhoto;
import com.imaginamos.farmatodo.model.cms.InfoPrivacy;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 1/4/17.
 * Property of Imaginamos.
 */

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "bannerEndpoint",
    version = "v1",
    apiKeyRequired = AnnotationBoolean.TRUE,
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Stores, deletes, edits and queries banners for all pages.")
public class BannerEndpoint {
  private static final Logger log = Logger.getLogger(Customer.class.getName());
  private Authenticate authenticate;

  public BannerEndpoint() {
    authenticate = new Authenticate();
  }

  /**
   * Creation of a banner. Allowed execute to new banner with 'directionBanner' equals 'false',
   * whose value indicates that entity to create is a Slideshow Banner.
   *
   * @param token          User's token.
   * @param tokenIdWebSafe identification of the User's token.
   * @param idCategory     identification of a category. (Department - Parent)
   * @param banner         Object of class 'Banner' that contain data to store of a new banner.
   * @return Object of class 'Banner' stored.
   * @throws ConflictException
   */
  @ApiMethod(name = "createBanner", path = "/bannerEndpoint/createBanner", httpMethod = ApiMethod.HttpMethod.POST)
  public Banner createBanner(@Named("token") final String token,
                             @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                             @Nullable @Named("idCategory") final Integer idCategory,
                             final Banner banner) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);
    //log.warning("method createBanner: "+idCategory +" banner: "+banner);
    Key<Department> classificationLevel1Key = null;
    /*if (idCategory != null) {
      Department department = ofy().load().type(Department.class).filter("id", idCategory).first().now();
      if (department == null)
        throw new ConflictException(Constants.INVALID_CATEGORY);

      classificationLevel1Key = Key.create(Department.class, department.getIdClassification());

      //if(banner.isDirectionBanner())
      // throw new ConflictException(Constants.INVALID_DIRECTION_CREATE_BANNER);
    }*/
    {
      final Key<Department> finalClassificationLevel1Key = classificationLevel1Key;
      return ofy().transact(new Work<Banner>() {
        @Override
        public Banner run() {
          Banner banner1 = new Banner();
          banner1.setIdBanner(UUID.randomUUID().toString());
          banner1.setRedirectUrl(banner.getRedirectUrl());
          banner1.setRedirectId(banner.getRedirectId());
          banner1.setRedirectType(banner.getRedirectType());
          banner1.setUrlBanner(banner.getUrlBanner());
          banner1.setDirectionBanner(banner.isDirectionBanner());
          banner1.setBannerWeb(banner.getBannerWeb());
          banner1.setOrder(banner.getOrder());
          banner1.setCampaignName(banner.getCampaignName());
          banner1.setCreative(banner.getCreative());
          banner1.setPosition(banner.getPosition());
          banner1.setIdWebSafeBanner(banner1.getIdBanner());
          /*
          if (idCategory != null) {
            banner1.setClassificationLevel1Ref(Ref.create(finalClassificationLevel1Key));
            Key<Banner> bannerKey = Key.create(finalClassificationLevel1Key, Banner.class, banner1.getIdBanner());
            banner1.setIdWebSafeBanner(bannerKey.toWebSafeString());
          } else {
            Key<Banner> bannerKey = Key.create(Banner.class, banner1.getIdBanner());
            banner1.setIdWebSafeBanner(bannerKey.toWebSafeString());
          }*/
          //ofy().save().entity(banner1).now();

          boolean result = APIAlgolia.createBanner(new com.imaginamos.farmatodo.model.algolia.Banner(banner1.getIdBanner(), banner.getUrlBanner(), banner.getRedirectUrl(), banner.getRedirectId(),
                    banner.getRedirectType(), banner1.getIdBanner(), banner.getOrder(), banner.isDirectionBanner(), banner.getBannerWeb(), banner.getCampaignName(), banner.getCreative(),
                    banner.getPosition(), (Objects.nonNull(idCategory) ? Long.parseLong(idCategory.toString()) : null),
                    (Objects.nonNull(banner1.getClassificationLevel1Ref()) ? Integer.parseInt(""+banner1.getClassificationLevel1Ref().get().getId()) : 1)));
          if(result){
            return banner1;
          }else{
            return null;
          }
        }
      });
    }
  }

  /**
   * Realiza la migración de Banner de DataStore a Algolia
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   */
  @ApiMethod(name = "migrateBannerToAlgolia", path = "/bannerEndpoint/migrateBannerToAlgolia", httpMethod = ApiMethod.HttpMethod.GET)
  public List<Banner> migrateBannerToAlgolia () throws ConflictException, BadRequestException {
    List<Banner> bannerList = ofy().load().type(Banner.class).list();

    bannerList.stream().forEach(banner ->{
      //log.warning("method migrateBannerToAlgolia: "+banner);
      if(Objects.nonNull(banner.getClassificationLevel1Ref())){
        Department department = ofy().load().type(Department.class).filterKey(banner.getClassificationLevel1Ref()).first().now();
        //log.warning("Category ="+ department.getId());
        APIAlgolia.createBanner(new com.imaginamos.farmatodo.model.algolia.Banner(banner.getIdBanner(), banner.getUrlBanner(), banner.getRedirectUrl(), banner.getRedirectId(),
                banner.getRedirectType(), banner.getIdWebSafeBanner(), banner.getOrder(), banner.isDirectionBanner(), banner.getBannerWeb(), banner.getCampaignName(), banner.getCreative(),
                banner.getPosition(), department.getId(), Integer.parseInt(""+banner.getClassificationLevel1Ref().get().getId())));
      }else {
        APIAlgolia.createBanner(new com.imaginamos.farmatodo.model.algolia.Banner(banner.getIdBanner(), banner.getUrlBanner(), banner.getRedirectUrl(), banner.getRedirectId(),
                banner.getRedirectType(), banner.getIdWebSafeBanner(), banner.getOrder(), banner.isDirectionBanner(), banner.getBannerWeb(), banner.getCampaignName(), banner.getCreative(),
                banner.getPosition(), null, 1));
      }
    });
    return bannerList;
  }

  /**
   * Upgrade of banner
   *
   * @param token          User's token.
   * @param tokenIdWebSafe identification of the User's token.
   * @param banner         Object of class 'Banner' that contain data to store of a new banner.
   * @throws ConflictException
   */
  @ApiMethod(name = "updateBanner", path = "/bannerEndpoint/updateBanner", httpMethod = ApiMethod.HttpMethod.PUT)
  public Banner updateBanner(@Named("token") final String token,
                             @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                             @Nullable @Named("idCategory") final Integer idCategory,
                             final Banner banner) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    //Key<Banner> bannerKey = Key.create(banner.getIdWebSafeBanner());
    //Banner banner1 = ofy().load().key(bannerKey).now();
    Banner banner1 = new Banner();
    banner1.setIdWebSafeBanner(banner.getIdWebSafeBanner());
    banner1.setRedirectUrl(banner.getRedirectUrl());
    banner1.setRedirectType(banner.getRedirectType());
    banner1.setRedirectId(banner.getRedirectId());
    banner1.setUrlBanner(banner.getUrlBanner());
    banner1.setDirectionBanner(banner.isDirectionBanner());
    banner1.setBannerWeb(banner.getBannerWeb());
    banner1.setOrder(banner.getOrder());
    banner1.setCampaignName(banner.getCampaignName());
    banner1.setCreative(banner.getCreative());
    banner1.setPosition(banner.getPosition());

    //ofy().save().entity(banner1).now();

    boolean result = APIAlgolia.updateBanner(new com.imaginamos.farmatodo.model.algolia.Banner(banner.getIdBanner(), banner.getUrlBanner(), banner.getRedirectUrl(), banner.getRedirectId(),
            banner.getRedirectType(), banner.getIdWebSafeBanner(), banner.getOrder(), banner.isDirectionBanner(), banner.getBannerWeb(), banner.getCampaignName(), banner.getCreative(),
            banner.getPosition(), (Objects.nonNull(idCategory) ? Long.parseLong(idCategory.toString()) : null), 0));
    if(result){
      return banner1;
    }else{
      return null;
    }
  }

  /**
   * Deleting of banner. Allowed execute to a banner with 'directionBanner' equals 'false',
   * whose value indicates that entity to create is a Slideshow Banner.
   *
   * @param token
   * @param tokenIdWebSafe
   * @throws ConflictException
   */
  @ApiMethod(name = "deleteBanner", path = "/bannerEndpoint/deleteBanner", httpMethod = ApiMethod.HttpMethod.DELETE)
  public void deleteBanner(@Named("idWebSafeBanner") final String idWebSafeBanner,
                           @Named("token") final String token,
                           @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    //Key<Banner> bannerKey = Key.create(idWebSafeBanner);
    //Banner banner1 = ofy().load().key(bannerKey).now();
    //ofy().delete().entity(banner1).now();

    APIAlgolia.deleteBanner(idWebSafeBanner);

  }

  @ApiMethod(name = "getBanners", path = "/bannerEndpoint/getBanners", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Banner> getBannerAlgolia(@Nullable @Named("idCategory") Integer idCategory,
                                              @Nullable @Named("directionBanner") Boolean directionBanner,
                                              @Nullable @Named("bannerWeb") Boolean bannerWeb,
                                              @Named("token") final String token,
                                              @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {
//    log.info("method: getBanners ");
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    List<com.imaginamos.farmatodo.model.algolia.Banner> bannerList = APIAlgolia.getBanners((Objects.nonNull(idCategory) ? Long.parseLong(idCategory.toString()) : null), directionBanner, bannerWeb);
      return  CollectionResponse.<Banner>builder().setItems(bannerList.stream().map(banner -> {
        Banner banner1 = new Banner();
        banner1.setRedirectUrl(banner.getRedirectUrl());
        banner1.setRedirectType(banner.getRedirectType());
        banner1.setRedirectId(banner.getRedirectId());
        banner1.setUrlBanner(banner.getUrlBanner());
        banner1.setDirectionBanner(banner.isDirectionBanner());
        banner1.setBannerWeb(banner.isBannerWeb());
        banner1.setOrder(banner.getOrder());
        banner1.setCampaignName(banner.getCampaignName());
        banner1.setCreative(banner.getCreative());
        banner1.setPosition(banner.getPosition());
        banner1.setIdWebSafeBanner(banner.getIdWebSafeBanner());
        //log.warning("method: getBanners - "+ banner1.getBannerWeb() +" : "+banner1.getIdBanner()+" : "+ banner1.getRedirectUrl() +" : "+ banner1.getUrlBanner());
        return banner1;
      }).collect(Collectors.toList())).build();
  }

  @ApiMethod(name = "getBannersOld", path = "/bannerEndpoint/getBannersOld", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Banner> getBanner(@Nullable @Named("idCategory") Integer idCategory,
                                              @Nullable @Named("directionBanner") Boolean directionBanner,
                                              @Nullable @Named("bannerWeb") Boolean bannerWeb,
                                              @Named("token") final String token,
                                              @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {
//    log.info("method: getBanners ");
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);
    boolean direction;
    if (directionBanner == null)
      direction = false;
    else
      direction = directionBanner;
    List<Banner> bannerList;
    if (idCategory != null) {
      Department department = ofy().load().type(Department.class).filter("id", idCategory).first().now();
      //log.warning("no banner web");
      bannerList = ofy().load().type(Banner.class).ancestor(department).list();//.filter("directionBanner", direction)
      List<Banner> bannerFilters = new ArrayList<>();
      for (Banner banner : bannerList) {
        if (banner.isDirectionBanner() == direction) {
          if(Objects.nonNull(banner.getRedirectUrl())) {
            banner.setRedirectUrl(banner.getRedirectUrl().replace("#/", ""));
//            log.info("method: getBanners - Con Categoría"+ banner.getBannerWeb() +" : "+banner.getIdBanner()+" : "+ banner.getRedirectUrl() +" : "+ banner.getUrlBanner());
          }
          bannerFilters.add(banner);
        }
      }
      bannerList = bannerFilters;

    } else {
      Key<Department> classificationLevel1 = Key.create(Department.class, 1L);
      if (bannerWeb != null)
        bannerList = ofy().load().type(Banner.class).filter("directionBanner", direction).filter("bannerWeb", bannerWeb).filterKey("<", classificationLevel1).list();
      else
        bannerList = ofy().load().type(Banner.class).filter("directionBanner", direction).filterKey("<", classificationLevel1).list();

//      log.info("method: getBanners - Evaluar Sin Categoría");
      if(Objects.nonNull(bannerList) && !bannerList.isEmpty()) {
        bannerList.stream().filter(banner -> Objects.nonNull(banner.getRedirectUrl())).forEach(banner -> banner.setRedirectUrl(banner.getRedirectUrl().replace("#/", "")));
        bannerList.stream().forEach(banner -> {
//          log.info("method: getBanners - Sin Categoría" + banner.getBannerWeb() + " : " + banner.getIdBanner() + " : " + banner.getRedirectUrl() + " : " + banner.getUrlBanner() + " classificationLevel1: "+classificationLevel1);
        });
      }
    }
    return CollectionResponse.<Banner>builder().setItems(bannerList).build();
  }

  @ApiMethod(name = "updateInfoPrivacy", path = "/bannerEndpoint/updateInfoPrivacy", httpMethod = ApiMethod.HttpMethod.PUT)
  public Answer updateInfoPrivacy(final InfoPrivacy infoPrivacy) throws BadRequestException, ConflictException {
    if (infoPrivacy == null)
      throw new BadRequestException(Constants.BODY_NOT_INITIALIZED);

    if (!authenticate.isValidToken(infoPrivacy.getToken(), infoPrivacy.getTokenIdWebSafe()))
      throw new ConflictException(Constants.INVALID_TOKEN);

    InfoPrivacy infoPrivacySaved = ofy().load().type(InfoPrivacy.class).first().now();
    if (infoPrivacySaved == null) {
      infoPrivacySaved = new InfoPrivacy();
      infoPrivacySaved.setIdInfoPrivacy(UUID.randomUUID().toString());
    }
    if (infoPrivacy.getHabeasData() != null)
      infoPrivacySaved.setHabeasData(infoPrivacy.getHabeasData());
    if (infoPrivacy.getPrivacyPolitics() != null)
      infoPrivacySaved.setPrivacyPolitics(infoPrivacy.getPrivacyPolitics());
    if (infoPrivacy.getTermsAndConditions() != null)
      infoPrivacySaved.setTermsAndConditions(infoPrivacy.getTermsAndConditions());
    ofy().save().entity(infoPrivacySaved);
    Answer answer = new Answer();
    answer.setConfirmation(true);
    return answer;
  }

    @ApiMethod(name = "migrateCategoryPhotoToAlgolia", path = "/bannerEndpoint/migrateCategoryPhotoToAlgolia", httpMethod = ApiMethod.HttpMethod.GET)
    public List<CategoryPhoto> migrateCategoryPhotoToAlgolia () throws ConflictException, BadRequestException {
        List<CategoryPhoto> categoryPhotoList = ofy().load().type(CategoryPhoto.class).list();
        categoryPhotoList.stream().forEach(categoryPhoto ->{
            //log.warning("method migrateCategoryPhotoToAlgolia: "+categoryPhoto);
            APIAlgolia.createCategoryPhoto(new com.imaginamos.farmatodo.model.algolia.CategoryPhoto(categoryPhoto.getIdCategoryPhoto(),
                    categoryPhoto.getIdDepartment(), categoryPhoto.getImagePosition(), categoryPhoto.getImageUrl(),categoryPhoto.getRedirect(), categoryPhoto.getRedirectUrl(),
                    categoryPhoto.getTokenIdWebSafe(), categoryPhoto.getToken(), UUID.randomUUID().toString()));
        });
        return categoryPhotoList;
    }

  @ApiMethod(name = "createCategoryPhoto", path = "/bannerEndpoint/createCategoryPhoto", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer createCategoryPhoto(final CategoryPhoto categoryPhoto) throws ConflictException, BadRequestException {
    if (categoryPhoto == null)
      throw new BadRequestException(Constants.BODY_NOT_INITIALIZED);

    if (!authenticate.isValidToken(categoryPhoto.getToken(), categoryPhoto.getTokenIdWebSafe()))
      throw new ConflictException(Constants.INVALID_TOKEN);

    categoryPhoto.setIdCategoryPhoto(UUID.randomUUID().toString());
    categoryPhoto.setIdPhotoWebSafe(UUID.randomUUID().toString());
    categoryPhoto.setImagePosition(calculateIndex(categoryPhoto.getImagePosition(), categoryPhoto.getIdDepartment()));
    boolean result = APIAlgolia.createCategoryPhoto(new com.imaginamos.farmatodo.model.algolia.CategoryPhoto(categoryPhoto.getIdCategoryPhoto(),
             categoryPhoto.getIdDepartment(), categoryPhoto.getImagePosition(), categoryPhoto.getImageUrl(),categoryPhoto.getRedirect(), categoryPhoto.getRedirectUrl(),
             categoryPhoto.getTokenIdWebSafe(), categoryPhoto.getToken(), categoryPhoto.getIdPhotoWebSafe()));
    Answer answer = new Answer();
    answer.setConfirmation(result);
    return answer;
  }

  @ApiMethod(name = "updateCategoryPhoto", path = "/bannerEndpoint/updateCategoryPhoto", httpMethod = ApiMethod.HttpMethod.PUT)
  public Answer updateCategoryPhoto(final CategoryPhoto categoryPhoto) throws ConflictException, BadRequestException {
    if (categoryPhoto == null)
      throw new BadRequestException(Constants.BODY_NOT_INITIALIZED);

    if (!authenticate.isValidToken(categoryPhoto.getToken(), categoryPhoto.getTokenIdWebSafe()))
      throw new ConflictException(Constants.INVALID_TOKEN);
    //log.warning(categoryPhoto.getIdPhotoWebSafe());
    APIAlgolia.updateCategoryPhoto(new com.imaginamos.farmatodo.model.algolia.CategoryPhoto(categoryPhoto.getIdCategoryPhoto(),
            categoryPhoto.getIdDepartment(), categoryPhoto.getImagePosition(), categoryPhoto.getImageUrl(),categoryPhoto.getRedirect(), categoryPhoto.getRedirectUrl(),
            categoryPhoto.getTokenIdWebSafe(), categoryPhoto.getToken(), categoryPhoto.getIdPhotoWebSafe()));

    Answer answer = new Answer();
    answer.setConfirmation(true);
    return answer;
  }

  @ApiMethod(name = "deleteCategoryPhoto", path = "/bannerEndpoint/deleteCategoryPhoto", httpMethod = ApiMethod.HttpMethod.DELETE)
  public Answer deleteCategoryPhoto(@Named("idPhotoWebSafe") final String idPhotoWebSafe,
                                    @Named("token") final String token,
                                    @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);
    boolean result = APIAlgolia.deleteCategoryPhoto(idPhotoWebSafe);
    Answer answer = new Answer();
    answer.setConfirmation(result);
    return answer;
  }

  @ApiMethod(name = "getCategoryPhoto", path = "/bannerEndpoint/getCategoryPhoto", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<CategoryPhoto> getCategoryPhoto(@Nullable @Named("idDepartment") final Long idDepartment,
                                                            @Named("token") final String token,
                                                            @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    List<com.imaginamos.farmatodo.model.algolia.CategoryPhoto> categoryPhotosList = APIAlgolia.getCategoryPhotos(idDepartment);
    return CollectionResponse.<CategoryPhoto>builder().setItems(categoryPhotosList.stream().map(categoryPhotoAlgolia -> {
      CategoryPhoto categoryPhoto = new CategoryPhoto();
      categoryPhoto.setIdPhotoWebSafe(categoryPhotoAlgolia.getIdPhotoWebSafe());
      categoryPhoto.setIdCategoryPhoto(categoryPhotoAlgolia.getIdCategoryPhoto());
      categoryPhoto.setIdDepartment(categoryPhotoAlgolia.getIdDepartment());
      categoryPhoto.setImagePosition(categoryPhotoAlgolia.getImagePosition());
      categoryPhoto.setImageUrl(categoryPhotoAlgolia.getImageUrl());
      categoryPhoto.setRedirect(categoryPhotoAlgolia.getRedirect());
      categoryPhoto.setRedirectUrl(categoryPhotoAlgolia.getRedirectUrl());
      categoryPhoto.setToken(categoryPhotoAlgolia.getToken());
      categoryPhoto.setTokenIdWebSafe(categoryPhotoAlgolia.getTokenIdWebSafe());
      log.info("method: getCategoryPhoto - " + categoryPhoto.getIdPhotoWebSafe() + " : " + categoryPhoto.getIdDepartment() + " : " + categoryPhoto.getIdCategoryPhoto() + " : " + categoryPhoto.getImagePosition());
      return categoryPhoto;
    }).collect(Collectors.toList())).build();
  }

  private Integer calculateIndex(int index, long idDepartment) {
    return APIAlgolia.calculateCategoryPhotoIndex(index, idDepartment);
  }
}

