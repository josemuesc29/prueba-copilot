package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model/mappers"
	inPorts "ftd-td-catalog-item-read-services/internal/best-seller/domain/ports/in"
	outPorts "ftd-td-catalog-item-read-services/internal/best-seller/domain/ports/out"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"strconv"
	"time"
)

const (
	GetBestSellerDepartmentLog        = "BestSellerService.GetBestSellerDepartment"
	getBestSellersLog                 = "BestSellerService.GetBestSellers"
	getBestSellersInAlgoliaByQueryLog = "BestSellerService.GetBestSellersInAlgoliaByQuery"
	executeGetAllProductsInfoLog      = "CarouselService.executeGetAllProductsInfo"
	findBestSellerInCacheLog          = "BestSellerService.findBestSellerInCache"
	repositoryConfig                  = "repository config"
	repositoryCategory                = "repository category"
	repositoryBestSellerDB            = "repository best sellers db"
	repositoryCache                   = "repository cache"
	repositoryProxyCatalogProduct     = "repository proxy catalog product"
	suggestedServices                 = "services"
	indexCatalogProducts              = "index catalog products"
	keyBestSellerCache                = "best_seller_department_%s_%s_%s"
	configBestSellerKey               = "BEST-SELLERS.CONFIG"
)

type bestSeller struct {
	outPortBestSellerDB    outPorts.BestSellerOutPort
	outPortConfig          sharedOutPorts.ConfigOutPort
	outPortCatalogCategory sharedOutPorts.CatalogCategoryOutPort
	outPortCatalogProduct  sharedOutPorts.CatalogProduct
	outPortCache           sharedOutPorts.Cache
}

func NewBestSeller(outPortBestSellerDB outPorts.BestSellerOutPort, outPortConfig sharedOutPorts.ConfigOutPort,
	outPortCatalogCategory sharedOutPorts.CatalogCategoryOutPort, outPortCatalogProduct sharedOutPorts.CatalogProduct, outPortCache sharedOutPorts.Cache) inPorts.BestSeller {
	return &bestSeller{outPortBestSellerDB: outPortBestSellerDB, outPortConfig: outPortConfig,
		outPortCatalogCategory: outPortCatalogCategory, outPortCatalogProduct: outPortCatalogProduct,
		outPortCache: outPortCache}
}

func (t *bestSeller) GetBestSellerDepartment(ctx *gin.Context, countryID,
	departmentID, storeID string) ([]model.BestSellerDepartment, error) {
	var rs []model.BestSellerDepartment

	findBestSellerInCache(ctx, t.outPortCache, countryID, departmentID, storeID, &rs)
	if len(rs) > 0 {
		return rs, nil
	}

	departmentName, valid, err := validExistDepartment(ctx, countryID, departmentID, t.outPortCatalogCategory)
	if !valid || err != nil {
		return nil, err
	}

	configBestSeller, err := t.outPortConfig.GetConfigBestSeller(ctx, countryID, configBestSellerKey)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
			fmt.Sprintf(enums.GetData, "error", repositoryConfig))
		return nil, err
	}

	var errMsg string

	if configBestSeller.AlgoliaRecommend {
		rs, err = getAlgoliaRecommend()
		errMsg = "Algolia recommend"
	} else {
		rs, err = getBestSellers(ctx, t,
			countryID, departmentID, storeID, departmentName, configBestSeller)
		errMsg = "get best sellers"
	}

	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
			fmt.Sprintf(enums.GetData, "error", errMsg))
		return nil, err
	}

	saveBestSellerInCache(ctx, t.outPortCache, countryID, departmentID, storeID, rs)
	log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
		fmt.Sprintf(enums.GetData, "Success", suggestedServices))
	return rs, nil
}

func getAlgoliaRecommend() ([]model.BestSellerDepartment, error) {
	return []model.BestSellerDepartment{}, nil
}

func getBestSellers(ctx *gin.Context, t *bestSeller, countryID, departmentID, storeID, departmentName string,
	configBestSeller sharedModel.ConfigBestSeller) ([]model.BestSellerDepartment, error) {
	var rs []model.BestSellerDepartment

	bestSellersDB, err := t.outPortBestSellerDB.GetBestSellerDepartment(countryID, departmentID)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), getBestSellersLog,
			fmt.Sprintf(enums.GetData, "error", repositoryBestSellerDB))
		return nil, err
	}

	if bestSellersDB != nil && len(*bestSellersDB) > 0 {
		err = executeGetAllProductsInfo(ctx, *bestSellersDB, t.outPortCatalogProduct,
			countryID, &rs, utils.StringToInt64(storeID))
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), getBestSellersLog,
			fmt.Sprintf(enums.GetData, "Success", indexCatalogProducts))
	} else {
		err = getBestSellersInAlgoliaByQuery(ctx, &rs, t.outPortCatalogProduct, countryID, storeID, departmentName, configBestSeller)
	}

	if err != nil {
		return nil, err
	}

	return rs, nil
}

