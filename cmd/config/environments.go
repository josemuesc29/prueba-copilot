package config

import (
	"log"

	"go-simpler.org/env"
)

type configEnv struct {
	ConfigReadHostUrl                 string `env:"URL_HOST_CONFIG_READ" default:"ftd-td-config-read-services.dev.farmatodo.com" usage:"URL host to consume Config read API"`
	CatalogCategoryHostUrl            string `env:"URL_HOST_CATALOG_READ" default:"ftd-td-catalog-read-services.dev.farmatodo.com" usage:"URL host to consume Config read API"`
	CatalogProductsUrl                string `env:"URL_CATALOG_PRODUCTS" default:"https://api-search.farmatodo.com" usage:"url for get catalog of prodcts"`
	BalancerUrl                       string `env:"URL_BALANCER" default:"https://env2-api-transactional-dev.farmatodo.com" usage:"url balancer"`
	ProxyCmsUrl                       string `env:"PROXY_CMS_URL" default:"http://localhost:8082/proxy-cms" usage:"URL to consume CMS Proxy"`
	ApiKeyCatalogProducts             string `env:"API_KEY_CATALOG_PRODUCTS" default:"0ec5a07f2e863610f77c56c2aa56c690" usage:"api key for catalog products"`
	ApplicationIDCatalogProducts      string `env:"APP_ID_CATALOG_PRODUCTS" default:"VCOJEYD2PO" usage:"application id for catalog products"`
	RedisHost                         string `env:"REDIS_HOST" default:"localhost" usage:"hots for redis"`
	RedisPort                         string `env:"REDIS_PORT" default:"6379" usage:"port for redis"`
	RedisBestSellerDepartmentTTL      int64  `env:"REDIS_BEST_SELLER_DEPARMENT_TTL"  default:"720" usage:"application id for catalog products"`
	RedisSameBrandDepartmentTTL       int64  `env:"REDIS_SAME_BRAND_DEPARMENT_TTL"  default:"720" usage:"application id for catalog products"`
	RedisProductsRelatedDepartmentTTL int64  `env:"REDIS_PRODUCTS_RELATED_DEPARMENT_TTL"  default:"720" usage:"application id for catalog products"`
	MaxItemsSubstitute                int    `env:"MAX_ITEMS_SUBSTITUTE"  default:"100" usage:"max substitute for products"`
	RedisItemDetailTTL                int64  `env:"REDIS_ITEM_DETAIL_TTL" default:"720" usage:"ttl for item detail cache in redis"`
	RedisItemStructureTTL             int64  `env:"REDIS_ITEM_STRUCTURE_TTL" default:"720" usage:"ttl for items structure  cache in redis"`
	RedisTextSeoTTL                   int64  `env:"REDIS_TEXT_SEO_TTL" default:"720" usage:"ttl for text seo cache in redis"`
	DbHost                            string `env:"DB_HOST" default:"localhost" usage:"host for database"`
	DbPort                            string `env:"DB_PORT" default:"5003" usage:"port for database"`
	DbUser                            string `env:"DB_USER" default:"postgres" usage:"user for database"`
	DbPassword                        string `env:"DB_PASSWORD" default:"admin" usage:"password for database"`
	DbName                            string `env:"DB_NAME" default:"catalog_db" usage:"name for database"`
	DbSSLMode                         string `env:"DB_SSLMODE" default:"disable" usage:"ssl mode for database"`
	DbTimeZone                        string `env:"DB_TIMEZONE" default:"America/Bogota" usage:"timezone for database"`
}

var Enviroments configEnv

func LoadEnviroments() {
	if err := env.Load(&Enviroments, nil); err != nil {
		log.Fatal(err)
	}
}
