package http

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"io"
	"net/http"
	"time"

	log "github.com/sirupsen/logrus"
)

const (
	errorMsgMethodHttpNotAllowed = "method is not allowed"
)

type Requestor struct {
	HttpMethod string
	MaxRetry   int
	Backoff    time.Duration
	TTLTimeOut time.Duration
	URL        string
	Body       any
	Headers    http.Header
	Context    context.Context
}

func DoRequest[T any](request Requestor, response *T) (int, error) {
	var correlationID string

	client := &http.Client{Timeout: request.TTLTimeOut}

	if request.Context != nil {
		if request.Context.Value(enums.HeaderCorrelationID) != nil {
			correlationID = request.Context.Value(enums.HeaderCorrelationID).(string)
		} else {
			correlationID = ""
		}

	}

	req, err := configRequest(request)
	if err != nil {
		log.Printf("[%s] error client: %s", correlationID, err.Error())
		return http.StatusUnprocessableEntity, err
	}

	resp, err := executeWithRetry(req, request, client)
	if err != nil {
		log.Printf("[%s] error client: %s", correlationID, err.Error())
		if resp != nil {
			return resp.StatusCode, err
		} else {
			return http.StatusInternalServerError, err
		}

	}

	return resp.StatusCode, handleResponse(resp, response, correlationID)
}

func handleResponse[T any](resp *http.Response, response *T, correlationID string) error {
	log.Printf("[%s] client: got response!", correlationID)
	log.Printf("[%s] client: status code: %d", correlationID, resp.StatusCode)

	resBody, err := io.ReadAll(resp.Body)
	if err != nil {
		log.Printf("[%s] client: could not read response body: %s", correlationID, err)
		return err
	}

	if resp.StatusCode < http.StatusOK || resp.StatusCode > http.StatusMultipleChoices {
		return errors.New(string(resBody))
	}

	if resp.StatusCode == http.StatusNoContent && len(resBody) == 0 {
		return nil
	}

	if err = json.Unmarshal(resBody, response); err != nil {
		log.Printf("[%s] failed to unmarshal JSON: %v", correlationID, err)
		return err
	}

	_ = resp.Body.Close()

	return nil
}

func configRequest(request Requestor) (*http.Request, error) {
	var req *http.Request
	var bodyBuffer bytes.Buffer
	var err error

	if request.Body != nil {
		err = json.NewEncoder(&bodyBuffer).Encode(request.Body)
		if err != nil {
			log.Printf("client: could not convert body to bytes.Buffer : %s", err)
			return nil, err
		}
	}

	req, err = setHttpMethod(request, bodyBuffer)
	if err != nil {
		log.Printf("client: could not create request: %s", err)
		return nil, err
	}

	req.Header = request.Headers
	req.Host = request.Headers.Get("Host")

	return req, nil
}

func setHttpMethod(request Requestor, bodyBuffer bytes.Buffer) (*http.Request, error) {
	switch request.HttpMethod {
	case http.MethodGet:
		return getNewRequest(request, &bytes.Buffer{})
	case http.MethodPost:
		return getNewRequest(request, &bodyBuffer)
	case http.MethodPut:
		return getNewRequest(request, &bodyBuffer)
	case http.MethodPatch:
		return getNewRequest(request, &bodyBuffer)
	default:
		log.Printf(errorMsgMethodHttpNotAllowed)
		return nil, errors.New(errorMsgMethodHttpNotAllowed)
	}
}

func getNewRequest(request Requestor, bodyBuffer *bytes.Buffer) (*http.Request, error) {
	if request.Context != nil {
		return http.NewRequestWithContext(request.Context, request.HttpMethod, request.URL, bodyBuffer)
	} else {
		return http.NewRequest(request.HttpMethod, request.URL, bodyBuffer)
	}
}

func shouldRetry(resp *http.Response) bool {
	if resp == nil || resp.StatusCode == http.StatusRequestTimeout ||
		resp.StatusCode == http.StatusServiceUnavailable ||
		resp.StatusCode == http.StatusInternalServerError ||
		resp.StatusCode == http.StatusBadGateway {
		return true
	}
	return false
}

func executeWithRetry(req *http.Request, request Requestor, client *http.Client) (*http.Response, error) {
	var err error
	var resp *http.Response

	for retry := 0; retry <= request.MaxRetry; retry++ {
		if retry > 0 {
			time.Sleep(request.Backoff)
		}

		resp, err = client.Do(req)
		if err != nil {
			if request.MaxRetry == 0 || retry == request.MaxRetry {
				log.Printf("client: error making http request: %s", err)
				return resp, err
			}

			if shouldRetry(resp) {
				continue
			} else {
				log.Printf("client: error making http request: %s", err)
				return resp, err
			}
		} else {
			break
		}
	}

	return resp, err
}
