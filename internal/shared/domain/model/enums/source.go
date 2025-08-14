package enums

type Source string

const (
	Android    Source = "ANDROID"
	Ios        Source = "IOS"
	Responsive Source = "RESPONSIVE"
	Web        Source = "WEB"
)

type PlatformType string

const (
	Mobile  PlatformType = "MOBILE"
	Desktop PlatformType = "WEB"
)

func GetPlatformType(platform Source) PlatformType {
	if platform == Web {
		return Desktop
	}

	return Mobile
}
