package mocks_server

import (
	"net/http"
	"net/http/httptest"
	neturl "net/url"
)

func ConfigMockSrver(response string, url string, httpMethod string, statusResponse int, responseError string) *httptest.Server {
	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		parsedURL, err := neturl.Parse(url)

		if err == nil && r.URL.Path == parsedURL.Path && r.Method == httpMethod {
			w.WriteHeader(statusResponse)
			_ , err = w.Write([]byte(response))
			if err != nil {
				return
			}
		} else {
			w.WriteHeader(http.StatusNotFound)
			_, err = w.Write([]byte(responseError))
			if err != nil {
				return
			}
		}
	})

	return httptest.NewServer(handler)
}
