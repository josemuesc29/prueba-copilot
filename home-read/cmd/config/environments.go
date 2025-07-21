package config

import (
	"log"

	"go-simpler.org/env"
)

type configEnv struct {
	CrmApiUrl                    string `env:"CRM_API_URL" default:"http://localhost:8081/crm" usage:"URL to consume CRM API"`
	ProxyCmsUrl                  string `env:"PROXY_CMS_URL" default:"http://localhost:8082/proxy-cms" usage:"URL to consume CMS Proxy"`
	CatalogProductsUrl           string `env:"URL_CATALOG_PRODUCTS" default:"https://api-search.farmatodo.com" usage:"url for get catalog of prodcts"`
	ApiKeyCatalogProducts        string `env:"API_KEY_CATALOG_PRODUCTS" default:"0ec5a07f2e863610f77c56c2aa56c690" usage:"api key for catalog products"`
	ApplicationIDCatalogProducts string `env:"APP_ID_CATALOG_PRODUCTS" default:"VCOJEYD2PO" usage:"application id for catalog products"`
	RedisSuggestedTTL            int64  `env:"REDIS_SUGGESTED_TTL"  default:"15" usage:"application id for catalog products"`
	RedisFlashOffersTTL          int64  `env:"REDIS_FLASH_OFFER_TTL"  default:"15" usage:"application id for catalog products"`
	RedisBannersTTL              int64  `env:"REDIS_BANNERS_TTL"  default:"15" usage:"minutes ttl for banners cache"`
	RedisHost                    string `env:"REDIS_HOST" default:"localhost" usage:"hots for redis"`
	RedisPort                    string `env:"REDIS_PORT" default:"6379" usage:"port for redis"`
	CacheHomeStructureTTL        int64  `env:"CACHE_HOME_STRUCTURE_TTL" default:"1440" usage:"cache ttl for home structure in minutes"`
}

var Enviroments configEnv

func LoadEnviroments() {
	if err := env.Load(&Enviroments, nil); err != nil {
		log.Fatal(err)
	}
}
