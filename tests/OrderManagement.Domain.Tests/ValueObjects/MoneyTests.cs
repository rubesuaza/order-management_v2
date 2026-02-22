using FluentAssertions;
using OrderManagement.Domain.Exceptions;
using OrderManagement.Domain.ValueObjects;
using Xunit;

namespace OrderManagement.Domain.Tests.ValueObjects;

public class MoneyTests
{
    [Fact]
    public void Money_DefaultCurrency_IsUsd()
    {
        var money = new Money(10.50m);
        money.Currency.Should().Be("USD");
        money.Amount.Should().Be(10.50m);
    }

    [Fact]
    public void Add_SameCurrency_ReturnsSum()
    {
        var a = new Money(10m, "USD");
        var b = new Money(5m, "USD");
        var result = a.Add(b);
        result.Amount.Should().Be(15m);
        result.Currency.Should().Be("USD");
    }

    [Fact]
    public void Add_DifferentCurrency_ThrowsCurrencyMismatchException()
    {
        var usd = new Money(10m, "USD");
        var eur = new Money(5m, "EUR");

        var act = () => usd.Add(eur);
        act.Should().Throw<CurrencyMismatchException>()
            .WithMessage("*currencies differ*");
    }

    [Fact]
    public void Subtract_SameCurrency_ReturnsDifference()
    {
        var a = new Money(10m, "USD");
        var b = new Money(3m, "USD");
        var result = a.Subtract(b);
        result.Amount.Should().Be(7m);
    }

    [Fact]
    public void Subtract_DifferentCurrency_ThrowsCurrencyMismatchException()
    {
        var usd = new Money(10m, "USD");
        var eur = new Money(5m, "EUR");

        var act = () => usd.Subtract(eur);
        act.Should().Throw<CurrencyMismatchException>();
    }

    [Fact]
    public void Multiply_ReturnsCorrectAmount()
    {
        var money = new Money(2.50m, "USD");
        var result = money.Multiply(4);
        result.Amount.Should().Be(10m);
        result.Currency.Should().Be("USD");
    }

    [Fact]
    public void Zero_ReturnsZeroAmount()
    {
        var zero = Money.Zero("USD");
        zero.Amount.Should().Be(0);
        zero.Currency.Should().Be("USD");
    }
}
