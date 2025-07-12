# App Engine & Java

## Requirements
- Java 17
- Gradle 7.6.4

## Build with Gradle

### Building the Project

To clean the project:

```bash
gradle clean
```

To build the project, without tests:

```bash
gradle clean build
```

### Deployment

#### Important Files by Environment

- `build.gradle`
- `main/webapp/WEB-INF/appengine-web.xml`
- `main/webapp/WEB-INF/web.xml`
- `com.imaginamos.farmatodo.model.util.URLConnections`

### Compiling and Deploying for Development Environment

```bash
gradle clean build -Pprofile=dev
```

### Compiling and Deploying for Production Environment

```bash
gradle clean build -Pprofile=prod
```

### Deploying the Project on App Engine: [contingency@venezuela]

**WARNING:** Ensure you are in the module directory.

```bash
gradle appengineDeploy
```

### Commands to Deploy the Application Version on App Engine

Compile and clean the entire project:

```bash
gradle clean build -Pprofile=prod
```

Compile and clean only the backend module:

```bash
gradle clean build -Pprofile=prod -p backend
```

Deploy on App Engine:

```bash
gradle -Pprofile=prod -p backend -PappengineVersion=version1 -PminInstances=0 -PappengineService=qa-env appengineDeploy
```

#### Parameters to Consider:
- `minInstances`: Minimum number of instances, default 1 for the production environment.
- `appengineVersion`: Version of the application (e.g., `qa-env`), default for the production environment.
- `appengineService`: Name of the service in App Engine, should correspond to the feature/RFC being deployed.
- `pathGoogleCloudSdk`: Path to the Google Cloud SDK. (Optional, only if not set in environment variables)
  - Example: `-PpathGoogleCloudSdk=/Users/usuariox/google-cloud-sdk`
- `profile`: Build profile, either `dev` or `prod`.
- `-p backend`: Indicates the backend module is being deployed.

To skip tests, add `-x test` at the end of the command.