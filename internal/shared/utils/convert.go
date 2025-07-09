package utils

import "strconv"

func AnyToBool(value any) bool {
	switch v := value.(type) {
	case string:
		return v == "true"
	case bool:
		return v
	case int, int8, int16, int32, int64, uint, uint8, uint16, uint32, uint64:
		return v != 0
	default:
		return false
	}
}

func AnyToInt64(value any) int64 {
	switch v := value.(type) {
	case int:
		return int64(v)
	case int8:
		return int64(v)
	case int16:
		return int64(v)
	case int32:
		return int64(v)
	case int64:
		return v
	case uint:
		return int64(v)
	case uint8:
		return int64(v)
	case uint16:
		return int64(v)
	case uint32:
		return int64(v)
	case uint64:
		if v > (1<<63)-1 {
			return 0
		}
		return int64(v)
	case string:
		i, _ := strconv.ParseInt(v, 10, 64)
		return i
	default:
		return 0
	}
}

func StringToInt64(value string) int64 {
	response, err := strconv.ParseInt(value, 10, 64)
	if err != nil {
		return 0
	}
	return response
}
