# 🧩 Analysis

## 🖥️ Pages and navigation

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

### Navigation Diagram:
![Wireframe Screens](/docs/img/wireframes/Wireframes%20Screens.png)

## 📦 Entities

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

## 🔒 User Permissions

### 👤 Anonymous Users:

**Available functionalities:**
* Viewing the component listing.
  * **NOTE:** *Search filters are available.*
* Viewing a component detail page.
* Viewing the reviews associated with a component.
* User registration.
  * **NOTE:** *Email service usage is included.*
* Using the AI-powered chatbot.

**Ownership:** An anonymous user does not own any other element within the application.

### 🧑 Registered Non-Administrator Users:

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

### 👨‍💼 Registered Administrator Users:

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

## 🖼️ Images

The images associated with the entities are the following:

* **User:** User profile image.
* **Component:** Product image.

## 📊 Charts

The PCMod application includes two charts:

* **Bar chart:** Best-selling products.
* **Pie chart:** Best-selling product types.

## ⚙️ Additional Technologies

The following additional technologies are used:

* ✉️ **JavaMailSender:** Sending emails when registering an account and completing a purchase.
* 📄 **PDF generation:** Generation of PDF files after completing a purchase as an invoice.
* 🤖 **Generative AI API:** AI-powered chatbot available in the application, communicating with an AI API.
* 💵 **Stripe API:** Communication with a secure payment gateway to complete purchases.

## 🔎 Advanced Algorithm or Query

The advanced query algorithm consists of searching for components within the database catalog using filters such as name, price, brand, and/or type.