func getBestSellersInAlgoliaByQuery(ctx *gin.Context, response *[]model.BestSellerDepartment,
	outPortProduct sharedOutPorts.CatalogProduct, countryID, storeID, departmentName string, configBestSeller sharedModel.ConfigBestSeller) error {

	products, err := outPortProduct.GetProductsInformationByQuery(ctx,
		fmt.Sprintf(configBestSeller.QueryProducts, strconv.Itoa(configBestSeller.CountItems), departmentName, storeID),
		countryID)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), getBestSellersInAlgoliaByQueryLog,
			fmt.Sprintf(enums.GetData, "Success", indexCatalogProducts))
		return err
	}

	for _, product := range products {
		*response = append(*response, mappers.MapProductInformationToBestSellerDepartment(product, utils.StringToInt64(storeID)))
	}

	return nil
}

func executeGetAllProductsInfo(ctx *gin.Context, bestSellersDB []model.BestSellerDepartmentEntity,
	outPortProduct sharedOutPorts.CatalogProduct, countryID string, response *[]model.BestSellerDepartment,
	storeID int64) error {
	products, err := outPortProduct.GetProductsInformationByObjectID(ctx,
		getProductsList(bestSellersDB), countryID)

	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), executeGetAllProductsInfoLog,
			fmt.Sprintf(enums.GetData, "error", repositoryProxyCatalogProduct))
		return err
	}

	for _, bestSellerDB := range bestSellersDB {
		product := findProductInfo(products, bestSellerDB.ItemID)

		if product.ObjectID == bestSellerDB.ItemID {
			*response = append(*response, mappers.MapProductInformationToBestSellerDepartment(product, storeID))
		} else {
			log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), executeGetAllProductsInfoLog,
				fmt.Sprintf(enums.NotFound, "Product", bestSellerDB.ItemID))
		}
	}

	return nil
}

func findProductInfo(products []sharedModel.ProductInformation, objectID string) sharedModel.ProductInformation {
	for _, product := range products {
		if product.ObjectID == objectID {
			return product
		}
	}

	return sharedModel.ProductInformation{}
}

func getProductsList(bestSellersDB []model.BestSellerDepartmentEntity) []string {
	var products []string

	for _, bestSellerDB := range bestSellersDB {
		products = append(products, bestSellerDB.ItemID)
	}
	return products
}

func findBestSellerInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, departmentID, storeID string, response *[]model.BestSellerDepartment) {
	suggestedFromCache, err := outPortCache.Get(ctx, fmt.Sprintf(keyBestSellerCache, countryID, departmentID, storeID))

	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
			fmt.Sprintf(enums.GetData, "Error", repositoryCache))
		return
	} else if suggestedFromCache == "" {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
			fmt.Sprintf(enums.GetData, "Not found", repositoryCache))
		return
	} else {
		err = json.Unmarshal([]byte(suggestedFromCache), &response)
		if err != nil {
			log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), findBestSellerInCacheLog, "Error decoding JSON:"+err.Error())
			return
		}
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
			fmt.Sprintf(enums.GetData, "Success", repositoryCache))
		return
	}
}

func saveBestSellerInCache(c *gin.Context, outPortCache sharedOutPorts.Cache, countryID, departmentID, storeID string, rs []model.BestSellerDepartment) {
	if len(rs) > 0 {
		suggestedToCache, errMarshal := json.Marshal(rs)
		if errMarshal == nil {
			err := outPortCache.Set(c, fmt.Sprintf(keyBestSellerCache, countryID, departmentID, storeID), string(suggestedToCache),
				time.Duration(config.Enviroments.RedisBestSellerDepartmentTTL)*time.Minute)

			if err != nil {
				log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog, "error to save cache")
			}
		}
	}
}

func validExistDepartment(ctx *gin.Context, countryID, departmentID string, outPortCatalogCategory sharedOutPorts.CatalogCategoryOutPort) (string, bool, error) {
	category, err := outPortCatalogCategory.GetCategoryByDepartment(ctx, countryID, departmentID)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
			fmt.Sprintf(enums.GetData, "error", repositoryCategory))
		return "", false, err
	}

	if category.Name == "" {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetBestSellerDepartmentLog,
			fmt.Sprintf(enums.NotFound, "Department", departmentID))
		return "", false, fmt.Errorf(enums.NotFound, "Department", departmentID)
	} else {
		return category.Name, true, nil
	}
}
