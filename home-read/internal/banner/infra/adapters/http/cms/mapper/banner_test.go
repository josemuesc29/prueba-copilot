package mapper

import (
	"ftd-td-home-read-services/internal/banner/domain/model"
	modelCms "ftd-td-home-read-services/internal/banner/infra/adapters/http/cms/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestGetBanners_EmptyData(t *testing.T) {
	cms := modelCms.BannerCms{Data: []modelCms.BannerData{}}
	result := GetBanners(cms)
	assert.Empty(t, result)
}

func TestGetBanners_SingleElement(t *testing.T) {
	viewed := true
	content := "content"
	cms := modelCms.BannerCms{
		Data: []modelCms.BannerData{
			{
				ID:                 1,
				DocumentID:         "doc-1",
				Extras:             map[string]interface{}{"key": "value"},
				Viewed:             &viewed,
				Title:              "Banner Title",
				Description:        "Banner Description",
				URL:                "https://example.com",
				Type:               "promo",
				ExpiresAt:          "2025-12-31",
				LinkText:           "Click here",
				CreatedAt:          "2025-01-01",
				UpdatedAt:          "2025-01-02",
				PublishedAt:        "2025-01-03",
				Locale:             "es-AR",
				Position:           1,
				Product:            "product-123",
				Content:            &content,
				Category:           "main",
				BannerName:         "Main Banner",
				ClassificationId:   1,
				WebImageUrl:        "https://example.com",
				ResponsiveImageUrl: "https://example.com",
				AndroidImageUrl:    "https://example.com",
				IosImageUrl:        "https://example.com",
			},
		},
	}

	result := GetBanners(cms)
	assert.Len(t, result, 1)
	assert.Equal(t, model.Banner{
		ID:                 1,
		DocumentID:         "doc-1",
		Extras:             map[string]interface{}{"key": "value"},
		Viewed:             &viewed,
		Title:              "Banner Title",
		Description:        "Banner Description",
		URL:                "https://example.com",
		Type:               "promo",
		ExpiresAt:          "2025-12-31",
		LinkText:           "Click here",
		CreatedAt:          "2025-01-01",
		UpdatedAt:          "2025-01-02",
		PublishedAt:        "2025-01-03",
		Locale:             "es-AR",
		Position:           1,
		Product:            "product-123",
		Content:            &content,
		Category:           "main",
		BannerName:         "Main Banner",
		ClassificationId:   1,
		WebImageUrl:        "https://example.com",
		ResponsiveImageUrl: "https://example.com",
		AndroidImageUrl:    "https://example.com",
		IosImageUrl:        "https://example.com",
	}, result[0])
}

func TestGetBanners_MultipleElements(t *testing.T) {
	cms := modelCms.BannerCms{
		Data: []modelCms.BannerData{
			{ID: 1, DocumentID: "doc1", Title: "A", Category: "main"},
			{ID: 2, DocumentID: "doc2", Title: "B", Category: "secondary"},
		},
	}
	result := GetBanners(cms)
	assert.Len(t, result, 2)
	assert.Equal(t, 1, result[0].ID)
	assert.Equal(t, "doc1", result[0].DocumentID)
	assert.Equal(t, "A", result[0].Title)
	assert.Equal(t, "main", result[0].Category)

	assert.Equal(t, 2, result[1].ID)
	assert.Equal(t, "doc2", result[1].DocumentID)
	assert.Equal(t, "B", result[1].Title)
	assert.Equal(t, "secondary", result[1].Category)
}
