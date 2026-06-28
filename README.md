# Lumière Salon Management System

A full-stack salon booking web app built with Spring Boot, MySQL, Thymeleaf, and Spring Security. It supports separate workflows for customers, stylists, and administrators.

Originally developed as a university group project and later expanded by Manula Keshawa Pinidiya [@manulakeshawa](https://github.com/manulakeshawa) as a personal portfolio project.

**Tech highlight:** Java 17 · Spring Boot · MySQL · Thymeleaf · Spring Security

**Live Demo:** https://beauty-salon-booking-system.onrender.com

> The free hosted demo may take a short time to wake up after inactivity.

## Tech Stack

| Area | Technologies |
| --- | --- |
| Backend | Java 17, Spring Boot, Spring MVC |
| Security | Spring Security, salted PBKDF2 password hashing |
| Data | MySQL, Spring Data JPA, Hibernate |
| Frontend | Thymeleaf, HTML, CSS, JavaScript |
| Build & Email | Maven / Maven Wrapper, HTTPS transactional email API (Brevo) |
| Deployment | Render, Aiven MySQL, Brevo Transactional Email API |

## Main Features

**Customers**

- Register, log in, and manage salon appointments
- Browse services and submit/manage reviews
- Update profile details and change passwords
- Use forgot-password and reset-password flows

**Stylists**

- Log in with email-based staff accounts
- View stylist dashboard and appointment-related workflow
- Update profile details and change passwords

**Administrators**

- Log in using username or email
- Manage customers, stylists, services, appointments, and reviews
- Update admin username, email, and password
- Create customer/stylist accounts and send first-time password setup emails

## Security-Focused Features

- Spring Security role-based access control for `CUSTOMER`, `STYLIST`, and `ADMIN` accounts
- Salted PBKDF2 password hashing with no plain-text password storage
- Global email uniqueness across customer, stylist, and admin account types
- Email-based first-time password setup and forgot-password reset using expiring tokens
- Setup/reset token hashes are stored instead of raw tokens
- Setup/reset links are emailed and not exposed in the admin UI
- CSRF protection, access-denied handling, and environment-based secrets

## Database

The app uses MySQL (`beauty_salon_db`) with Spring Data JPA. On first run, `data.sql` seeds demo data automatically and `SeedDataInitializer` prevents the demo seed from being re-applied on every restart. To reset the demo database, drop and recreate the database, then run the app again.

`data.sql` is demo seed data for a fresh/local database setup, not the live database. Editing or deleting records in MySQL does not automatically update `data.sql`.

> Passwords in `data.sql` are hashed demo passwords. Never store real credentials, email app passwords, reset tokens, or setup tokens there.

## Environment Variables

Configure these values locally before running the app:

| Variable                      | Purpose |
|-------------------------------| --- |
| `PORT`                        | HTTP port supplied by a deployment platform; defaults to `8080` locally |
| `SPRING_DATASOURCE_URL`       | MySQL JDBC URL; defaults to `jdbc:mysql://localhost:3306/beauty_salon_db` |
| `SPRING_DATASOURCE_USERNAME`  | MySQL username; defaults to `root` |
| `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD` | MySQL password. `SPRING_DATASOURCE_PASSWORD` is used for deployment; `DB_PASSWORD` is also supported as a local fallback.|
| `APP_BASE_URL`                | Base URL used in emailed setup/reset links, for example `http://localhost:8080` |
| `APP_SEED_ENABLED`            | Enables demo seed data from `data.sql`; defaults to `true` |
| `BREVO_API_KEY`               | Brevo Transactional Email API key used for HTTPS email sending |
| `MAIL_FROM`                   | Verified sender email address |
| `MAIL_FROM_NAME`              | Sender display name; defaults to `Lumiere Salon` |
| `EMAIL_PROVIDER`              | Optional email provider selector; currently defaults to `brevo` |

Email sending uses Brevo's HTTPS transactional email API instead of SMTP, so it works on platforms such as Render Free where outbound SMTP ports are blocked.

Do not commit real secrets or real email API keys.

## Deployment Notes

The live demo is deployed with Render for the Spring Boot web service, Aiven MySQL for the cloud database, and Brevo Transactional Email API for password setup/reset emails.

For deployment, `APP_BASE_URL` must be set to the live site URL so emailed setup/reset links point to the deployed app instead of localhost. `APP_SEED_ENABLED` can be set to `false` after the demo database has been seeded once.

## Setup and Run

1. Clone the repository.

   ```powershell
   git clone https://github.com/manulakeshawa/beauty-salon-booking-system.git
   cd beauty-salon-booking-system
   ```

2. Create the MySQL database.

   ```sql
   CREATE DATABASE beauty_salon_db;
   ```

3. Configure the environment variables listed above using your system environment variables, IntelliJ Run Configuration, or your deployment platform's environment/secret settings.


4. Run the application with the Maven Wrapper.

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. Open the app.

   ```text
   http://localhost:8080
   ```

To build without running tests:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## Demo Accounts

The following non-admin demo accounts are available for local/demo testing and come from `data.sql`.

| Role | Login | Password |
| --- | --- | --- |
| Customer | `amaya.f@email.com` | `amaya123` |
| Customer | `sarah.j@email.com` | `pass123` |
| Stylist | `nalika@lumieresalon.lk` | `#nalika@lume` |
| Stylist | `kasun@lumieresalon.lk` | `#kasun@lume` |
| Stylist | `kamindu@lumieresalon.lk` | `#kamindu@lume` |

Admin demo access is intentionally not listed publicly for the live deployment.

## Screenshots

### Home Page
![Home Page](docs/screenshots/home-page.png)

### Services
![Services](docs/screenshots/services.png)

### Appointment Booking
![Appointment Booking](docs/screenshots/booking.png)

### Admin Dashboard
![Admin Dashboard](docs/screenshots/admin-dashboard.png)

### Customer Dashboard
![Customer Dashboard](docs/screenshots/customer-dashboard.png)

### Password Reset
![Password Reset](docs/screenshots/password-reset.png)

## Project Structure

```text
src/main/java/com/manula/beautysalon/
  controller/      MVC controllers
  model/           JPA entities and domain models
  repository/      Spring Data repositories
  security/        Authentication and access control
  service/         Business logic, account flows, and email services
  util/            Shared helper utilities

src/main/resources/
  templates/       Thymeleaf views
  static/          CSS, images, and frontend assets
  data.sql         Local demo seed data
  application.properties
  
docs/screenshots/  README screenshots
Dockerfile         Deployment container configuration
LICENSE            MIT license
pom.xml            Maven project configuration
```

## Portfolio Enhancements by Manula Keshawa Pinidiya

- Migrated the original file-based storage system to MySQL with Spring Data JPA/Hibernate
- Refactored the project structure and added service-layer business logic
- Added Spring Security role-based authentication and route protection for customer, stylist, and admin accounts
- Added salted PBKDF2 password hashing, global email uniqueness, profile management, and secure account update flows
- Added first-time password setup and forgot-password reset flows using expiring email tokens
- Added email sending with Brevo HTTPS transactional email API for cloud deployment compatibility
- Prepared the project for deployment with environment-based configuration, Render hosting, and Aiven MySQL
- Improved README documentation, screenshots, and code comments for portfolio presentation

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Original Contributors - SLIIT 2026 Group WD109

| Student ID | GitHub |
| --- | --- |
| IT25101660 | Manula Keshawa Pinidiya — original contributor; portfolio version maintained at [@manulakeshawa](https://github.com/manulakeshawa) |
| IT25101942 | [@IT25101942](https://github.com/IT25101942) |
| IT25102887 | [@IT25102887](https://github.com/IT25102887) |
| IT25100717 | [@Anudi717](https://github.com/Anudi717) |
| IT25101934 | [@it25101934](https://github.com/it25101934) |
