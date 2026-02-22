namespace OrderManagement.Application.Exceptions;

/// <summary>
/// Application-level exception for conflict/business rule violations (maps to HTTP 409).
/// </summary>
public sealed class ApplicationConflictException : Exception
{
    public ApplicationConflictException(string message) : base(message) { }
}
