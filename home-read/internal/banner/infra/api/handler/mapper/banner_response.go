package mapper

import (
	"ftd-td-home-read-services/internal/banner/domain/model"
	"ftd-td-home-read-services/internal/banner/infra/api/handler/dto/response"
)

func MapToBannerResponse(banners []model.Banner, source string, bannerId string) []response.BannerResponse {
	var responseList []response.BannerResponse

	for _, banner := range banners {
		if banner.BannerName != bannerId {
			continue
		}
		var imageURL string
		switch source {
		case "WEB":
			imageURL = banner.WebImageUrl
		case "RESPONSIVE":
			imageURL = banner.ResponsiveImageUrl
		case "IOS":
			imageURL = banner.IosImageUrl
		case "ANDROID":
			imageURL = banner.AndroidImageUrl
		}

		responseList = append(responseList, response.BannerResponse{
			ID:          banner.ID,
			DocumentID:  banner.DocumentID,
			Extras:      banner.Extras,
			Viewed:      banner.Viewed,
			ImageURL:    imageURL,
			Title:       banner.Title,
			Description: banner.Description,
			URL:         banner.URL,
			Type:        banner.Type,
			ExpiresAt:   banner.ExpiresAt,
			LinkText:    banner.LinkText,
			CreatedAt:   banner.CreatedAt,
			UpdatedAt:   banner.UpdatedAt,
			PublishedAt: banner.PublishedAt,
			Locale:      banner.Locale,
			Position:    banner.Position,
			Product:     banner.Product,
			Content:     banner.Content,
			Category:    banner.Category,
			BannerName:  banner.BannerName,
		})
	}

	return responseList
}
