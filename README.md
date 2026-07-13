# 🖥️2026 - PCMod: A Web Application for managing an e-commerce platform for PC components

![Logo PCMod](/docs/img/Full_Logo.png)

## 🖥️ Introduction

PCMod is a web-based application designed for buying or selling PC components online.

Its primary functionality is to facilitate the purchase and sale of computer hardware, including CPUs, GPUs, RAM modules, and other components. Additionally, the platform allows users to submit reviews for the products. The application provides a searchable and filterable product catalog, detailed product pages, secure payment processing, an AI-powered chatbot that acts as a virtual assistant, automated email notifications for user registration and purchase confirmation, and the generation of PDF invoices for completed transactions ([see the section "Basic, intermediate and advanced features"](#️-basic-intermediate-and-advanced-features)).

The functionalities available within the application depend on the user's assigned role, with each role having access to a specific set of features ([see the section "Features available by user role"](#-features-available-by-user-role)).

## 🎯 Objectives

This section outlines the functional and technical objectives to be achieved during the development of the PCMod application.

### Functional Objectives

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

### Technical Objectives

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

⚠️ **NOTE:** *At this stage, the functional and technical objectives of the application have been defined; however, the implementation has not yet begun.*

## 🛠️ Methodology

This project follows an iterative and incremental development methodology based on the principles of Extreme Programming (XP).

GitHub Projects and GitHub Issues are used to manage tasks through a Kanban board. GitHub Flow is adopted as the version control workflow. GitHub Actions is used to automate testing, code quality checks, and the publication of Docker images to Docker Hub.

The project is divided into the following phases:

* 📄 **Phase 1: Definition of features and user interfaces:** Definition of the functional and technical objectives, classification of features into basic, intermediate, and advanced levels, definition of permissions for each user role, design of application pages using *wireframes*, selection of charts to be displayed, design of the application's class structure, selection of additional technologies, and design of the advanced search algorithm.
* 📁 **Phase 2: Repository, testing and continuous integration:** Creation of the backend and frontend projects, configuration of testing workflows in the repository, implementation of the minimum functionality required to verify communication between application layers, testing of the initial functionality, and configuration of static code analysis with SonarQube.
* 🖥️ **Phase 3: Version 0.1 – Basic functionality and Docker:** Implementation of the application's basic functionality, release of version 0.1, creation of the required Dockerfile and Docker Compose files, and publication of version 0.1 to Docker Hub.
* 🖥️ **Phase 4: Version 0.2 – Intermediate functionality:** Implementation of the application's intermediate functionality, release of version 0.2, and publication of version 0.2 to Docker Hub.
* 🖥️ **Phase 5: Version 1.0 – Advanced functionality:** Implementation of the application's advanced functionality, release of version 1.0, and publication of version 1.0 to Docker Hub.
* 📝 **Phase 6: Thesis report:** Preparation of the project's final report.
* 🧑‍🏫 **Phase 7: Thesis defense:** Preparation and defense of the project before the evaluation committee.

The planned start and delivery dates for each phase are shown below:

| Phase | Start Date | Planned Delivery Date | Actual Delivery Date |
| ----- | :--------: | :-------------------: | :------------------: |
| Phase 1: Definition of features and user interfaces | 30/06/2026 | 15/09/2026 | 13/07/2026 |
| Phase 2: Repository, testing and continuous integration | - | 15/10/2026 | - |
| Phase 3: Version 0.1 – Basic functionality and Docker | - | 15/12/2026 | - |
| Phase 4: Version 0.2 – Intermediate functionality | - | 01/03/2027 | - |
| Phase 5: Version 1.0 – Advanced functionality | - | 15/04/2027 | - |
| Phase 6: Thesis report | - | 15/05/2027 | - |
| Phase 7: Thesis defense | - | 15/06/2027 | - |

### 📊  Gantt chart:

![Gantt chart](/docs/img/gantt_chart.png)

## 🖥️ Basic, intermediate and advanced features

The functionalities available in the application are divided into basic, intermediate, and advanced features as follows:

### 🔧 Basic features:

* Component creation
* Component editing
* Component deletion
* Component listing
* Displaying component detail pages
* Adding components to the shopping cart
* Removing components from the shopping cart
* Viewing components in the shopping cart
* Completing purchases
* Review creation
* Review modification
* Review deletion
* Listing component reviews
* User registration
* User login
* Editing personal information
* User account deletion
* Accessing the user profile page
* Listing all user purchases
* Listing all users
* Listing all PCMod purchases

### 🛠️ Intermediate features:

* Email sending (user registration and purchase completion notifications)
* PDF generation (purchase invoice sent by email)
* Advanced search filtering (component name, type, brand, and price)
* Displaying non-graphical statistics
* Displaying graphical statistics

### ⚙️ Advanced features:

* Stripe payment gateway integration
* AI-powered chatbot integration

## 👤 Features Available by User Role

PCMod users will have access to different functionalities depending on their assigned role:

| Feature | Anonymous User | Registered Non-Administrator User | Registered Administrator User |
|---------|:------:|:------:|:------:|
| Component creation | ❌ | ❌ | ✅ |
| Component editing | ❌ | ❌ | ✅ |
| Component deletion | ❌ | ❌ | ✅ |
| Component listing | ✅ | ✅ | ✅ |
| Component filtering | ✅ | ✅ | ✅ |
| Displaying component detail pages | ✅ | ✅ | ✅ |
| Adding components to the shopping cart | ❌ | ✅ | ✅ |
| Removing components from the shopping cart | ❌ | ✅ | ✅ |
| Viewing components in the shopping cart | ❌ | ✅ | ✅ |
| Completing purchases (Stripe integration) | ❌ | ✅ | ✅ |
| Review creation | ❌ | ✅ | ✅ |
| Review modification (controlled by owner or administrator) | ❌ | ✅ | ✅ |
| Review deletion (controlled by owner or administrator) | ❌ | ✅ | ✅ |
| Listing component reviews | ✅ | ✅ | ✅ |
| User registration | ✅ | ❌ | ❌ |
| User login | ❌ | ✅ | ✅ |
| Editing personal information (controlled by owner or administrator) | ❌ | ✅ | ✅ |
| User account deletion (controlled by owner or administrator) | ❌ | ✅ | ✅ |
| Accessing the user profile page (controlled by owner or administrator) | ❌ | ✅ | ✅ |
| Listing total user purchases | ❌ | ✅ | ✅ |
| Listing users | ❌ | ❌ | ✅ |
| Listing total PCMod purchases | ❌ | ❌ | ✅ |
| Displaying non-graphical statistics | ❌ | ❌ | ✅ |
| Displaying graphical statistics | ❌ | ❌ | ✅ |
| Email service usage | ✅ | ✅ | ✅ |
| PDF generation service usage | ❌ | ✅ | ✅ |
| AI-powered chatbot usage | ✅ | ✅ | ✅ |

## 🧩 Analysis

### 🖥️ Pages and navigation

This section describes the content of each designed page and presents the corresponding *wireframe* for each one.

* **Main page:** The initial page of the web application. It provides access to the component listing page, user registration, login, AI-powered chatbot, and the user dropdown menu.
  ![Wireframe Main Page](/docs/img/wireframes/wireframe-main-page.png)

* **Available components listing:** Page containing all available components in the store, allowing users to apply filters. It provides access to the component creation page (administrators only).
  ![Wireframe Products Page](/docs/img/wireframes/wirefreme-products-page.png)

* **Component detail page:** Page displaying the attributes of a component. It provides access to review creation, editing, and deletion (registered non-administrator users and administrators, with ownership control), as well as component editing and deletion (administrators only).
  ![Wireframe Detail Product Page](/docs/img/wireframes/wireframe-detail-product-page.png)

* **Component creation / editing form:** Page containing a form with the required fields to store a component in the database.
  ![Wireframe Add / Modify Component Form](/docs/img/wireframes/wireframe-add-modify-form.png)

* **Shopping cart page:** Page displaying the products added to the user's shopping cart and providing access to the payment process.
  ![Wireframe Cart Page](/docs/img/wireframes/wireframe-cart-page.png)

* **Login page:** Page containing a form to authenticate users using their email address and password. It provides access to the registration page.
  ![Wireframe LogIn Page](/docs/img/wireframes/wireframe-login-page.png)

* **Registration page:** Page containing a form to register new users and store their information in the database. It provides access to the login page.
  ![Wireframe Register Page](/docs/img/wireframes/wireframe-register-form.png)

* **Administration panel:** Page displaying statistics, the list of users, and the list of sales (administrators only). It provides access to the user profile page.
  ![Wireframe Admin Page](/docs/img/wireframes/wireframe-admin-page.png)

* **User profile page:** Page displaying the personal information of the registered user and a list of their completed purchases. It allows profile editing and account deletion.
  ![Wireframe User Profile Page](/docs/img/wireframes/wireframe-profile-page.png)

* **User menu:** Menu accessible from the website header after login. It provides access to the user profile page, the administration panel (administrators only), and the logout option.
  ![Wireframe Dropdown User Menu](/docs/img/wireframes/wireframe-user-dropdown.png)

* **AI chatbot prototype:** Page/component that enables users to interact through a chat interface with an artificial intelligence model.
  ![Wireframe AI Chatbot](/docs/img/wireframes/wireframe-chatbot-AI.png)

* **Error page:** Error page following the visual style of the rest of the application, displaying the error code and message. It provides access to the main page.
  ![Wireframe Error Page](/docs/img/wireframes/wireframe-error-page.png)

#### Navigation Diagram:
![Wireframe Screens](/docs/img/wireframes/Wireframes%20Screens.png)

### 📦 Entities

This section describes the entities present in the PCMod application, along with their corresponding attributes. They are represented in the following class diagram:

![Class Diagram](/docs/img/class_diagram.png)

| Class | Attributes |
|:------:|------------------|
| User | ID, name, surname, username, email, encodedPassword, address, purchaseList, reviewList, image |
| Review | ID, title, content, rating, component, author, date |
| Component | ID, name, description, type, brand, price, stock, reviewList, image |
| Purchase | ID, price, date, status, PurchaseItemList, customer |
| PurchaseItem | ID, unitPrice, quantity, component |

Two enumerated types have been added in order to improve maintainability and facilitate future extensions if required:

| Enum | Values |
|:----:|---------|
| PurchaseStatus | ACTIVE, FINISHED |
| ComponentType | CPU, GPU, RAM, STORAGE, MOTHERBOARD, PSU, CASE |

### 🔒 User Permissions

#### 👤 Anonymous Users:

**Available functionalities:**
* Viewing the component listing.
  * **NOTE:** *Search filters are available.*
* Viewing a component detail page.
* Viewing the reviews associated with a component.
* User registration.
  * **NOTE:** *Email service usage is included.*
* Using the AI-powered chatbot.

**Ownership:** An anonymous user does not own any other element within the application.

#### 🧑 Registered Non-Administrator Users:

**Available functionalities:**
* Viewing the component listing.
  * **NOTE:** *Search filters are available.*
* Viewing a component detail page.
* CRUD operations for reviews.
  * **NOTE:** *A registered non-administrator user can only edit or delete their own reviews.*
* CRUD operations for users.
  * **NOTE:** *A registered non-administrator user can only edit or delete their own user account.*
* Purchasing process: Adding/removing products from the shopping cart, viewing cart contents, and completing purchases.
  * **NOTE:** *Email service usage and Stripe integration are included.*
* User login.
* Using the AI-powered chatbot.

**Ownership:** A registered non-administrator user owns their completed purchases and created reviews.

#### 👨‍💼 Registered Administrator Users:

**Available functionalities:**
* CRUD operations for components.
  * **NOTE:** *Search filters are available.*
* CRUD operations for reviews.
  * **NOTE:** *Administrator users can edit and delete reviews created by any user.*
* CRUD operations for users.
  * **NOTE:** *Administrator users can edit and delete any user's account.*
* Purchasing process: Adding/removing products from the shopping cart, viewing cart contents, and completing purchases.
  * **NOTE:** *Email service usage and Stripe integration are included.*
* User login.
* Using the AI-powered chatbot.
* Accessing statistics: Viewing graphical and non-graphical statistics.

**Ownership:** A registered administrator user owns the available components.

### 🖼️ Images

The images associated with the entities are the following:

* **User:** User profile image.
* **Component:** Product image.

### 📊 Charts

The PCMod application includes two charts:

* **Bar chart:** Best-selling products.
* **Pie chart:** Best-selling product types.

### ⚙️ Additional Technologies

The following additional technologies are used:

* ✉️ **JavaMailSender:** Sending emails when registering an account and completing a purchase.
* 📄 **PDF generation:** Generation of PDF files after completing a purchase as an invoice.
* 🤖 **Generative AI API:** AI-powered chatbot available in the application, communicating with an AI API.
* 💵 **Stripe API:** Communication with a secure payment gateway to complete purchases.

### 🔎 Advanced Algorithm or Query

The advanced query algorithm consists of searching for components within the database catalog using filters such as name, price, brand, and/or type.

## 📰 Project Tracking

* 📰 **Medium blog**: [Medium link](https://medium.com/@jaimeeg5)
* 🗃️ **GitHub Project**: [GitHub Project link](https://github.com/orgs/codeurjc-students/projects/43/views/2)

## 👨‍🎓 Author

The development of the PCMod application is carried out as a Bachelor's Thesis in Software Engineering at the ETSII of the URJC.

| Name | Role | Email | GitHub |
|------|------|-------|--------|
| Jaime Esteban García | Author | j.esteban.2023@alumnos.urjc.es | [jaimeeg5](https://github.com/jaimeeg5) |
| Iván Chicano Capelo | Supervisor | ivan.chicano@urjc.es | [ivchicano](https://github.com/ivchicano) |

## 🤖 Use of AI Tools

The following section outlines the AI tools used during the different stages of the development of this Bachelor's Thesis:

| Phase | Tools | Goals |
|-------|-------|-------|
| 1 - Definition of features and user interfaces | Microsoft Copilot | #1 Logo generation for PCMod |
| 2 - Repository, testing and continuous integration| - | - |
| 3 - Version 0.1: Basic functionality and Docker| - | - |
| 4 - Version 0.2: Intermediate functionality | - | - |
| 5 - Version 1.0: Advanced functionality | - | - |
| 6 - Thesis report | - | - |
| 7 - Thesis defense | - | - |

Detailed information regarding the use of these tools can be found in [AI_USAGE.md](/AI_USAGE.md).
