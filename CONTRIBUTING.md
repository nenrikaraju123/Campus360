# Contributing to Campus360

First off, thank you for considering contributing to Campus360! It's people like you that make Campus360 such a great tool for school management.

## 1. Where do I go from here?

If you've noticed a bug or have a feature request, make sure to check our [Issues](../../issues) to see if someone has already filed it. If not, feel free to open a new one using our templates!

## 2. Setting up for local development

To contribute code, you'll need to set up the project locally. Campus360 consists of a Spring Boot backend and a React (Vite) frontend.

### Prerequisites
- **Java 17+**
- **Node.js 18+**
- **Docker & Docker Compose** (for running PostgreSQL and Mailhog)

### Backend Setup
1. Open a terminal in the project root.
2. Run `docker-compose up -d` to start the PostgreSQL database and MailHog.
3. Build the backend using Maven: `./mvnw clean install` (or `mvn clean install`).
4. Run the application: `./mvnw spring-boot:run` (or run `Campus360Application` from your IDE).

### Frontend Setup
1. Navigate to the frontend directory: `cd frontend`
2. Install dependencies: `npm install`
3. Start the development server: `npm run dev`

The app will be available at `http://localhost:5173`. The backend runs on `http://localhost:8080`.

## 3. Creating a Pull Request

When you're ready to submit a change, please follow these steps:

1. **Fork the repository** and clone your fork locally.
2. **Create a branch** for your feature or bugfix (`git checkout -b feature/your-feature-name`).
3. **Commit your changes**. Write clear, concise commit messages.
4. **Push your branch** to your fork (`git push origin feature/your-feature-name`).
5. **Open a Pull Request** against the `main` branch of this repository.

### PR Guidelines
- Ensure your PR description clearly explains the problem you are solving and the approach you took.
- Please verify that your code compiles and all existing tests pass (`mvn test`).
- We strongly encourage adding unit tests for any new features or bug fixes.
- For frontend changes, ensure there are no linting errors (`npm run lint`).

## 4. Code Style

- **Java**: We follow standard Java conventions. We prefer clear naming over excessive comments.
- **TypeScript/React**: We use Prettier for formatting and ESLint for code quality. Please run `npm run format` before committing.

## 5. Contact

If you have questions, reach out by opening an issue or emailing rajunerenika@gmail.com.
