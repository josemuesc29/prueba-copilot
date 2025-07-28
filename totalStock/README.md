# App Engine Standard & Spring Boot Frameworks & Java

## Build with Maven

### Building the project

To build the project:

    mvn clean install

To build the project, without tests:

    mvn clean package -DskipTests

To build the project, without tests, whit a specific profile:

    mvn clean package -DskipTests -Dspring-boot.run.profiles=develop
    
### Local - Spring Boot configurations

Para ejecutar el proyecto localmente es necesario autenticarse ante GCLOUD, para poder acceder al Datastore desde el ambiente local:

    gcloud beta auth application-default login

Ejecutar el proyecto localmente:

    mvn spring-boot:run
    mvn spring-boot:run -Dspring-boot.run.profiles=develop

### App Engine - Spring Boot configurations

    -Dapp.deploy.project=[--project]
    -Dapp.deploy.version=[--version]
    -Dapp.deploy.promote=[true|false]
    -Dapp.deploy.stopPreviousVersion=[true|false]
    -Dapp.stage.appEngineDirectory=[--appEngineDirectory]
    -Dspring-boot.run.profiles=[--profile]

####Desplegar el proyecto en App Engine: [develop]

    mvn appengine:deploy -Dapp.stage.appEngineDirectory=src/main/appengine/develop -Dapp.deploy.projectId=dev-domicilios-farmatodo -Dspring-boot.run.profiles=develop -Dapp.deploy.version=[--version]

####Desplegar el proyecto en App Engine: [sandbox]

    mvn appengine:deploy -Dapp.stage.appEngineDirectory=src/main/appengine/sandbox -Dapp.deploy.projectId=sandbox-domicilios-farmatodo -Dspring-boot.run.profiles=sandbox -Dapp.deploy.version=[--version]

####Desplegar el proyecto en App Engine: [production]

    mvn appengine:deploy -Dapp.stage.appEngineDirectory=src/main/appengine/production -Dapp.deploy.projectId=stunning-base-164402 -Dspring-boot.run.profiles=production -Dapp.deploy.version=[--version]

### Creating a Docker container

##### 1. Check the last image and delete it:

    docker images
    docker rmi ftd-services-shopping-cart

##### 2. Build the container image and save it to tar.gz:

    docker build -t ftd-services-shopping-cart:latest .
    docker save ftd-services-shopping-cart | gzip > ftd-services-shopping-cart.tar.gz

### Installing a Docker container in a remote server

##### 1. Stop and delete the old container:

    docker ps -a
    docker stop ftd-services-shopping-cart
    docker rm ftd-services-shopping-cart

##### 2. Delete the container image:

    docker images
    docker rmi ftd-services-shopping-cart

##### 3. Load the image to the docker server:

    zcat ftd-services-shopping-cart.tar.gz | docker load  

##### 4. Run the container using docker:

    docker run -p 8080:8080 ftd-services-shopping-cart:latest

##### 5. Submit image to Container Registry [https://cloud.google.com/sdk/gcloud/reference/builds/submit]

    gcloud builds submit --tag gcr.io/sandbox-domicilios-farmatodo/ftd-services-shopping-cart
    gcloud builds submit --tag us-central1-docker.pkg.dev/devops-farmatodo/farmatodo-colombia-tradicional/ftd-services-shopping-cart

##### 6. Run image in cloud run [https://cloud.google.com/sdk/gcloud/reference/run/deploy]

    gcloud run deploy ftd-services-shopping-cart --image=gcr.io/sandbox-domicilios-farmatodo/ftd-services-shopping-cart --platform=managed --region=us-central1 --allow-unauthenticated --max-instance=2 --no-traffic 
    gcloud run deploy ftd-services-shopping-cart --image=gcr.io/stunning-base-164402/ftd-services-shopping-cart --platform=managed --region=us-central1 --allow-unauthenticated --max-instance=2 --no-traffic
