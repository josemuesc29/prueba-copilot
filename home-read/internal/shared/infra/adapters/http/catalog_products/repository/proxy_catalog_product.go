package repository

import (
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/shared/domain/model"
	"ftd-td-home-read-services/internal/shared/domain/ports/out"
	pkgHttp "ftd-td-home-read-services/internal/shared/infra/adapters/http"
	"ftd-td-home-read-services/internal/shared/infra/adapters/http/catalog_products/mappers"
	"ftd-td-home-read-services/internal/shared/infra/adapters/http/catalog_products/model/enums"
	"ftd-td-home-read-services/internal/shared/infra/adapters/http/catalog_products/model/request"
	"ftd-td-home-read-services/internal/shared/infra/adapters/http/catalog_products/model/response"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"net/http"
	"time"
)

const (
	retry                          = 1
	urlCatalogProducts             = "%s/1/indexes/products-colombia/%s"
	urlCatalogProductsByObjectsIds = "%s/1/indexes/*/objects"
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
				"Content-Type":      {"application/json"},
				"x-algolia-api-key": {config.Enviroments.ApiKeyCatalogProducts},
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
				"Content-Type":             {"application/json"},
				"x-algolia-api-key":        {config.Enviroments.ApiKeyCatalogProducts},
				"x-algolia-application-id": {config.Enviroments.ApplicationIDCatalogProducts},
			},
			Context: c,
		},
		&resp)

	return mappers.CatalogProductsInformationToCarouselProductInformationList(resp), err
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
