package response

import (
	"encoding/json"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestOk(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	data := map[string]string{"foo": "bar"}
	Ok(c, data)

	assert.Equal(t, http.StatusOK, w.Code)

	var resp genericResponse
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "OK", resp.Code)
	assert.Equal(t, "Success", resp.Message)
}

func TestConflictError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	ConflictError(c, "error")

	assert.Equal(t, http.StatusConflict, w.Code)

	var resp genericResponse
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "Conflict", resp.Code)
	assert.Equal(t, "error", resp.Message)
}

func TestBadRequest(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	BadRequest(c, "param error")

	assert.Equal(t, http.StatusBadRequest, w.Code)

	var resp genericResponse
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "Bad Request", resp.Code)
	assert.Equal(t, "param error", resp.Message)
	assert.Nil(t, resp.Data)
}

func TestServerError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	ServerError(c, "internal error")

	assert.Equal(t, http.StatusInternalServerError, w.Code)

	var resp genericResponse
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "Internal Server Error", resp.Code)
	assert.Equal(t, "internal error", resp.Message)
	assert.Nil(t, resp.Data)
}
