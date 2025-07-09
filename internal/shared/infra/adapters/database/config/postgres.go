package config

import (
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	log "github.com/sirupsen/logrus"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"time"
)

type PostgresConfig struct {
	Host     string
	Port     string
	User     string
	Password string
	DBName   string
	SSLMode  string
	TimeZone string
}

func NewPostgresConnection() (*gorm.DB, error) {
	cfg := PostgresConfig{
		Host:     config.Enviroments.DbHost,
		Port:     config.Enviroments.DbPort,
		User:     config.Enviroments.DbUser,
		Password: config.Enviroments.DbPassword,
		DBName:   config.Enviroments.DbName,
		SSLMode:  config.Enviroments.DbSSLMode,
		TimeZone: config.Enviroments.DbTimeZone,
	}

	dsn := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s TimeZone=%s search_path=shopping_cart",
		cfg.Host, cfg.Port, cfg.User, cfg.Password, cfg.DBName, cfg.SSLMode, cfg.TimeZone,
	)

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
	})
	if err != nil {
		return nil, err
	}

	sqlDB, err := db.DB()
	if err != nil {
		return nil, err
	}

	// Optional tuning
	sqlDB.SetMaxOpenConns(25)
	sqlDB.SetMaxIdleConns(25)
	sqlDB.SetConnMaxLifetime(5 * time.Minute)

	log.Println("✅ Connected to PostgreSQL")
	return db, nil
}
