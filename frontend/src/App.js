import { useState } from "react";
import Header from "./components/Header";
import ExceptionForm from "./components/ExceptionForm";
import LoadingState from "./components/LoadingState";
import AnalysisResult from "./components/AnalysisResult";
import { analyzeException } from "./services/exceptionAnalyzerApi";

function App() {
  const [analysis, setAnalysis] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function handleAnalyze(exceptionDetails) {
    setIsLoading(true);
    setErrorMessage("");
    setAnalysis(null);

    try {
      const result = await analyzeException(exceptionDetails);
      setAnalysis(result);
    } catch (error) {
      setErrorMessage(error.message || "Something went wrong. Please try again.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="app">
      <Header />

      <main className="container main-content">
        <ExceptionForm onSubmit={handleAnalyze} isLoading={isLoading} />

        <div className="output-area">
          {isLoading && <LoadingState />}

          {!isLoading && errorMessage && (
            <div className="error-banner" role="alert">
              <strong>Analysis failed.</strong> {errorMessage}
            </div>
          )}

          {!isLoading && analysis && <AnalysisResult analysis={analysis} />}

          {!isLoading && !analysis && !errorMessage && (
            <p className="empty-state">
              Enter an exception above to get an AI-powered analysis.
            </p>
          )}
        </div>
      </main>
    </div>
  );
}

export default App;
