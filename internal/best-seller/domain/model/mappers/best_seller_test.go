package mappers

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestMapProductInformationToBestSellerDepartment(t *testing.T) {
	productInfo := sharedModel.ProductInformation{
		MediaImageUrl:     "img.jpg",
		MediaDescription:  "desc media",
		FullPrice:         100.5,
		Brand:             "brandX",
		Sales:             10,
		GrayDescription:   "desc gray",
		OfferPrice:        80.0,
		OfferDescription:  "oferta",
		ID:                "id-1",
		OfferText:         "texto oferta",
		Marca:             "marcaX",
		ObjectID:          "obj-1",
		OnlyOnline:        true,
		DeliveryTime:      "24h",
		Highlight:         true,
		Generics:          false,
		LargeDescription:  "desc larga",
		AnywaySelling:     true,
		Spaces:            5,
		Status:            "active",
		TaxRate:           19,
		ListUrlImages:     []string{"img1.jpg", "img2.jpg"},
		MeasurePum:        1.5,
		LabelPum:          "kg",
		IDHighlights:      []int{1, 2},
		IDSuggested:       []int{3, 4},
		Departments:       []string{"dep1", "dep2"},
		SubCategory:       "subcat",
		Supplier:          "proveedor",
		Outofstore:        false,
		OfferStartDate:    1234567890,
		OfferEndDate:      1234567999,
		PrimePrice:        70.0,
		PrimeTextDiscount: "prime descuento",
		PrimeDescription:  "prime desc",
		RmsClass:          "classA",
		RmsDeparment:      "depA",
		RmsGroup:          "groupA",
		RmsSubclass:       "subclassA",
		HasStock:          true,
		URL:               "http://url.com",
	}
	storeGroupID := int64(99)

	result := MapProductInformationToBestSellerDepartment(productInfo, storeGroupID)

	expected := model.BestSellerDepartment{
		MediaImageUrl:     "img.jpg",
		Description:       "desc media",
		FullPrice:         100.5,
		MediaDescription:  "desc media",
		Brand:             "brandX",
		Sales:             10,
		DetailDescription: "desc gray",
		OfferPrice:        80.0,
		OfferDescription:  "oferta",
		ID:                "id-1",
		OfferText:         "texto oferta",
		IdStoreGroup:      99,
		Marca:             "marcaX",
		ObjectID:          "obj-1",
		OnlyOnline:        true,
		DeliveryTime:      "24h",
		Highlight:         true,
		Generic:           false,
		LargeDescription:  "desc larga",
		AnywaySelling:     true,
		Spaces:            5,
		Status:            "active",
		TaxRate:           19,
		ListUrlImages:     []string{"img1.jpg", "img2.jpg"},
		MeasurePum:        1.5,
		LabelPum:          "kg",
		HighlightsID:      []int{1, 2},
		SuggestedID:       []int{3, 4},
		Departments:       []string{"dep1", "dep2"},
		SubCategory:       "subcat",
		Supplier:          "proveedor",
		Outofstore:        false,
		OfferStartDate:    1234567890,
		OfferEndDate:      1234567999,
		PrimePrice:        70.0,
		PrimeTextDiscount: "prime descuento",
		PrimeDescription:  "prime desc",
		RmsClass:          "classA",
		RmsDeparment:      "depA",
		RmsGroup:          "groupA",
		RmsSubclass:       "subclassA",
		WithoutStock:      true,
		URL:               "http://url.com",
	}

	assert.Equal(t, expected, result)
}
