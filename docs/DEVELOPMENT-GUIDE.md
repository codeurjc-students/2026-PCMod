# 🖥️ Development Guide

## 📄 Table of contents
* [Introduction](#-introduction)
* [Technologies](#️-technologies)
* [Tools](#️-tools)
* [Architecture](#️-architecture)
* [Quality Control](#-quality-control)
* [Development Process](#-development-process)
* [Application Execution and Code Edition](#-application-execution-and-code-edition)

## 🗒️ Introduction

This project is based on a Single-Page Application (SPA) architecture, in which the interface only updates the components that change, rather than reloading the entire page. This makes the web application faster and improves the user experience. The server exposes a REST API from which the frontend retrieves the data to display.

PCMod follows a Client-Server model, clearly separating the frontend, backend, and database:

* Client (Frontend): A React (TypeScript) SPA that communicates with the server's REST API.

* Server (Backend): A Spring Boot (Java) application that provides the REST API. It isolates controllers, services, and repositories. It uses Spring Security, JWT and Spring Data JPA.

* Database: A MySQL database managed by the server via Spring Data JPA.

This project includes a full suite of unit, integration, and system tests for both frontend and backend.

The REST API is documented using the OpenAPI Specification and Swagger UI.

CI/CD workflows are configured via GitHub Actions to run tests and perform SonarCloud analysis.

### Summary

| Element | Description |
|---------|-------------|
| Architecture | Web SPA with REST API |
| Technologies | React 19, React Router 8, TypeScript, Java 25, Spring Boot 4, Spring Security (JWT), Spring Data JPA, MapStruct, MySQL 8+|
| Tools | VS Code, Postman, MySQL Workbench, Maven 3.9+, Node.js 24.X.X+, npm 11.X.X+, Vite, OpenAPI Specification, Swagger UI |
| Quality Control | JUnit, Mockito, Testcontainers, REST Assured, Selenium, Vitest, JSDOM, SonarCloud, JaCoCo |
| Deployment | - |
| Development Process |  Iterative and incremental using Git, GitHub, GitHub Flow, GitHub Projects, and GitHub Actions |

## ⚙️ Technologies

This section describes the technologies powering the PCMod application.

### 🖥️ Frontend:

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg" width="20"/> React

  React is a library for building dynamic user interfaces. It bases interfaces on components instead of pages, and only updates the parts of the DOM that have changed. It was chosen because it enables building a SPA and integrates seamlessly with Spring Boot.

  [React Official Site](https://react.dev/)

* ### <img src="https://cdn.simpleicons.org/reactrouter/CA4245" width="20"/> React Router

  React Router is a React-based routing framework that enables routing in a SPA. It manages data loading, generates the layout, and integrates with Vite for hot reloading.

  [React Router Official Site](https://reactrouter.com/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg" width="20"/> TypeScript

  TypeScript is a typed superset of JavaScript. It was chosen to implement the frontend along with React Router.
  
  [TypeScript Official Site](https://www.typescriptlang.org/)

### ⚙️ Backend:

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="20"/> Java

  Java 25 (LTS) is the selected programming language for implementing the backend of PCMod. It provides an object-oriented foundation, strong typing, and JVM-based platform independence, offering high performance and security for the system's core business logic.
  
  [Java Official Site](https://www.java.com/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="20"/> Spring Boot

  Spring Boot is a Spring-based framework that simplifies web application development. It includes an embedded web server, makes configuration easier, and accelerates development.

  [Spring Boot Official Site](https://spring.io/projects/spring-boot)

* ### <img src="https://spring.io/img/projects/spring-security.svg" width="15"/>  Spring Security + JWT

  Spring Security is a framework that provides authentication and authorization for Spring-based applications. It uses JWTs for stateless sessions and offers HTTPS support.

  [Spring Security Official Site](https://spring.io/projects/spring-security)

* ### <img src="https://spring.io/img/projects/spring-data.svg" width="15"/> Spring Data JPA

  Spring Data JPA is part of the Spring ecosystem and enables the implementation of a data access layer by simplifying queries and providing supporting pagination.

  [Spring Data JPA Official Site](https://spring.io/projects/spring-data-jpa)

* ### <img src="https://www.pngkit.com/png/detail/223-2231944_mapstruct-logo.png" width="30"/> MapStruct

  MapStruct is an automatic code generator that simplifies the mapping between Java classes and DTOs.

  [MapStruct Official Site](https://mapstruct.org/)

### 🗄️ Database:

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" width="20"/> MySQL

  MySQL is the open-source relational database management system chosen to store PCMod's data. It use benefits the application through Spring Data JPA mapping while ensuring data integrity and fast queries.

  [MySQL Official Site](https://www.mysql.com/)

## 🛠️ Tools

This section describes the tools used to develop the PCMod application.

### 🧑‍💻 Code Development:

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vscode/vscode-original.svg" width="20"/> VS Code

  VS Code is the code editor chosen for developing this project. It supports multiple programming languages, such as Java (Backend) and TypeScript (Frontend), and Git integration. It also offers a large collection of extensions that simplifies the development process.

  [VS Code Official Site](https://code.visualstudio.com/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postman/postman-original.svg" width="20"/> Postman

  Postman is the platform used to design, test and document PCMod's REST API. It allows sending requests to the application server and receiving the corresponding responses, as well as exporting the REST API endpoint collection.

  [Postman Official Site](https://www.postman.com/)

* ### <img src="https://icons.iconarchive.com/icons/papirus-team/papirus-apps/128/mysql-workbench-icon.png" width="20"/> MySQL Workbench

  MySQL Workbench is a visual tool that simplifies MySQL database management and querying while allowing users to create and visualize database diagrams.

  [MySQL Workbench Official Site](https://www.mysql.com/products/workbench/)

### 📦 Build and Package Management:

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/maven/maven-original.svg" width="20"/> Maven

  Maven is a build automation tool for Java projects. It is used to compile code, manage dependencies, run tests, and generate distribution files.

  [Maven Official Site](https://maven.apache.org/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/nodejs/nodejs-original.svg" width="20"/> Node.js

  Node.js is a runtime environment that allows executing JavaScript outside the browser.

  [Node.js Official Site](https://nodejs.org/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/npm/npm-original.svg" width="20"/> npm

  npm is the package manager for Node.js. It allows installing, managing, updating, and sharing JavaScript packages and dependencies.

  [npm Official Site](https://www.npmjs.com/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/vitejs/vitejs-original.svg" width="20"/> Vite

  Vite is a fast frontend build tool. It simplifies the configuration of React projects, handles paths resolution, runs the local development server, and enables instant hot module reloading in the browser.

  [Vite Official Site](https://vite.dev/)

### 📁 Version Control and Repository:

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/git/git-original.svg" width="20"/> Git

  Git is the distributed version control system selected for this project. It is used to track, manage, and collaborate on code changes throughout development.

  [Git Official Site](https://git-scm.com/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/github/github-original.svg" width="20"/> GitHub

  GitHub is the platform chosen to host this project's Git repository. It provides an intuitive interface for creating, reviewing and merging pull requests, as well as managing and publishing project releases.

  [GitHub Official Site](https://github.com/)

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/github/github-original.svg" width="20"/> GitHub Projects

  GitHub Projects is a project management tool integrated with GitHub for tracking issues. In this project, it is used as a Kanban board with three stages: To Do, In Progress, and Done.

  [GitHub Projects Official Site](https://docs.github.com/es/issues/planning-and-tracking-with-projects/learning-about-projects/about-projects)

### 🧪 Testing:

* ### <img src="https://junit.org/assets/img/junit-logo.svg" width="50"/> JUnit

  JUnit 6 is a testing framework for Java code. In this project, it is used for backend unit testing.

  [JUnit Official Site](https://junit.org/)

* ### <img src="https://raw.githubusercontent.com/mockito/mockito/main/config/javadoc/resources/org/mockito/logo@2x.png" width="40"/> Mockito

  Mockito is a Java mocking framework used to create stubs, mocks, and spies for unit testing. In PCMod, it is used to isolate database-related dependencies in backend unit tests.

  [Mockito Official Site](https://site.mockito.org/)

* ### <img src="https://testcontainers.com/images/testcontainers-logo.svg" width="50"/> Testcontainers

  Testcontainers is an open-source library used to create and manage Docker containers during tests. PCMod uses it to run a MySQL container during backend integration tests.

  [Testcontainers Official Site](https://testcontainers.com/)

* ### <img src="https://rest-assured.io/img/logo-transparent.png" width="20"/> REST Assured

  REST Assured is a library that simplifies testing REST APIs. This dependency is used for backend system tests.

  [REST Assured Official Site](https://rest-assured.io/)

* ### <img src="https://cdn.simpleicons.org/selenium/43B02A" width="20"/> Selenium

  Selenium is a framework used to automate browser-based testing. It is used to verify frontend functionality during system end-to-end tests.

  [Selenium Official Site](https://www.selenium.dev/)

* ### <img src="https://cdn.jsdelivr.net/gh/callback-io/allogo@main/public/logos/vitest/icon.svg" width="20"/> Vitest

  Vitest is a testing framework for JavaScript and TypeScript projects, designed to implement unit, integration, and component tests. It offers fast execution and Vite integration. PCMod uses it for frontend unit and integration testing.

  [Vitest Official Site](https://vitest.dev/)

* ### <img src="https://raw.githubusercontent.com/jsdom/jsdom/main/logo.svg" width="20"/> JSDOM

  JSDOM is a JavaScript-based tool for Node.js that emulates a browser environment without a graphical interface. It allows manipulating and testing HTML documents.

  [JSDOM Official Site](https://github.com/jsdom/jsdom)

### ✅ CI/CD and Quality Reports:

* ### <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/githubactions/githubactions-original.svg" width="20"/> GitHub Actions

  GitHub Actions is a tool that allows developers to run automated workflows, which can include build, test, and deployment jobs. In this project, it is also used to run jobs that analyze code quality and test coverage, and upload the results to SonarCloud.

  [GitHub Actions Official Site](https://github.com/features/actions)

* ### <img src="https://cdn.simpleicons.org/sonarqubecloud" width="20"/> SonarCloud

  SonarCloud is a cloud-based static code analysis tool used to detect code smells, security vulnerabilities, and maintainability issues, as well as to track test coverage.

  [SonarCloud Official Site](https://www.sonarsource.com/products/sonarqube/cloud/)

* ### <img src="https://cdn.jsdelivr.net/gh/jacoco/jacoco@master/org.jacoco.doc/docroot/doc/resources/report.gif" width="20"/> JaCoCo

  JaCoCo is a Java code coverage tool used to measure how many lines of code, methods, branches, and classes are covered by tests. In PCMod, it generates coverage reports during backend testing to be uploaded to SonarCloud.

  [JaCoCo Official Site](https://www.jacoco.org/jacoco/)

### 📄 API Documentation:

* ### <img src="https://cdn.jsdelivr.net/gh/OAI/OpenAPI-Style-Guide@main/graphics/vector/OpenAPI_Specification_Logo_Pantone.svg" width="40"/> OpenAPI Specification

  OpenAPI Specification is a standard for describing and documenting REST APIs. It defines endpoints, request and response formats, parameters, authentication methods, and other contract details.

  [OpenAPI Specification Official Site](https://www.openapis.org/)

* ### <img src="https://cdn.simpleicons.org/swagger" width="20"/> Swagger UI

  Swagger UI is a tool that allows users to visualize and interact with APIs based on their OpenAPI specification.

  [Swagger UI Official Site](https://swagger.io/open-source/swagger-ui/)

## 🏗️ Architecture

### ⚙️ Deployment

PCMod is divided into three processes:

* Client (Frontend): A React SPA available on port 5173 that communicates with the REST API through HTTP requests. It allows users to interact with the application.
* Server (Backend): A Spring Boot REST API available on port 443 that communicates with the frontend via HTTP and with the database via TCP. It contains the business logic and operate with the database.
* Database: A MySQL database available on port 3306, that communicates with the backend via TCP. It is a relational database that ensures data integrity and persistence.

Communication between the frontend and backend is carried out using JSON format.

### 🌐 REST API

The REST API is documented using the OpenAPI Specification and Swagger UI. It can be accessed at [PCMod API Documentation](https://raw.githack.com/codeurjc-students/2026-PCMod/main/docs/api/api-docs.html).

## ✅ Quality Control

In this project, there is a complete test suite for both the frontend and backend.

### 🖥️ Frontend Tests

#### Frontend Unit Tests

Frontend unit tests are implemented with Vitest.

* `renders all components`: Checks if calling the getComponents method in components-service returns the components from the database (mocked with sample data).

#### Frontend Integration Tests

Frontend integration tests are implemented with Vitest.

* `check communication between frontend and backend`: Checks the rendering of the list of PC components by calling the REST API.

#### Frontend System Tests

Frontend system tests are implemented with Selenium and JUnit.

* `loadComponentsTest`: Verifies that when opening the web application in a browser, the list of PC components loads correctly from the REST API.

### ⚙️ Backend Tests

#### Backend Unit 

Backend unit tests are implemented with JUnit and Mockito.

* `testComponentsLoad`: Verifies that ComponentsService correctly retrieves the list of PC components from the ComponentsRepository (which is mocked).

#### Backend Integration Tests

Backend integration tests are implemented with JUnit and Testcontainers.

* `loadComponents`: Verifies that findAll method in ComponentService returns the correct PC components values when using a real database container.

#### Backend System Tests

Backend system tests are implemented with JUnit and REST Assured.

* `getComponents`: Verifies that the REST API response when requesting "/components/" matches the expected values previously loaded into the database. 

---

 A summary of these tests can be found in the table below:

| Test | Quantity | Technologies and tools |
|------|:--------:|------------------------|
| Frontend Unit Tests | 1 | Vitest |
| Frontend Integration Tests | 1 | Vitest |
| Frontend System Tests | 1 | Selenium, JUnit |
| Backend Unit Tests | 1 | JUnit, Mockito |
| Backend Integration Tests | 1 | JUnit, Testcontainers |
| Backend System Tests | 1 | JUnit, REST Assured |

The images below are the results of executing the complete test suite with the coverage reports:

* Frontend Tests:

  ![Frontend Tests Execution and Coverage](/docs/img/testing/frontend_tests_execution_and_coverage.png)

  ![Frontend System Tests Execution](/docs/img/testing/frontend_system_tests_execution.png)

  **Note:** Frontend system tests are executed from the backend folder.

* Backend Tests:

  ![Backend Tests Execution](/docs/img/testing/backend_tests_execution.png)

  ![Backend Tests Coverage](/docs/img/testing/backend_tests_coverage.png)

---

### ☁️ SonarCloud Analysis

To execute a quality analysis with SonarCloud, the GitHub Actions workflows generate JaCoCo and a LCOV reports after passing the tests.

The metrics reported by SonarCloud after merging all feature and test branches into main are shown below:

![Sonar Analysis Results](/docs/img/sonar_results.png)

The analysis done has reviewed the quality of:
* Lines of code: 486 (Java: 326 | TypeScript: 147)
* Functions: 38 (Backend: 27 | Frontend: 11)
* Classes: 11
* Files: 18 (Backend: 11 | Frontend: 7)

## 🧑‍💻 Development Process

This project is based on an iterative and incremental process, following the Agile Manifesto principles and good practices of Extreme Programming (XP).

### 📝 Issues Management

GitHub Issues and GitHub Projects are used to manage issues, using a Kanban board with three columns: To Do, In Progress, and Done. Issues are classified into "Task", "Feature", and "Documentation". Once a feature and its corresponding tests are completed, the issue can be moved to the Done column.

![Project Kanban Board](/docs/img/kanban_github_projects.png)

### 🗂️ Repository and Version Control System

As a version control system, PCMod uses Git to track and manage changes. The Git repository is hosted on GitHub to take advantage of its features (Github Issues, GitHub Projects, GitHub Actions).

Development follows GitHub Flow strategy, using the branches main and feature/fix:

* `main`: Stable branch, ready for deployment.
* `feature/*`: Branch for new functionalities. It is merged into main once the feature is completed and all tests pass.
* `fix/*`: Branch for bug fixes. It is merged to main once the bug is resolved and all corresponding tests pass.

Metrics:

| Element | Metric |
|---------|:--------:|
| Commits | ~30 |
| Branches | 7 |
| Issues | 22 |

### 🔄 Continuous Integration (CI)

In the PCMod repository, two CI/CD workflows are active:

* Basic Workflow: Triggered on every push to a feature or fix branch.

  * Frontend unit tests.
  * Backend unit tests.
  * Sonar Cloud branch analysis.

* Complete Workflow: Triggered when a pull request to main is opened, reopened, or synchronized. It blocks merging into main if any test fails.

  * Backend unit tests.
  * Backend integration tests.
  * Backend system tests.
  * Frontend unit tests.
  * Frontend integration tests.
  * Frontend system tests.
  * Sonar Cloud analysis.

Both workflows contain the workflow_dispatch trigger, allowing the developer to run them manually without pushing commits or opening a PR.

Additionally, these workflows ignore changes in .md files or the /docs directory.

## 🌐 Application Execution and Code Edition

This section provides the steps to clone the repository, run the PCMod application and execute the full test suite locally.

### ⬇️ Clone the repository

Requirements:
* Git

You can check if Git is installed by running the following command in a terminal:
```
git --version
```
If the output shows a message like `git version X.XX.X`, Git is installed. 
If not, please download and install it from  [Git Official Installation Site](https://git-scm.com/install/)

Once Git is installed, clone the repository by running the following command in a terminal:

```
git clone https://github.com/codeurjc-students/2026-PCMod.git
```

The application can be run in two different modes: development and production.

### 🧑‍💻  Run in development mode

#### ⚙️ Backend and database

Requirements:

* MySQL Server 8.0+
* Java 25
* Maven 3.9+
* Docker

Verify that Java and Maven are installed by running the following commands in a terminal:
```
java -version
mvn -version
```
If the output shows `java version 25.X.X` for Java and `Apache Maven 3.9.X` for Maven, they are installed. 
If not, please download Java from  [Java Official Site](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html) and Maven from [Maven Official Site](https://maven.apache.org/download.cgi).

To run the database, ensure that Docker Engine is installed and running. You can check this with:
```
docker --version
docker info
```
If the first command returns an error, please download and install Docker from  [Docker Official Site](https://www.docker.com/products/docker-desktop/). If the second command returns an error, make sure the Docker daemon is running.

Once Docker is installed and running, execute:
```
docker run --rm -e MYSQL_ROOT_PASSWORD=password \
-e MYSQL_DATABASE=pcmod -p 3306:3306 -d mysql:8.0
```

Then, open a terminal in the cloned repository's directory and run the following commands to start the backend:
```
cd backend/pcmod
mvn spring-boot:run
```

#### 🖥️ Frontend

Requirements:

* Node.js 24.X.X+
* npm 11.X.X+

You can check if Node.js and npm are installed with:
```
node -v
npm -v
```
If the output displays `v.24.XX.X` for Node.js and `11.XX.X` for npm, they are installed. 
If not, please download both from  [Node + npm Official Site](https://nodejs.org/en/download).

Open a terminal on the cloned repository's directory and execute:
```
cd frontend
npm install
npm run dev
```

Finally, open a browser and navigate to https://localhost:5173/.

#### 🛑 Stop application

To stop running PCMod, terminate the terminal tasks or press `Ctrl + C` in both backend and frontend terminals.

To stop the MySQL Docker container execute in a terminal:
```
docker ps # Locate the CONTAINER ID for MySQL
docker stop <CONTAINER ID>
```

### 🌐 Run in production mode

#### ⚙️ Build and run

Requirements:

* MySQL Server 8.0+
* Java 25
* Maven 3.9+
* Node.js 24.X.X+
* npm 11.X.X+
* Docker

Verify that Java and Maven are installed by running the following commands in a terminal:
```
java -version
mvn -version
```
If the output shows `java version 25.X.X` for Java and `Apache Maven 3.9.X` for Maven, they are installed.
If not, please download Java from  [Java Official Site](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html) and Maven from [Maven Official Site](https://maven.apache.org/download.cgi).

Verify that Node.js and npm are installed with:
```
node -v
npm -v
```
If the output displays `v.24.XX.X` for Node.js and `11.XX.X` for npm, they are installed. 
If not, please download both from  [Node + npm Official Site](https://nodejs.org/en/download).

Open a terminal in the cloned repository's directory and execute the following commands to build the frontend:
```
cd frontend
npm install
npm run build
```
Then, copy the files generated in `/frontend/build/client/` to `/backend/pcmod/src/main/resources/static/`.

To run the database, ensure that Docker Engine is installed and running. You can check this with:
```
docker --version
docker info
```
If the first command returns an error, please download and install Docker from  [Docker Official Site](https://www.docker.com/products/docker-desktop/). If the second command returns an error, make sure the Docker daemon is running.

Once Docker is installed and running, execute:
```
docker run --rm -e MYSQL_ROOT_PASSWORD=password \
-e MYSQL_DATABASE=pcmod -p 3306:3306 -d mysql:8.0
```

Finally, in a terminal on the cloned repository's directory, execute the following commands to run the application:
```
cd backend/pcmod
mvn spring-boot:run
```

Open a browser and navigate to https://localhost:443/.

#### 🛑 Stop application

To stop running PCMod, terminate the terminal task or press `Ctrl + C` in the backend terminal.

To stop the MySQL Docker container execute in a terminal:
```
docker ps # Locate the CONTAINER ID for MySQL
docker stop <CONTAINER ID>
```

### 🛠️ Development tools

#### Code

To view and edit the code, use VS Code: [VS Official Site](https://code.visualstudio.com/Download). 

It is recommended to install "Extension Pack for Java" and "Spring Boot Extension Pack" extensions in VS Code.

If you have already cloned the repository, open the folder where it is saved. 
If not, execute in a terminal:
```
git clone https://github.com/codeurjc-students/2026-PCMod.git
```
The project is organized as follows:

* `/backend/pcmod`: Contains the source files for the REST API (server side). 

* `/frontend`: Contains the source files for the client side.

* `/docs`: Contains REST API specifications and project documentation.

* `/.github`: Contains GitHub Actions workflows files.

You can navigate and open source files using the file explorer in the left sidebar.

#### REST API

To test and interact with the REST API, download Postman from [Postman Official Site](https://www.postman.com/downloads/) if you haven't already.

First, navigate to the backend directory in your terminal and start the server:
```
cd backend/pcmod
mvn spring-boot:run
```

Then, import the Postman collection included in the repository (File > Import):
[Postman PCMod Collection](/docs/api/postman/PCMod.postman_collection.json)

Select an endpoint from the left sidebar. You can customize request parameters in the "Params" tab. Click the "Send" button in the top right corner to view the server response.

### 🧪 Run Tests

> [!NOTE]
> Before executing tests, execute the following commands in a terminal from the cloned repository's directory to make the Maven wrapper executable:
> ```
> cd backend/pcmod
> chmod +x mvnw
> ```

To run all backend tests:
> [!NOTE]
> Before executing these commands, verify that the Docker daemon is running.
```
# Unit + Integration + System

cd backend/pcmod
./mvnw verify '-Dtest=!es.codeurjcstudents.pcmod.e2e.ui.**'
```

To run all frontend tests:
```
# Unit + Integration

cd frontend
npm install
cd ../backend/pcmod
./mvnw spring-boot:run > /dev/null 2>&1 < /dev/null &          
npx wait-on https-get://localhost:443/api/v1/components/          
cd ../../frontend
npm run test
```
```
# System (requires both frontend and backend to be running)

cd frontend
npm run dev > /dev/null 2>&1 < /dev/null &
cd ../backend/pcmod
./mvnw spring-boot:run > /dev/null 2>&1 < /dev/null &     
npx wait-on http-get://localhost:5173 https-get://localhost:443/api/v1/components/
./mvnw test -Dgroups="client-system"
```

#### Backend Unit Tests

From the root directory of the cloned repository:
```
cd backend/pcmod
./mvnw clean test -Dgroups="server-unit"
```

#### Backend Integration Tests

To run integration tests, ensure Docker Engine is installed and running:
```
docker --version
docker info
```
If the first command returns an error, please download and install Docker from  [Docker Official Site](https://www.docker.com/products/docker-desktop/). If the second command returns an error, make sure the Docker daemon is running.

From the root directory of the cloned repository:
```
cd backend/pcmod
./mvnw clean test -Dgroups="server-integration"
```

#### Backend System Tests

From the root directory of the cloned repository:
```
cd backend/pcmod
./mvnw clean test -Dgroups="server-system"
```

#### Frontend Unit Tests

From the root directory of the cloned repository:
```
cd frontend
npm run test:unit
```

#### Frontend Integration Tests

> [!NOTE]
> The backend must be running before executing integration tests.

First, start the backend:
```
cd backend/pcmod
./mvnw spring-boot:run
```
In a new terminal, run:
```
cd frontend
npm run test:integration
```

#### Frontend System Tests

> [!NOTE]
> The backend and the frontend must be running before executing system tests.

First, start the frontend:
```
cd frontend
npm run dev
```
In a second terminal, start the backend:
```
cd backend/pcmod
./mvnw spring-boot:run
```
In a third terminal, run the system tests:
```
cd backend/pcmod
./mvnw test -Dgroups="client-system"
```

### 📦 Publish a Release

To publish a release you must follow these steps:

1. Ensure all tests pass.
2. Verify that the SonarCloud quality gate passes.
3. On GitHub, navigate to "Releases" and click "Draft a new release"
4. Specify the release title, description, and tag version, and click "Publish release".
5. Update the version of the app in pom.xml, package.json, and docker-compose.yml.