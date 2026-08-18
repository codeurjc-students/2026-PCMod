# 🎯 Objectives

This section outlines the functional and technical objectives to be achieved during the development of the PCMod application.

## Functional Objectives

The primary functional objectives of PCMod are the implementation of CRUD (Create, Read, Update, Delete) operations for PC components, reviews, and users. The application aims to provide users with an easy and efficient way to browse and filter PC components, as well as complete purchases.

To meet the authorization requirements based on user roles and resource ownership, it is necessary to implement an user authentication and management system (e.g., access to the administration panel).

As advanced objectives, the application will integrate services capable of sending emails, generating PDF invoices, and assisting users through an AI-powered chatbot.

* Enable administrators to perform complete CRUD operations for PC components.
* Provide users with efficient search and filtering capabilities for PC components.
* Implement an user authentication and management system supporting multiple user roles with different privilege levels.
* Enable complete CRUD operations for reviews.
* Enable complete CRUD operations for users.
* Provide a complete purchasing workflow (shopping cart → secure payment using Stripe → email confirmation).
* Send automated email notifications when a new user registers or a purchase is completed.
* Automatically generate and send purchase invoices in PDF format via email.
* Provide administrators with a dashboard containing statistics, a user list, and a purchase history.
* Integrate an AI-powered chatbot to assist users.

## Technical Objectives

PCMod follows a client–server architecture, using React and TypeScript for the frontend, Spring Boot and Java for the backend, and MySQL as the relational database management system. The application is deployed over HTTPS on port 443.

Postman will be used to design, document, and test the REST API endpoints provided by the backend. In addition, SpringDoc and Swagger/OpenAPI will be used to generate and maintain the API documentation.

To ensure software quality, automated testing will be implemented using tools such as Mockito, Selenium, and JUnit. In addition, static code quality analysis will be performed using SonarQube.

Finally, the application packaging and publishing process will be carried out using Docker and GitHub Actions workflows.

* Develop a web application based on a client–server architecture, separating the frontend, backend, and database layers.
* Develop the frontend using React and TypeScript.
* Develop the backend using Spring Boot and Java.
* Manage application data through a MySQL relational database.
* Design, document, and test the available REST API endpoints using Postman, SpringDoc, and Swagger/OpenAPI.
* Integrate external APIs to support secure payment processing and the AI chatbot.
* Use additional technologies for email delivery, chart generation, and PDF generation.
* Ensure software quality through automated testing, GitHub Actions workflows, and static code analysis using SonarQube.
* Containerize the application and publish the Docker image to Docker Hub using GitHub Actions workflows.

⚠️ **NOTE:** *At this stage, the functional and technical objectives of the application have been defined, and development has begun; however, the implementation is not yet functional.*