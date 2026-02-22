namespace OrderManagement.Domain.Exceptions;

public sealed class InvalidItemException : DomainException
{
    public InvalidItemException(string message) : base(message) { }
}
