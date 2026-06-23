# Campus360

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Backend: Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203-brightgreen)]()
[![Frontend: React](https://img.shields.io/badge/Frontend-React%2018-blue)]()

Campus360 is an enterprise-grade, multi-tenant School Management System designed to handle complex administrative workflows for universities, colleges, and schools.

It seamlessly manages everything from student and faculty onboarding to course assignments, and features a powerful real-time notification engine.

## 🌟 Key Features

- **Multi-Tenancy**: Fully isolated tenant management, meaning multiple distinct institutions can run on the exact same platform.
- **Real-Time Notifications**: Powered by Server-Sent Events (SSE), users get instant in-app alerts when bulk imports finish, profiles are approved, or events occur.
- **Enterprise Email Templates**: Built-in, responsive HTML email templates with micro-animations and beautiful UI for onboarding and alerts.
- **Bulk Imports**: Easily onboard thousands of students and faculty members in one go.
- **Role-Based Access Control (RBAC)**: Fine-grained permissions for SUPER_ADMIN, INSTITUTION_ADMIN, HOD, FACULTY, STUDENT, and PARENT.

## 🛠️ Technology Stack

**Backend**
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL

**Frontend**
- React 18
- Vite
- TypeScript
- TailwindCSS
- Lucide Icons
- Server-Sent Events (SSE)

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Docker & Docker Compose

### Installation

1. **Clone the repo**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Campus360.git
   cd Campus360
   ```

2. **Start Infrastructure Services** (Postgres & MailHog)
   ```bash
   docker-compose up -d
   ```

3. **Run Backend (Spring Boot)**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   The backend will start on `http://localhost:8080`.

4. **Run Frontend (React/Vite)**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The frontend will be available at `http://localhost:5173`.

## 🤝 Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to set up your development environment and submit Pull Requests.
Please also review our [CODE OF CONDUCT](CODE_OF_CONDUCT.md).

## 📝 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

## 📧 Contact

Project Link: [https://github.com/nenrikaraju123/Campus360](https://github.com/nenrikaraju123/Campus360)
Email: rajunerenika@gmail.com
