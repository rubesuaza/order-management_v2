namespace OrderManagement.Domain.ValueObjects;

public readonly record struct Money(decimal Amount, string Currency = "USD")
{
    public static Money Zero(string currency = "USD") => new(0, currency);

    public Money Add(Money other)
    {
        EnsureSameCurrency(other);
        return new Money(Amount + other.Amount, Currency);
    }

    public Money Subtract(Money other)
    {
        EnsureSameCurrency(other);
        return new Money(Amount - other.Amount, Currency);
    }

    public Money Multiply(int factor) => new(Amount * factor, Currency);

    private void EnsureSameCurrency(Money other)
    {
        if (!string.Equals(Currency, other.Currency, StringComparison.OrdinalIgnoreCase))
            throw new Exceptions.CurrencyMismatchException(
                $"Cannot perform operation: currencies differ ({Currency} vs {other.Currency})");
    }
}
