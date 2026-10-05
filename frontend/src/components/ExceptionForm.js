import { useState } from "react";

// Limits match the backend's validation rules.
const MAX_LENGTHS = {
  exceptionType: 200,
  message: 2000,
  stackTrace: 20000,
};

function validate(values) {
  const errors = {};
  if (!values.exceptionType.trim()) {
    errors.exceptionType = "Exception Type is required.";
  }
  if (!values.message.trim()) {
    errors.message = "Exception Message is required.";
  }
  if (!values.stackTrace.trim()) {
    errors.stackTrace = "Stack Trace is required.";
  }
  return errors;
}

function ExceptionForm({ onSubmit, isLoading }) {
  const [values, setValues] = useState({
    exceptionType: "",
    message: "",
    stackTrace: "",
  });
  const [errors, setErrors] = useState({});

  function handleChange(event) {
    const { name, value } = event.target;
    setValues((previous) => ({ ...previous, [name]: value }));

    // Clear a field's error as soon as the user starts fixing it.
    if (errors[name]) {
      setErrors((previous) => ({ ...previous, [name]: undefined }));
    }
  }

  function handleSubmit(event) {
    event.preventDefault();

    const validationErrors = validate(values);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    onSubmit({
      exceptionType: values.exceptionType.trim(),
      message: values.message.trim(),
      stackTrace: values.stackTrace.trim(),
    });
  }

  return (
    <form className="card exception-form" onSubmit={handleSubmit} noValidate>
      <FormField id="exceptionType" label="Exception Type" error={errors.exceptionType}>
        <input
          id="exceptionType"
          name="exceptionType"
          type="text"
          placeholder="java.lang.NullPointerException"
          value={values.exceptionType}
          onChange={handleChange}
          maxLength={MAX_LENGTHS.exceptionType}
          disabled={isLoading}
          aria-invalid={Boolean(errors.exceptionType)}
          autoComplete="off"
          spellCheck="false"
        />
      </FormField>

      <FormField id="message" label="Exception Message" error={errors.message}>
        <textarea
          id="message"
          name="message"
          rows={3}
          placeholder={'Cannot invoke "User.getName()" because "user" is null'}
          value={values.message}
          onChange={handleChange}
          maxLength={MAX_LENGTHS.message}
          disabled={isLoading}
          aria-invalid={Boolean(errors.message)}
          spellCheck="false"
        />
      </FormField>

      <FormField id="stackTrace" label="Stack Trace" error={errors.stackTrace}>
        <textarea
          id="stackTrace"
          name="stackTrace"
          className="stack-trace-input"
          rows={10}
          placeholder={
            "java.lang.NullPointerException: ...\n" +
            "\tat com.example.UserService.getUserName(UserService.java:42)\n" +
            "\tat com.example.UserController.getUser(UserController.java:25)"
          }
          value={values.stackTrace}
          onChange={handleChange}
          maxLength={MAX_LENGTHS.stackTrace}
          disabled={isLoading}
          aria-invalid={Boolean(errors.stackTrace)}
          spellCheck="false"
        />
      </FormField>

      <button type="submit" className="analyze-button" disabled={isLoading}>
        {isLoading ? "Analyzing..." : "Analyze Exception"}
      </button>
    </form>
  );
}

function FormField({ id, label, error, children }) {
  return (
    <div className="form-field">
      <label htmlFor={id}>{label}</label>
      {children}
      {error && <p className="field-error">{error}</p>}
    </div>
  );
}

export default ExceptionForm;
