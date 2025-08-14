package entities_test

import (
	"testing"
	"ftd-td-catalog-item-read-services/internal/total-stock/infra/adapters/database/entities"
	"github.com/stretchr/testify/assert"
)

func TestLocationItem_TableName(t *testing.T) {
	// Arrange
	entity := entities.LocationItem{}

	// Act
	tableName := entity.TableName()

	// Assert
	assert.Equal(t, "location_item", tableName)
}
