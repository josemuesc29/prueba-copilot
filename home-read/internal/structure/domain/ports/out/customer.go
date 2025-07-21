package out

//go:generate mockgen -source=customer.go -destination=../../../../../test/mocks/structure/domain/ports/out/customer_mock.go

import (
	"context"
	"ftd-td-home-read-services/internal/structure/domain/model"
)

type CustomerRepository interface {
	GetCustomerByID(ctx context.Context, countryID string, customerID string) (model.Customer, error)
}
