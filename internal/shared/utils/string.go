package utils

import "strings"

func StringPtr(s string) *string {
	return &s
}

func ReplacePlaceholders(input *string, replacements map[string]string) *string {
	if input == nil || strings.TrimSpace(*input) == "" {
		return input
	}

	for placeholder, replacement := range replacements {
		input = StringPtr(strings.ReplaceAll(*input, "{"+placeholder+"}", replacement))
	}

	return input
}
