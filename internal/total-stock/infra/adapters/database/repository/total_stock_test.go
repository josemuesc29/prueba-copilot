package repository_test

import (
	"testing"
	"github.com/DATA-DOG/go-sqlmock"
	"github.com/stretchr/testify/assert"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"ftd-td-catalog-item-read-services/internal/total-stock/infra/adapters/database/repository"
)

func TestTotalStockRepository_GetStockByItemAndStores_Success(t *testing.T) {
	// Arrange
	db, mock, err := sqlmock.New()
	assert.NoError(t, err)
	defer db.Close()

	gormDB, err := gorm.Open(postgres.New(postgres.Config{
		Conn: db,
	}), &gorm.Config{})
	assert.NoError(t, err)

	repo := repository.NewTotalStockRepository(gormDB)

	countryID := "AR"
	itemID := "123"
	storeIDs := []string{"1", "2"}
	expectedStock := int64(150)

	expectedSQL := `SELECT COALESCE(SUM(stock), 0) FROM "location_item" WHERE country_id = $1 AND item_id = $2 AND location_id IN ($3,$4)`

	rows := sqlmock.NewRows([]string{"coalesce"}).AddRow(expectedStock)

	mock.ExpectQuery(expectedSQL).
		WithArgs(countryID, itemID, "1", "2").
		WillReturnRows(rows)

	// Act
	totalStock, err := repo.GetStockByItemAndStores(countryID, itemID, storeIDs)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expectedStock, totalStock)
	assert.NoError(t, mock.ExpectationsWereMet())
}

func TestTotalStockRepository_GetStockByItemAndStores_NoRows(t *testing.T) {
	// Arrange
	db, mock, err := sqlmock.New()
	assert.NoError(t, err)
	defer db.Close()

	gormDB, err := gorm.Open(postgres.New(postgres.Config{
		Conn: db,
	}), &gorm.Config{})
	assert.NoError(t, err)

	repo := repository.NewTotalStockRepository(gormDB)

	countryID := "AR"
	itemID := "999" // Non-existent item
	storeIDs := []string{"1"}

	expectedSQL := `SELECT COALESCE(SUM(stock), 0) FROM "location_item" WHERE country_id = $1 AND item_id = $2 AND location_id IN ($3)`

	// When no rows are found, the COALESCE should return 0.
	rows := sqlmock.NewRows([]string{"coalesce"}).AddRow(0)

	mock.ExpectQuery(expectedSQL).
		WithArgs(countryID, itemID, "1").
		WillReturnRows(rows)

	// Act
	totalStock, err := repo.GetStockByItemAndStores(countryID, itemID, storeIDs)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, int64(0), totalStock)
	assert.NoError(t, mock.ExpectationsWereMet())
}
