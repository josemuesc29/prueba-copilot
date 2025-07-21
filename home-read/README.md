# ftd-td-home-read-services

in charge of read information(purchases, structure, banners, recently viewed, offers, etc) that use in home.

## Prerequisites

- Go (version 1.24.1 or higher)

## Installation

1. Clone the repository:
2. Install the dependencies:
   ```bash
   go mod tidy
   ```
3. Create a `.env` file in the root directory and add the following environment variables:
   ```bash
    BASE_URL=your_base_url
   ```
4. Run the application:
   ```bash
   go run cmd/main.go
   ```

## Run locally

```bash
go run cmd/main.go
```

## Curl de prueba

```
curl --location 'http://localhost:8080/home/r/AR/v1/health'
```

## Run tests

```bash
go test ./...
```

Execute test with coverage

```bash
go test ./... -coverprofile=coverage.out && go tool cover -html=coverage.out
```

# Steps to generate mocks for the tests

## Tests

### 1. Install GoMock on your system

```bash
go install go.uber.org/mock/mockgen@latest
```

### 2. Genetate mocks with GoMock

1. #### Add `go:generate` directive to the interface file

   In the interface file, add the following line at the top of the file, modifying the relative paths based on the location of your interface file and the desired destination for the test layer.

   ```go
   //go:generate mockgen -source=customer.go -destination=../../../../../test/mocks/domain/ports/out/customer_mock.go
   ```

2. #### Generate the mocks

   Then, run the following command from the root directory of project to generate all mocks defined with `go:generate`:

   ```bash
   go generate ./...
   ```

## Deployment

### Submit docker image to Artifac registry GCP

```bash

gcloud builds submit --tag us-central1-docker.pkg.dev/devops-farmatodo/farmatodo/ftd-td-home-read-services:{versionTag}
```
