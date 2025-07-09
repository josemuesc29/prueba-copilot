package repository

import (
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	pkgHttp "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_products/mappers"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_products/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_products/model/request"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_products/model/response"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"net/http"
	"time"
)

const (
	retry                          = 1
	urlCatalogProducts             = "%s/1/indexes/products-colombia/%s"
	urlCatalogProductsByObjectsIds = "%s/1/indexes/*/objects"
	urlCatalogProductsByQuery      = "%s/1/indexes/*/queries"
	headerContentType              = "Content-Type"
	headerAlgoliaApiKey            = "x-algolia-api-key"
	headerAlgoliaAppID             = "x-algolia-application-id"
	contentTypeValueJson           = "application/json"
)

type CatalogProduct struct {
}

func NewCatalogProduct() out.CatalogProduct {
	return CatalogProduct{}
}

func (t CatalogProduct) GetProductInformation(c *gin.Context, productID string) (model.ProductInformation, error) {
	var resp response.CatalogProductInformation

	url := fmt.Sprintf(urlCatalogProducts, config.Enviroments.CatalogProductsUrl, productID)
	log.Println("URL Catalog Products:", url)

	_, err := pkgHttp.DoRequest(
		pkgHttp.Requestor{
			HttpMethod: http.MethodGet,
			MaxRetry:   retry,
			Backoff:    5 * time.Second,
			TTLTimeOut: 3 * time.Second,
			URL:        url,
			Body:       nil,
			Headers: http.Header{
				headerContentType:   {contentTypeValueJson},
				headerAlgoliaApiKey: {config.Enviroments.ApiKeyCatalogProducts},
			},
			Context: c,
		},
		&resp)

	return mappers.CatalogProductInformationToCarouselProductInformation(&resp), err
}

func (t CatalogProduct) GetProductsInformationByObjectID(c *gin.Context, products []string, countryID string) ([]model.ProductInformation, error) {
	var resp response.CatalogProductsInformation

	url := fmt.Sprintf(urlCatalogProductsByObjectsIds, config.Enviroments.CatalogProductsUrl)
	log.Println("URL Catalog Products by Object IDs:", url)

	_, err := pkgHttp.DoRequest(
		pkgHttp.Requestor{
			HttpMethod: http.MethodPost,
			MaxRetry:   retry,
			Backoff:    5 * time.Second,
			TTLTimeOut: 3 * time.Second,
			URL:        url,
			Body:       getRequetsByProducts(products, countryID),
			Headers: http.Header{
				headerContentType:   {contentTypeValueJson},
				headerAlgoliaApiKey: {config.Enviroments.ApiKeyCatalogProducts},
				headerAlgoliaAppID:  {config.Enviroments.ApplicationIDCatalogProducts},
			},
			Context: c,
		},
		&resp)

	return mappers.CatalogProductsInformationToCarouselProductInformationList(resp.Results), err
}

func (t CatalogProduct) GetProductsInformationByQuery(c *gin.Context, params string, countryID string) ([]model.ProductInformation, error) {
	var resp response.CatalogProductsInformationByQuery

	body := request.CatalogByQuery{
		Requests: []request.Query{
			{
				Query:     "",
				IndexName: enums.CountryIndexName[countryID],
				Params:    params,
			},
		},
	}

	url := fmt.Sprintf(urlCatalogProductsByQuery, config.Enviroments.CatalogProductsUrl)
	log.Println("URL Catalog Products by Query:", url)

	_, err := pkgHttp.DoRequest(
		pkgHttp.Requestor{
			HttpMethod: http.MethodPost,
			MaxRetry:   retry,
			Backoff:    5 * time.Second,
			TTLTimeOut: 3 * time.Second,
			URL:        url,
			Body:       body,
			Headers: http.Header{
				headerContentType:   {contentTypeValueJson},
				headerAlgoliaApiKey: {config.Enviroments.ApiKeyCatalogProducts},
				headerAlgoliaAppID:  {config.Enviroments.ApplicationIDCatalogProducts},
			},
			Context: c,
		},
		&resp)

	return mappers.CatalogProductsInformationToCarouselProductInformationList(resp.Results[0].Hits), err
}

func getRequetsByProducts(products []string, countryID string) request.RqCatalogProductsIndexByObjects {
	var req request.RqCatalogProductsIndexByObjects

	for _, product := range products {
		req.Requests = append(req.Requests, request.Objects{
			IndexName: enums.CountryIndexName[countryID],
			ObjectID:  product,
		})
	}
	log.Println("Request Catalog Products by Object IDs:", req)
	return req
}
