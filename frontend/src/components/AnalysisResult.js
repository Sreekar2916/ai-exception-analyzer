const KNOWN_SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

function AnalysisResult({ analysis }) {
  const severity = String(analysis.severity).toUpperCase();
  const severityClass = KNOWN_SEVERITIES.includes(severity)
    ? `severity-${severity.toLowerCase()}`
    : "severity-unknown";

  return (
    <section className="card analysis-result" aria-labelledby="analysis-title">
      <div className="result-header">
        <h2 id="analysis-title">AI Analysis</h2>
        <span className={`severity-badge ${severityClass}`}>{severity}</span>
      </div>

      <div className="result-grid">
        <ResultItem title="Exception Type">
          <code className="exception-type">{analysis.exceptionType}</code>
        </ResultItem>

        <ResultItem title="Severity">
          <span className={`severity-text ${severityClass}`}>{severity}</span>
        </ResultItem>
      </div>

      <ResultItem title="Root Cause" highlight>
        <p>{analysis.rootCause}</p>
      </ResultItem>

      <ResultItem title="Possible Causes">
        <BulletList items={analysis.possibleCauses} />
      </ResultItem>

      <ResultItem title="Suggested Fix" highlight>
        <p>{analysis.suggestedFix}</p>
      </ResultItem>

      <ResultItem title="Prevention">
        <BulletList items={analysis.prevention} />
      </ResultItem>
    </section>
  );
}

function ResultItem({ title, highlight = false, children }) {
  return (
    <div className={highlight ? "result-item result-item-highlight" : "result-item"}>
      <h3>{title}</h3>
      {children}
    </div>
  );
}

function BulletList({ items }) {
  if (items.length === 0) {
    return <p className="muted">None provided.</p>;
  }
  return (
    <ul>
      {items.map((item, index) => (
        <li key={index}>{item}</li>
      ))}
    </ul>
  );
}

export default AnalysisResult;
