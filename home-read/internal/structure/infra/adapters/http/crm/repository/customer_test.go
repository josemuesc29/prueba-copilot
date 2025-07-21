package repository

import (
	"context"
	"ftd-td-home-read-services/cmd/config"
	apperrors "ftd-td-home-read-services/internal/structure/domain/errors"
	"ftd-td-home-read-services/internal/structure/domain/model"
	mockserver "ftd-td-home-read-services/test/mocks/server"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
)

const (
	countryID      = "AR"
	customerID     = "123"
	crmUrlMock     = "/r/" + countryID + "/v1/customers/" + customerID
	jsonResponseOk = `{
		"code": "200",
		"message": "OK",
		"data": {
			"customerId": "123",
			"firstName": "John",
			"lastName": "Doe"
		}
	}`
	jsonResponseNotFound = `{
		"code": "NO_CONTENT",
		"message": "Customer not found by id: ` + customerID + `",
		"data": null
	}`
	jsonResponseError = `{
		"code": "APPLICATION_ERROR",
		"message": "Internal Server Error",
		"data": null
	}`
	headerCorrelationValue = "1291823jhau1uha"
)

func TestGetCustomerByIDShouldReturnCustomer(t *testing.T) {
	// Arrange
	server, ctx := setUpServer(http.StatusOK, jsonResponseOk)
	defer server.Close()

	repository := NewCustomerRepository()

	expectedCustomer := model.Customer{
		ID:        customerID,
		FirstName: "John",
		LastName:  "Doe",
	}

	// Act
	customer, err := repository.GetCustomerByID(ctx, countryID, customerID)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expectedCustomer, customer)
}

func TestGetCustomerByIDShouldReturnCustomerNotFoundWhenServerReturns204(t *testing.T) {
	// Arrange
	server, ctx := setUpServer(http.StatusNoContent, jsonResponseNotFound)
	defer server.Close()

	repository := NewCustomerRepository()

	// Act
	customer, err := repository.GetCustomerByID(ctx, countryID, customerID)

	// Assert
	assert.Error(t, err)
	assert.ErrorIs(t, err, apperrors.ErrCustomerNotFound)
	assert.Equal(t, model.Customer{}, customer)
}

func TestGetCustomerByIDShouldReturnErrorWhenServerReturnsError(t *testing.T) {
	// Arrange
	server, ctx := setUpServer(http.StatusInternalServerError, jsonResponseError)
	defer server.Close()

	repository := NewCustomerRepository()
	// Act
	customer, err := repository.GetCustomerByID(ctx, countryID, customerID)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, model.Customer{}, customer)
}

func setUpServer(statusCode int, jsonResponse string) (*httptest.Server, context.Context) {
	server := mockserver.ConfigMockSrver(
		jsonResponse,
		crmUrlMock,
		http.MethodGet,
		statusCode,
		"{}",
	)

	config.Enviroments.CrmApiUrl = server.URL

	ctx := context.Background()

	return server, ctx
}
