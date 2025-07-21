package model

type Platform string

const (
	Android    Platform = "ANDROID"
	Ios        Platform = "IOS"
	Responsive Platform = "RESPONSIVE"
	Web        Platform = "WEB"
)

type PlatformType string

const (
	Mobile  PlatformType = "MOBILE"
	Desktop PlatformType = "WEB"
)

func GetPlatformType(platform Platform) PlatformType {
	if platform == Web {
		return Desktop
	}

	return Mobile
}
