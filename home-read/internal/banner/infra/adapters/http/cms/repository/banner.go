package repository

import (
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/banner/domain/model"
	"ftd-td-home-read-services/internal/banner/domain/ports/out"
	"ftd-td-home-read-services/internal/banner/infra/adapters/http/cms/mapper"
	modelCms "ftd-td-home-read-services/internal/banner/infra/adapters/http/cms/model"
	pkgHttp "ftd-td-home-read-services/internal/shared/infra/adapters/http"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"net/http"
	"time"
)

const (
	retry        = 2
	urlCmsBanner = "%s/%s/v1/banner/banners"
)

type bannerCms struct {
}

func NewBannerCmsRepository() out.BannerCmsOutPort {
	return &bannerCms{}
}

func (o *bannerCms) GetBanners(c *gin.Context, countryId string) ([]model.Banner, error) {
	var resp modelCms.BannerCms
	url := fmt.Sprintf(urlCmsBanner, config.Enviroments.ProxyCmsUrl, countryId)
	log.Println("URL CMS Banners:", url)

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

	return mapper.GetBanners(resp), err
}
