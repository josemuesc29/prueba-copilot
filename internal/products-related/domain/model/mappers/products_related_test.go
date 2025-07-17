package mappers

import (
	"testing"

	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/stretchr/testify/assert"
)

func TestMapProductInformationToProductsRelatedItem(t *testing.T) {
	t.Run("should correctly map ProductInformation to ProductsRelatedItem", func(t *testing.T) {
		productInfo := &sharedModel.ProductInformation{
			ObjectID:         "prod123",
			MediaDescription: "Test Media Description",
			LargeDescription: "Test Large Description",
			MediaImageUrl:    "http://example.com/image.jpg",
			FullPrice:        99.99,
			Prime:            "Yes",
			RMSProvider:      "TestProvider",
			Collections:      []string{"new", "featured"},
			AnywaySelling:    true,
			Barcode:          "1234567890123",
			BarcodeList:      []string{"1234567890123", "1234567890124"},
			Brand:            "TestBrand",
			Categorie:        "TestCategory",
			Classification: []sharedModel.Classification{
				{ID: 1, Name: "Class1", TypeID: 10, TypeName: "Type1"},
			},
			CustomLabelForStockZero: "Out of Stock",
			CustomLabelForStockZeroByCity: []sharedModel.CustomLabelForStockZeroByCity{
				{CityCode: "NYC", CustomLabel: "Out of Stock in NYC"},
			},
			CustomTag:            new(string),
			DeliveryTime:         "2-3 days",
			Departments:          []string{"Electronics", "Computers"},
			DesHiddenSEO:         new(string),
			FullPriceByCity:      []sharedModel.FullPriceByCity{},
			Generics:             false,
			GrayDescription:      "Gray description",
			HasStock:             true,
			Highlight:            true,
			ID:                   "prod123",
			IDOffersGroup:        []int{1, 2},
			IDHighlights:         []int{3, 4},
			IDSuggested:          []int{5, 6},
			IsCupon:              0,
			LabelPum:             "PUM Label",
			LastUpdate:           sharedModel.LastUpdate{JobName: "updateJob", TimeStamp: 1625140800},
			ListUrlImages:        []string{"http://example.com/img1.jpg", "http://example.com/img2.jpg"},
			Marca:                "TestMarca",
			MeasurePum:           1.0,
			MetadesSEO:           new(string),
			MetatituloSEO:        new(string),
			OfferDescription:     "Special Offer",
			OfferEndDate:         1627819200,
			OfferPrice:           79.99,
			OfferPriceByCity:     []interface{}{},
			OfferStartDate:       1625140800,
			OfferText:            "20% off",
			OnlyOnline:           false,
			Outofstore:           false,
			PrimeDescription:     "Prime benefits apply",
			PrimePrice:           75.99,
			PrimeTextDiscount:    "10% off for Prime members",
			RequirePrescription:  false,
			RmsClass:             "CLASS-A",
			RmsDeparment:         "DEPT-A",
			RmsGroup:             "GROUP-A",
			RmsSubclass:          "SUBCLASS-A",
			Sales:                100,
			Spaces:               5,
			Status:               "A",
			StoresWithOffer:      []int{101, 102},
			StoresWithPrimeOffer: []interface{}{},
			StoresWithStock:      []int{101, 102, 103},
			SubCategory:          "SubTestCategory",
			Supplier:             "TestSupplier",
			TaxRate:              21,
			URL:                  "/product/prod123",
			UrlCanonical:         new(string),
		}
		*productInfo.CustomTag = "custom"
		*productInfo.DesHiddenSEO = "hidden seo"
		*productInfo.MetadesSEO = "meta seo"
		*productInfo.MetatituloSEO = "metatitle seo"
		*productInfo.UrlCanonical = "/canonical/prod123"

		var productsRelatedItem model.ProductsRelatedItem
		mappedItem := MapProductInformationToProductsRelatedItem(&productsRelatedItem, productInfo)

		assert.Equal(t, productInfo.ObjectID, mappedItem.ID)
		assert.Equal(t, productInfo.MediaDescription, mappedItem.MediaDescription)
		assert.Equal(t, productInfo.LargeDescription, mappedItem.LargeDescription)
		assert.Equal(t, productInfo.MediaImageUrl, mappedItem.MediaImageUrl)
		assert.Equal(t, productInfo.FullPrice, mappedItem.FullPrice)

	})
}
