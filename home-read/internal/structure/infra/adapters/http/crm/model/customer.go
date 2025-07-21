package model

type Customer struct {
	ID             string `json:"customerId"`
	FirstName      string `json:"firstName"`
	LastName       string `json:"lastName"`
	DocumentType   string `json:"documentTypeId"`
	DocumentNumber string `json:"documentNumber"`
	Email          string `json:"email"`
	Phone          string `json:"phone"`
}
