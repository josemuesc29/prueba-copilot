package repository

import (
	"errors"
	"github.com/DATA-DOG/go-sqlmock"
	"github.com/stretchr/testify/assert"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"regexp"
	"testing"
	"time"
)

func setupMockDB(t *testing.T) (*gorm.DB, sqlmock.Sqlmock) {
	db, mock, err := sqlmock.New()
	assert.NoError(t, err)
	gdb, err := gorm.Open(postgres.New(postgres.Config{
		Conn: db,
	}), &gorm.Config{})
	assert.NoError(t, err)
	return gdb, mock
}

func TestGetBestSellerDepartment_Success(t *testing.T) {
	db, mock := setupMockDB(t)
	repo := NewBestSellerRepository(db)

	rows := sqlmock.NewRows([]string{"country_id", "department_id", "item_best_seller_id", "item_id", "create_date", "update_date"}).
		AddRow("CO", "12", "bs-1", "item-1", time.Now(), time.Now())

	mock.ExpectQuery(regexp.QuoteMeta(`SELECT * FROM "catalog"."best_sellers" WHERE country_id = $1 AND department_id = $2 ORDER BY create_date DESC`)).
		WithArgs("CO", "12").
		WillReturnRows(rows)

	result, err := repo.GetBestSellerDepartment("CO", "12")
	assert.NoError(t, err)
	assert.NotNil(t, result)
	assert.Equal(t, "CO", (*result)[0].CountryID)
	assert.Equal(t, "12", (*result)[0].DepartmentID)
}

func TestGetBestSellerDepartment_DBError(t *testing.T) {
	db, mock := setupMockDB(t)
	repo := NewBestSellerRepository(db)

	mock.ExpectQuery(regexp.QuoteMeta(`SELECT * FROM "catalog"."best_sellers" WHERE country_id = $1 AND department_id = $2 ORDER BY create_date DESC`)).
		WithArgs("CO", "12").
		WillReturnError(errors.New("db error"))

	result, err := repo.GetBestSellerDepartment("CO", "12")
	assert.Error(t, err)
	assert.Nil(t, result)
}
