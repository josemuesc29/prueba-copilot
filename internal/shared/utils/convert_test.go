package utils

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestAnyToBoolWhenString_WhenSuccess(t *testing.T) {
	result := AnyToBool("true")

	assert.Equal(t, result, true)
}

func TestAnyToBoolWhenBool_WhenSuccess(t *testing.T) {
	result := AnyToBool(true)

	assert.Equal(t, result, true)
}

func TestAnyToBoolWhenNumber_WhenSuccess(t *testing.T) {
	result := AnyToBool(2)

	assert.Equal(t, result, true)
}

func TestAnyToBoolWhenInterface_WhenSuccess(t *testing.T) {
	var value interface{}
	result := AnyToBool(value)

	assert.Equal(t, result, false)
}

func TestAnyToInt64WhenNumber_WhenSuccess(t *testing.T) {
	//int
	result := AnyToInt64(1)
	assert.Equal(t, result, int64(1))

	//int8
	result = AnyToInt64(int8(1))
	assert.Equal(t, result, int64(1))

	//int16
	result = AnyToInt64(int16(1))
	assert.Equal(t, result, int64(1))

	//int32
	result = AnyToInt64(int32(1))
	assert.Equal(t, result, int64(1))

	//int64
	result = AnyToInt64(int64(1))
	assert.Equal(t, result, int64(1))

	//uint
	result = AnyToInt64(uint(1))
	assert.Equal(t, result, int64(1))

	//uint8
	result = AnyToInt64(uint8(1))
	assert.Equal(t, result, int64(1))

	//uint16
	result = AnyToInt64(uint16(1))
	assert.Equal(t, result, int64(1))

	//uint32
	result = AnyToInt64(uint32(1))
	assert.Equal(t, result, int64(1))

	//uint64
	result = AnyToInt64(uint64(1))
	assert.Equal(t, result, int64(1))

	//uint64
	result = AnyToInt64(uint64(9223372036854775809))
	assert.Equal(t, result, int64(0))
}

func TestAnyToInt64WhenString_WhenSuccess(t *testing.T) {
	result := AnyToInt64("1")
	assert.Equal(t, result, int64(1))
}

func TestAnyToInt64WhenInterface_WhenSuccess(t *testing.T) {
	var value interface{}
	result := AnyToInt64(value)
	assert.Equal(t, result, int64(0))
}

func TestStringToInt64_WhenSuccess(t *testing.T) {
	result := StringToInt64("123")
	assert.Equal(t, result, int64(123))
}

func TestStringToInt64_WhenFail(t *testing.T) {
	result := StringToInt64("123w")
	assert.Equal(t, result, int64(0))
}
