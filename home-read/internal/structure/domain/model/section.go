package model

type Section struct {
	ID         int         `json:"id"`
	Title      string      `json:"title"`
	Components []Component `json:"components"`
}
