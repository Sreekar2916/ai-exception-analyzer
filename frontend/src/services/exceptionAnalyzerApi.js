// Relative URL: in development the webpack dev server proxies /api
// to the Spring Boot backend at http://localhost:8080 (see webpack.config.js).
const ANALYZE_URL = "/api/v1/exceptions/analyze";

// Readable labels for the backend's field names in validation errors.
const FIELD_LABELS = {
  exceptionType: "Exception Type",
  message: "Exception Message",
  stackTrace: "Stack Trace",
};

/**
 * Sends an exception to the backend for AI analysis.
 * Resolves with the analysis object, or throws an Error whose message
 * is safe to show to the user.
 */
export async function analyzeException({ exceptionType, message, stackTrace }) {
  let response;
  try {
    response = await fetch(ANALYZE_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify({ exceptionType, message, stackTrace }),
    });
  } catch {
    // fetch() only rejects on network failures (server down, no connection, etc.)
    throw new Error(
      "Could not reach the analysis server. Please make sure the backend is running and try again."
    );
  }

  const body = await readJsonSafely(response);

  if (!response.ok) {
    throw new Error(buildErrorMessage(response.status, body));
  }

  if (!isValidAnalysis(body)) {
    throw new Error("The server returned an unexpected response. Please try again.");
  }

  return {
    exceptionType: body.exceptionType || exceptionType,
    severity: body.severity || "UNKNOWN",
    rootCause: body.rootCause,
    possibleCauses: Array.isArray(body.possibleCauses) ? body.possibleCauses : [],
    suggestedFix: body.suggestedFix,
    prevention: Array.isArray(body.prevention) ? body.prevention : [],
  };
}

async function readJsonSafely(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
}

function isValidAnalysis(body) {
  return (
    body !== null &&
    typeof body === "object" &&
    typeof body.rootCause === "string" &&
    typeof body.suggestedFix === "string"
  );
}

function buildErrorMessage(status, body) {
  if (status === 400) {
    const fieldErrors = body && body.errors ? Object.entries(body.errors) : [];
    if (fieldErrors.length > 0) {
      const details = fieldErrors
        .map(([field, text]) => `${FIELD_LABELS[field] || field}: ${text}`)
        .join("; ");
      return `The request was rejected. ${details}`;
    }
    return "The request was rejected. Please check the entered values and try again.";
  }

  if (status === 502) {
    return "The AI service is currently unavailable. Please try again in a moment.";
  }

  if (status === 504) {
    return "Could not reach the analysis server. Please make sure the backend is running and try again.";
  }

  if (status >= 500) {
    return "The server encountered an error while analyzing the exception. Please try again.";
  }

  return `The request failed (HTTP ${status}). Please try again.`;
}
