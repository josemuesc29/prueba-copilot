package mapper

import (
	"ftd-td-home-read-services/internal/banner/domain/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

// Test básico de conversión exitosa
func TestMapToBannerResponse_Success(t *testing.T) {
	content := "Contenido del banner"
	viewed := true

	banners := []model.Banner{
		{
			ID:                 1,
			DocumentID:         "doc-123",
			Extras:             map[string]interface{}{"key": "value"},
			Viewed:             &viewed,
			WebImageUrl:        "https://example.com/banner-web.jpg",
			ResponsiveImageUrl: "https://example.com/banner-responsive.jpg",
			IosImageUrl:        "https://example.com/banner-ios.jpg",
			AndroidImageUrl:    "https://example.com/banner-android.jpg",
			Title:              "Título",
			Description:        "Descripción",
			URL:                "https://example.com",
			Type:               "promo",
			ExpiresAt:          "2025-12-31",
			LinkText:           "Ver más",
			CreatedAt:          "2025-01-01",
			UpdatedAt:          "2025-01-02",
			PublishedAt:        "2025-01-03",
			Locale:             "es-CO",
			Position:           1,
			Product:            "producto",
			Content:            &content,
			Category:           "main",
			BannerName:         "Banner Principal",
		},
	}

	source := "WEB"
	bannerId := "Banner Principal"
	expectedImageURL := banners[0].WebImageUrl

	result := MapToBannerResponse(banners, source, bannerId)

	assert.Len(t, result, 1)
	assert.Equal(t, banners[0].ID, result[0].ID)
	assert.Equal(t, banners[0].DocumentID, result[0].DocumentID)
	assert.Equal(t, banners[0].Extras, result[0].Extras)
	assert.Equal(t, banners[0].Viewed, result[0].Viewed)
	assert.Equal(t, expectedImageURL, result[0].ImageURL)
	assert.Equal(t, banners[0].Title, result[0].Title)
	assert.Equal(t, banners[0].Description, result[0].Description)
	assert.Equal(t, banners[0].URL, result[0].URL)
	assert.Equal(t, banners[0].Type, result[0].Type)
	assert.Equal(t, banners[0].ExpiresAt, result[0].ExpiresAt)
	assert.Equal(t, banners[0].LinkText, result[0].LinkText)
	assert.Equal(t, banners[0].CreatedAt, result[0].CreatedAt)
	assert.Equal(t, banners[0].UpdatedAt, result[0].UpdatedAt)
	assert.Equal(t, banners[0].PublishedAt, result[0].PublishedAt)
	assert.Equal(t, banners[0].Position, result[0].Position)
	assert.Equal(t, banners[0].Locale, result[0].Locale)
	assert.Equal(t, banners[0].Product, result[0].Product)
	assert.Equal(t, banners[0].Content, result[0].Content)
	assert.Equal(t, banners[0].Category, result[0].Category)
	assert.Equal(t, banners[0].BannerName, result[0].BannerName)
}

func TestMapToBannerResponse_BySource(t *testing.T) {
	content := "Contenido del banner"
	viewed := true

	banner := model.Banner{
		ID:                 1,
		DocumentID:         "doc-123",
		Extras:             map[string]interface{}{"key": "value"},
		Viewed:             &viewed,
		WebImageUrl:        "https://example.com/banner-web.jpg",
		ResponsiveImageUrl: "https://example.com/banner-responsive.jpg",
		IosImageUrl:        "https://example.com/banner-ios.jpg",
		AndroidImageUrl:    "https://example.com/banner-android.jpg",
		Title:              "Título",
		Description:        "Descripción",
		URL:                "https://example.com",
		Type:               "promo",
		ExpiresAt:          "2025-12-31",
		LinkText:           "Ver más",
		CreatedAt:          "2025-01-01",
		UpdatedAt:          "2025-01-02",
		PublishedAt:        "2025-01-03",
		Locale:             "es-CO",
		Position:           1,
		Product:            "producto",
		Content:            &content,
		Category:           "main",
		BannerName:         "Banner Principal",
	}

	testCases := []struct {
		name           string
		source         string
		expectedImgURL string
	}{
		{"WEB source", "WEB", banner.WebImageUrl},
		{"RESPONSIVE source", "RESPONSIVE", banner.ResponsiveImageUrl},
		{"IOS source", "IOS", banner.IosImageUrl},
		{"ANDROID source", "ANDROID", banner.AndroidImageUrl},
	}
	bannerId := "Banner Principal"
	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			result := MapToBannerResponse([]model.Banner{banner}, tc.source, bannerId)

			assert.Len(t, result, 1)
			assert.Equal(t, banner.ID, result[0].ID)
			assert.Equal(t, banner.DocumentID, result[0].DocumentID)
			assert.Equal(t, banner.Extras, result[0].Extras)
			assert.Equal(t, banner.Viewed, result[0].Viewed)
			assert.Equal(t, tc.expectedImgURL, result[0].ImageURL)
			assert.Equal(t, banner.Title, result[0].Title)
			assert.Equal(t, banner.Description, result[0].Description)
			assert.Equal(t, banner.URL, result[0].URL)
			assert.Equal(t, banner.Type, result[0].Type)
			assert.Equal(t, banner.ExpiresAt, result[0].ExpiresAt)
			assert.Equal(t, banner.LinkText, result[0].LinkText)
			assert.Equal(t, banner.CreatedAt, result[0].CreatedAt)
			assert.Equal(t, banner.UpdatedAt, result[0].UpdatedAt)
			assert.Equal(t, banner.PublishedAt, result[0].PublishedAt)
			assert.Equal(t, banner.Locale, result[0].Locale)
			assert.Equal(t, banner.Product, result[0].Product)
			assert.Equal(t, banner.Content, result[0].Content)
			assert.Equal(t, banner.Category, result[0].Category)
			assert.Equal(t, banner.BannerName, result[0].BannerName)
		})
	}
}
