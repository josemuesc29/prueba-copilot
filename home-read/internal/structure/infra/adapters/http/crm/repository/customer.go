package repository

import (
	"context"
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	pkgHttp "ftd-td-home-read-services/internal/shared/infra/adapters/http"
	"ftd-td-home-read-services/internal/shared/utils"
	apperrors "ftd-td-home-read-services/internal/structure/domain/errors"
	"ftd-td-home-read-services/internal/structure/domain/model"
	"ftd-td-home-read-services/internal/structure/domain/ports/out"
	entity "ftd-td-home-read-services/internal/structure/infra/adapters/http/crm/model"
	sharedentity "ftd-td-home-read-services/internal/structure/infra/adapters/http/model"
	"net/http"
	"time"

	"github.com/jinzhu/copier"
)

const (
	retryGetCustomer   = 0
	urlCrmGetCustomer  = "%s/r/%s/v1/customers/%s"
	getCustomerByIDLog = "CrmCustomerRepository.GetCustomerByID"
)

type crmCustomerRepository struct {
}

func NewCustomerRepository() out.CustomerRepository {
	return &crmCustomerRepository{}
}

func (s crmCustomerRepository) GetCustomerByID(ctx context.Context, countryID string, customerID string) (model.Customer, error) {
	var response sharedentity.ApiResponse[entity.Customer]

	request := pkgHttp.Requestor{
		HttpMethod: http.MethodGet,
		MaxRetry:   retryGetCustomer,
		Backoff:    0 * time.Second,
		TTLTimeOut: 5 * time.Second,
		URL:        fmt.Sprintf(urlCrmGetCustomer, config.Enviroments.CrmApiUrl, countryID, customerID),
		Body:       nil,
		Context:    ctx,
	}

	statusCode, err := pkgHttp.DoRequest(request, &response)

	if err != nil {
		if statusCode == http.StatusNoContent || statusCode == http.StatusNotFound {
			utils.LogWarn(ctx, getCustomerByIDLog, fmt.Sprintf("Customer with id %s not found: %v", customerID, err))
			return model.Customer{}, apperrors.ErrCustomerNotFound
		}

		utils.LogError(ctx, getCustomerByIDLog, fmt.Sprintf("Failed to get customer with id %s: %v", customerID, err))
	}

	customer := model.Customer{}
	errCopy := copier.Copy(&customer, &response.Data)
	if errCopy != nil {
		return model.Customer{}, err
	}

	return customer, err
}
