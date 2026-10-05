# AI Exception Analyzer

A Spring Boot REST API with a React frontend that uses an AI model (through the Groq API) to analyze Java and Spring Boot exceptions. It explains the root cause, possible causes, a suggested fix, and how to prevent the problem.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Architecture](#project-architecture)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [How It Works](#how-it-works)
- [Setup and Installation](#setup-and-installation)
- [Running the Backend](#running-the-backend)
- [Running the Frontend](#running-the-frontend)
- [Testing](#testing)
- [Example](#example)
- [Error Handling](#error-handling)
- [Environment Variables](#environment-variables)
- [Learning Outcomes](#learning-outcomes)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

## Overview

When a Java application crashes, it prints an **exception**: a type (for example `java.lang.NullPointerException`), a message, and a stack trace. For beginners, these can be hard to understand, and even experienced developers spend time working out what went wrong.

**AI Exception Analyzer** makes this easier. You give it three pieces of information:

1. **Exception Type**: for example `java.lang.NullPointerException`
2. **Exception Message**: for example `Cannot invoke "User.getName()" because "user" is null`
3. **Stack Trace**: the lines that show where the error happened

The Spring Boot backend builds a prompt from this information and sends it to an AI model through the **Groq API**. The AI's answer comes back as structured JSON containing:

| Field            | Meaning                                                  |
|------------------|----------------------------------------------------------|
| `exceptionType`  | The type of exception that was analyzed                  |
| `severity`       | How serious the problem is: `LOW`, `MEDIUM`, `HIGH` or `CRITICAL` |
| `rootCause`      | The most likely reason the exception happened            |
| `possibleCauses` | A list of other things that could cause it               |
| `suggestedFix`   | How to fix the problem                                   |
| `prevention`     | A list of tips to avoid the problem in the future        |

The project shows how a normal Spring Boot application can work with a Large Language Model (LLM) in a clean, layered and testable way.

---

## Features

- **Java / Spring Boot exception analysis**: send any exception type, message and stack trace for analysis.
- **AI-powered root cause analysis** using a model hosted on Groq.
- **Severity rating** limited to `LOW`, `MEDIUM`, `HIGH` or `CRITICAL`.
- **Possible causes, suggested fix and prevention tips** in every response.
- **Structured AI output**: the request to Groq includes a strict JSON schema, and the backend checks the AI's answer before returning it.
- **Input validation** with Jakarta Bean Validation (required fields and maximum lengths).
- **Centralized error handling** with consistent JSON error responses.
- **REST API**: a single `POST` endpoint that you can call from Postman, curl or the frontend.
- **Groq AI integration** through Spring's `RestClient`, with configurable model and timeout.
- **React frontend**: a simple web page with a form, a loading indicator, readable error messages and a formatted result card.
- **Unit and integration tests** written with JUnit 5, Mockito, MockMvc and `MockRestServiceServer`.

---

## Technology Stack

### Backend

| Technology | Purpose |
|------------|---------|
| Java 21 | Programming language |
| Spring Boot 4.1.1 | Application framework |
| Spring Web MVC (`spring-boot-starter-webmvc`) | REST controller and `RestClient` HTTP calls |
| Jakarta Bean Validation (`spring-boot-starter-validation`) | Validates incoming requests |
| Jackson | Converts JSON to and from Java objects |
| Maven (with Maven Wrapper) | Build tool and dependency management |
| Groq API | Hosts the AI model that performs the analysis |
| JUnit 5 | Testing framework |
| Mockito | Creates mock objects in tests |
| AssertJ | Readable test assertions |
| MockMvc / MockRestServiceServer | Tests the controller and the Groq HTTP client without real network calls |

### Frontend

| Technology | Purpose |
|------------|---------|
| React 19 | Builds the user interface |
| JavaScript | Frontend language |
| CSS | Styling |
| Webpack 5 + webpack-dev-server | Bundles the code and runs the development server |
| Babel | Converts modern JavaScript/JSX into browser-compatible code |

---

## Project Architecture

```
Postman / React Frontend
          │
          ▼
ExceptionAnalyzerController   (REST API layer)
          │
          ▼
ExceptionAnalyzerService      (service layer, uses PromptBuilder)
          │
          ▼
AIClient                      (interface)
          │
          ▼
GroqAIClient                  (implementation)
          │
          ▼
Groq API                      (AI model)
          │
          ▼
AI response (JSON)
          │
          ▼
ExceptionAnalyzeResponse      (returned to the user)
```

Each layer in simple terms:

| Layer | What it does |
|-------|--------------|
| **Postman / React Frontend** | Where the user enters the exception details and sends the request. |
| **ExceptionAnalyzerController** | Receives the HTTP request, validates the input, and passes it to the service. It contains no business logic. |
| **ExceptionAnalyzerService** | Coordinates the work: it asks `PromptBuilder` to create the AI prompt, then asks the AI client to analyze it. |
| **AIClient** | An interface that describes what any AI client must do (`analyze(prompt)`). The service depends on this interface, not on Groq directly, so another AI provider could be added later without changing the service. |
| **GroqAIClient** | The real implementation. It sends the prompt to Groq's chat completions endpoint, reads the answer, and checks that it is valid. |
| **Groq API** | The external AI service that runs the language model. |
| **ExceptionAnalyzeResponse** | The final Java object (DTO) that is converted to JSON and sent back to the user. |

---

## Project Structure

```
ai-exception-analyzer
├── pom.xml                         # Maven configuration and dependencies
├── mvnw / mvnw.cmd                 # Maven Wrapper scripts (no Maven install needed)
├── src
│   ├── main
│   │   ├── java/com/sreekar/aiexceptionanalyzer
│   │   │   ├── AiExceptionAnalyzerApplication.java
│   │   │   ├── client
│   │   │   │   ├── AIClient.java
│   │   │   │   └── GroqAIClient.java
│   │   │   ├── config
│   │   │   │   └── AIConfig.java
│   │   │   ├── controller
│   │   │   │   └── ExceptionAnalyzerController.java
│   │   │   ├── dto
│   │   │   │   ├── ExceptionAnalyzeRequest.java
│   │   │   │   └── ExceptionAnalyzeResponse.java
│   │   │   ├── exception
│   │   │   │   ├── AIClientException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── service
│   │   │   │   ├── ExceptionAnalyzerService.java
│   │   │   │   └── ExceptionAnalyzerServiceImpl.java
│   │   │   └── util
│   │   │       └── PromptBuilder.java
│   │   └── resources
│   │       └── application.properties
│   └── test/java/com/sreekar/aiexceptionanalyzer
│       ├── AiExceptionAnalyzerApplicationTests.java
│       ├── client/GroqAIClientTest.java
│       ├── controller/ExceptionAnalyzerControllerTest.java
│       ├── dto/ExceptionAnalyzeRequestValidationTest.java
│       ├── service/ExceptionAnalyzerServiceImplTest.java
│       └── util/PromptBuilderTest.java
└── frontend
    ├── package.json                # npm scripts and dependencies
    ├── webpack.config.js           # Webpack build and dev server (port 3000, /api proxy)
    ├── babel.config.js
    ├── public
    │   └── index.html
    └── src
        ├── index.js                # React entry point
        ├── App.js                  # Main component, holds page state
        ├── App.css                 # Styles
        ├── components
        │   ├── Header.js
        │   ├── ExceptionForm.js
        │   ├── LoadingState.js
        │   └── AnalysisResult.js
        └── services
            └── exceptionAnalyzerApi.js   # Calls the backend API
```

### Important backend classes

| Class | Description |
|-------|-------------|
| `AiExceptionAnalyzerApplication` | The main class that starts the Spring Boot application. |
| `ExceptionAnalyzerController` | REST controller that exposes `POST /api/v1/exceptions/analyze`. |
| `ExceptionAnalyzerService` / `ExceptionAnalyzerServiceImpl` | Service interface and its implementation. Builds the prompt and calls the AI client. |
| `PromptBuilder` | Creates the text prompt sent to the AI. It tells the AI to return only JSON with the six required fields, wraps the user's input in `<exception>` tags, and tells the AI to treat that input only as data, not as instructions. Empty values become `Not provided`. |
| `AIClient` | Interface for AI providers. |
| `GroqAIClient` | Calls Groq's `/chat/completions` endpoint with the API key, model, a low temperature (`0.2`) and a strict JSON schema. It then reads the AI's answer, removes Markdown code fences if present, converts it into `ExceptionAnalyzeResponse`, and checks that all fields are present and the severity is valid. |
| `AIConfig` | Reads the `groq.*` settings (API key, base URL, model, timeout) from `application.properties`. |
| `ExceptionAnalyzeRequest` | Request DTO with validation rules. |
| `ExceptionAnalyzeResponse` | Response DTO with the six analysis fields. |
| `AIClientException` | Thrown when anything goes wrong while talking to Groq. |
| `GlobalExceptionHandler` | Converts exceptions into clean JSON error responses with the right HTTP status code. |

### Important frontend files

| File | Description |
|------|-------------|
| `ExceptionForm.js` | Form with the three input fields. Checks that all fields are filled in before sending. |
| `LoadingState.js` | Shows a spinner while the analysis is running. |
| `AnalysisResult.js` | Displays the result, including a color-coded severity badge. |
| `exceptionAnalyzerApi.js` | Sends the request to the backend and turns error responses into user-friendly messages. |

---

## API Documentation

### Analyze an exception

| Item | Value |
|------|-------|
| **URL** | `http://localhost:8080/api/v1/exceptions/analyze` |
| **Method** | `POST` |
| **Content-Type** | `application/json` |

### Request body

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `exceptionType` | string | Yes | Must not be blank, max 200 characters |
| `message` | string | Yes | Must not be blank, max 2000 characters |
| `stackTrace` | string | No (API) / Yes (frontend form) | Max 20000 characters |

> The backend API accepts a request without a stack trace, but the React form requires it so that the AI gets as much information as possible.

```json
{
  "exceptionType": "java.lang.NullPointerException",
  "message": "Cannot invoke User.getName() because user is null",
  "stackTrace": "java.lang.NullPointerException at UserService.java:42"
}
```

### Response body (200 OK)

```json
{
  "exceptionType": "java.lang.NullPointerException",
  "severity": "HIGH",
  "rootCause": "Attempting to invoke getName() on a null User object",
  "possibleCauses": [
    "User object not initialized before use",
    "Missing null check before calling getName()"
  ],
  "suggestedFix": "Add a null check before invoking getName()",
  "prevention": [
    "Always validate objects for null before use",
    "Add unit tests covering null scenarios"
  ]
}
```

> **Note:** This is an example. The response is generated by an AI model, so the wording, severity and number of list items can be different each time. The **structure** (the six fields) is always the same, because the backend rejects AI answers that do not match it.

### HTTP status codes

| Status | When it happens |
|--------|-----------------|
| `200 OK` | The exception was analyzed successfully. |
| `400 Bad Request` | Validation failed (for example, a blank field or text that is too long), or the request body is missing or is not valid JSON. |
| `502 Bad Gateway` | The AI service could not be used: the API key is missing, Groq returned an error, the request timed out, or the AI's answer was invalid. |
| `500 Internal Server Error` | Any other unexpected error. |

Other standard Spring MVC errors (for example, sending a request with the wrong `Content-Type`) are returned with their normal status code in the same JSON error format.

### Error response format

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-01-01T10:15:30.123Z",
  "errors": {
    "exceptionType": "exceptionType must not be blank"
  }
}
```

The `errors` field only appears for validation errors.

---

## How It Works

1. **The user enters exception details** in the React form (or sends a request with Postman/curl).
2. **The request reaches `ExceptionAnalyzerController`** at `POST /api/v1/exceptions/analyze`.
3. **Validation checks the input.** The `@Valid` annotation checks the rules on `ExceptionAnalyzeRequest`. If a rule fails, a `400 Bad Request` is returned and the AI is never called.
4. **The service takes over.** `ExceptionAnalyzerServiceImpl` receives the valid request.
5. **`PromptBuilder` creates the AI prompt.** It combines fixed instructions (return only JSON, use these six fields, severity must be one of four values) with the user's exception details.
6. **`AIClient` sends the request to Groq.** `GroqAIClient` checks that an API key is configured, then sends the prompt to Groq's chat completions endpoint, together with a strict JSON schema describing the expected answer.
7. **Groq analyzes the exception** using the configured model and returns its answer.
8. **The answer is converted into `ExceptionAnalyzeResponse`.** `GroqAIClient` extracts the text, parses the JSON, checks that all required fields exist and that the severity is valid, and trims the values. If anything is wrong, an `AIClientException` is thrown, which becomes a `502 Bad Gateway`.
9. **The result is returned to the user** as JSON. The React frontend displays it as a formatted card.

---

## Setup and Installation

### Prerequisites

| Requirement | Notes |
|-------------|-------|
| Java 21 (JDK) | Required to build and run the backend |
| Maven | Not needed separately: the project includes the Maven Wrapper (`mvnw` / `mvnw.cmd`) |
| Node.js and npm | Required only for the React frontend |
| Groq API key | Create one in the Groq console |

### 1. Get the project

Clone or download this repository, then open a terminal in the project folder.

### 2. Set the `GROQ_API_KEY` environment variable

The application reads the API key from the `GROQ_API_KEY` environment variable. **Never put your real key in the source code or in this README.**

On **Windows PowerShell**:

```powershell
[Environment]::SetEnvironmentVariable("GROQ_API_KEY", "YOUR_GROQ_API_KEY", "User")
```

Replace `YOUR_GROQ_API_KEY` with your own key.

> **Important:** This saves the variable for your Windows user account, but programs that are already open will not see it. **Restart IntelliJ IDEA and/or your terminal** after setting it.

On **macOS / Linux** (current terminal session only):

```bash
export GROQ_API_KEY="YOUR_GROQ_API_KEY"
```

If the key is missing, the application still starts, but every analysis request returns `502 Bad Gateway` (the server log says the `GROQ_API_KEY` environment variable must be set).

---

## Running the Backend

From the project root on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS / Linux:

```bash
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**.

### Quick test with PowerShell

```powershell
$body = @{
  exceptionType = "java.lang.NullPointerException"
  message       = "Cannot invoke User.getName() because user is null"
  stackTrace    = "java.lang.NullPointerException at UserService.java:42"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/analyze" `
  -Method Post -ContentType "application/json" -Body $body
```

You can also send the same JSON body with **Postman**: choose `POST`, enter the URL above, and set the body to *raw → JSON*.

---

## Running the Frontend

The frontend is in the `frontend` folder. Make sure the backend is already running, then:

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:3000** in your browser.

How it connects to the backend: the Webpack dev server (port `3000`) forwards every request that starts with `/api` to the Spring Boot backend at `http://localhost:8080`. Because of this proxy, the browser only talks to one address, and no CORS configuration is needed.

To create a production build in `frontend/dist`:

```bash
npm run build
```

> The backend does not serve the built frontend files. During development, use `npm start`.

---

## Testing

Run all backend tests from the project root:

```powershell
.\mvnw.cmd clean test
```

Current result:

```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
```

> Some error stack traces appear in the console during the test run. This is expected: several tests deliberately trigger failures (for example, a Groq error or an unexpected exception) to check that they are handled correctly.

The tests do **not** call the real Groq API and do not need an API key.

| Test class | Tests | What it checks |
|------------|:-----:|----------------|
| `AiExceptionAnalyzerApplicationTests` | 1 | The Spring application context starts |
| `GroqAIClientTest` | 10 | Sending the prompt and parsing the answer, code-fence removal, missing API key, HTTP and network errors, empty responses, responses without text, invalid JSON, missing fields and unknown severity |
| `ExceptionAnalyzerControllerTest` | 6 | Successful response, validation errors (400), malformed JSON (400), AI failure (502), unexpected error (500) |
| `ExceptionAnalyzeRequestValidationTest` | 3 | A valid request passes; a blank `exceptionType` or `message` fails |
| `ExceptionAnalyzerServiceImplTest` | 2 | The service sends the built prompt to the AI client and passes AI failures through |
| `PromptBuilderTest` | 3 | The prompt contains the instructions and exception details, uses `Not provided` for a missing stack trace, and removes `<exception>` tags from user input |

---

## Example

Imagine your application throws this exception:

```
java.lang.NullPointerException: Cannot invoke "com.example.User.getName()" because "user" is null
	at com.example.UserService.getUserName(UserService.java:42)
	at com.example.UserController.getUser(UserController.java:25)
```

**Request:**

```json
{
  "exceptionType": "java.lang.NullPointerException",
  "message": "Cannot invoke \"com.example.User.getName()\" because \"user\" is null",
  "stackTrace": "java.lang.NullPointerException: Cannot invoke \"com.example.User.getName()\" because \"user\" is null\n\tat com.example.UserService.getUserName(UserService.java:42)\n\tat com.example.UserController.getUser(UserController.java:25)"
}
```

**The kind of analysis you can expect** (actual AI output will vary):

```json
{
  "exceptionType": "java.lang.NullPointerException",
  "severity": "HIGH",
  "rootCause": "UserService.getUserName() calls getName() on a User reference that is null at line 42.",
  "possibleCauses": [
    "The user was not found in the database and the lookup returned null",
    "The User object was never initialized before being used",
    "An upstream method returned null instead of throwing an exception"
  ],
  "suggestedFix": "Check whether the user is null before calling getName(), or use Optional and return a 404 or throw a clear exception when the user does not exist.",
  "prevention": [
    "Return Optional<User> from repository lookups instead of null",
    "Validate inputs and lookup results before using them",
    "Add unit tests for the 'user not found' case"
  ]
}
```

---

## Error Handling

All errors are handled in one place: `GlobalExceptionHandler` (a `@RestControllerAdvice`). Every error is returned as JSON with `status`, `message` and `timestamp`.

| Situation | Handled exception | HTTP status | Message |
|-----------|-------------------|-------------|---------|
| A field fails validation | `MethodArgumentNotValidException` | 400 | `Validation failed` (plus an `errors` object listing each invalid field) |
| Body is missing or not valid JSON | `HttpMessageNotReadableException` | 400 | `Request body is missing or is not valid JSON` |
| Problem with the AI service | `AIClientException` | 502 | `Unable to analyze the exception using the AI service` |
| Standard Spring MVC errors (e.g. unsupported media type) | `Exception` that implements `ErrorResponse` | Original status | Spring's error detail |
| Anything else | `Exception` | 500 | `An unexpected error occurred` |

`GroqAIClient` throws `AIClientException` when:

- `GROQ_API_KEY` is not set
- Groq returns an HTTP error (for example 401 or 429)
- the call fails or times out (default timeout: 30 seconds)
- the response is empty, is not valid JSON, or contains no text
- the AI's analysis is missing required fields or has a severity that is not `LOW`, `MEDIUM`, `HIGH` or `CRITICAL`

For AI errors, the details are written to the server log, but the user only sees a general message. This keeps internal details out of the API response.

The React frontend also handles errors: it validates the form before sending, shows the backend's field validation messages, and shows clear messages when the backend cannot be reached or the AI service is unavailable.

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `GROQ_API_KEY` | Yes | *(none)* | Your Groq API key |
| `GROQ_MODEL` | No | `openai/gpt-oss-20b` | The Groq model to use for analysis |

These are connected in `src/main/resources/application.properties`:

```properties
groq.api-key=${GROQ_API_KEY:}
groq.base-url=https://api.groq.com/openai/v1
groq.model=${GROQ_MODEL:openai/gpt-oss-20b}
groq.timeout=30s
```

> **Security:** Never commit API keys to GitHub. Keep them in environment variables only. If a key is ever pushed by mistake, revoke it in the Groq console and create a new one.

---

## Learning Outcomes

This project demonstrates:

- Building a **REST API** with Spring Boot
- Using **DTOs** (Java records) for requests and responses
- Separating code into **controller, service and client layers**
- Programming to **interfaces** (`ExceptionAnalyzerService`, `AIClient`)
- **Calling an external API** with Spring's `RestClient`
- **AI / LLM integration** with the Groq API
- **Prompt building**, including keeping user input separate from instructions
- Requesting **structured JSON output** from an LLM and validating it
- **JSON handling** with Jackson
- **Input validation** with Jakarta Bean Validation
- **Centralized exception handling** with `@RestControllerAdvice`
- **Externalized configuration** with `@ConfigurationProperties` and environment variables
- **Unit testing** with JUnit 5, Mockito, MockMvc and `MockRestServiceServer`
- Building a **React frontend** with Webpack and connecting it to a Spring Boot backend through a dev-server proxy

---

## Future Improvements

These features are **not implemented yet**. They are ideas for the future:

- Store previous analyses in a database
- Add user authentication
- Support more AI providers (by adding new `AIClient` implementations)
- Analyze uploaded log files
- Add GitHub integration

---

## Author

<!-- Replace the placeholders below with your details. -->

- **Name:** *Your Name*
- **GitHub:** *[your-github-username](https://github.com/your-github-username)*
