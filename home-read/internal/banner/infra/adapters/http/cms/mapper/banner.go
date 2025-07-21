package mapper

import (
	"ftd-td-home-read-services/internal/banner/domain/model"
	modelCms "ftd-td-home-read-services/internal/banner/infra/adapters/http/cms/model"
)

func GetBanners(bannerCms modelCms.BannerCms) []model.Banner {
	banners := make([]model.Banner, 0, len(bannerCms.Data))

	for _, bannerCms := range bannerCms.Data {
		banners = append(banners, model.Banner{
			ID:                 bannerCms.ID,
			DocumentID:         bannerCms.DocumentID,
			Extras:             bannerCms.Extras,
			Viewed:             bannerCms.Viewed,
			Title:              bannerCms.Title,
			Description:        bannerCms.Description,
			URL:                bannerCms.URL,
			Type:               bannerCms.Type,
			ExpiresAt:          bannerCms.ExpiresAt,
			LinkText:           bannerCms.LinkText,
			CreatedAt:          bannerCms.CreatedAt,
			UpdatedAt:          bannerCms.UpdatedAt,
			PublishedAt:        bannerCms.PublishedAt,
			Locale:             bannerCms.Locale,
			Position:           bannerCms.Position,
			Product:            bannerCms.Product,
			Content:            bannerCms.Content,
			Category:           bannerCms.Category,
			BannerName:         bannerCms.BannerName,
			ClassificationId:   bannerCms.ClassificationId,
			WebImageUrl:        bannerCms.WebImageUrl,
			ResponsiveImageUrl: bannerCms.ResponsiveImageUrl,
			AndroidImageUrl:    bannerCms.AndroidImageUrl,
			IosImageUrl:        bannerCms.IosImageUrl,
		})
	}

	return banners

}
