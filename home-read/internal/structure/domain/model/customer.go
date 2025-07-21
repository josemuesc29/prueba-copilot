package model

type Customer struct {
	ID             string `json:"id"`
	FirstName      string `json:"firstName"`
	LastName       string `json:"lastName"`
	DocumentType   string `json:"documentType"`
	DocumentNumber string `json:"documentNumber"`
	Email          string `json:"email"`
	Phone          string `json:"phone"`
}
