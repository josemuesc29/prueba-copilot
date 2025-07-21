package main

import (
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/cmd/dig"
	"ftd-td-home-read-services/cmd/router"
	"log"
)

func main() {
	container := dig.BuildContainer()

	config.LoadEnviroments()

	err := container.Invoke(func(r *router.Router) {
		router := router.SetupRouter(r)

		if err := router.Run(":8080"); err != nil {
			log.Fatalf("Failed to start the server: %v", err)
		}
	})

	if err != nil {
		log.Fatalf("Failed to resolve dependencies: %v", err)
	}
}
