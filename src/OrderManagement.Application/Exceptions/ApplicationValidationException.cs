namespace OrderManagement.Application.Exceptions;

/// <summary>
/// Application-level exception for validation errors (maps to HTTP 400).
/// </summary>
public sealed class ApplicationValidationException : Exception
{
    public ApplicationValidationException(string message) : base(message) { }

    public ApplicationValidationException(string message, Exception innerException)
        : base(message, innerException) { }
}
