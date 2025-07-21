package repository

import (
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/offer/domain/model"
	"ftd-td-home-read-services/internal/offer/domain/ports/out"
	"ftd-td-home-read-services/internal/offer/infra/adapters/http/cms/mapper"
	modelCms "ftd-td-home-read-services/internal/offer/infra/adapters/http/cms/model"
	pkgHttp "ftd-td-home-read-services/internal/shared/infra/adapters/http"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"net/http"
	"time"
)

const (
	retry             = 2
	urlCmsOffersFlash = "%s/%s/v1/offer/flash"
)

type offerCms struct {
}

func NewOfferCmsRepository() out.OfferCmsOutPort {
	return &offerCms{}
}

func (o *offerCms) GetFlashOffer(c *gin.Context, countryId string) ([]model.FlashOffer, error) {
	var resp modelCms.FlashOfferCms
	url := fmt.Sprintf(urlCmsOffersFlash, config.Enviroments.ProxyCmsUrl, countryId)
	log.Println("URL CMS flashOffers:", url)

	_, err := pkgHttp.DoRequest(
		pkgHttp.Requestor{
			HttpMethod: http.MethodGet,
			MaxRetry:   retry,
			Backoff:    5 * time.Second,
			TTLTimeOut: 5 * time.Second,
			URL:        url,
			Body:       nil,
			Headers: http.Header{
				"Content-Type": {"application/json"},
			},
			Context: c,
		},
		&resp)

	return mapper.GetflashOfferFromCms(resp), err
}
