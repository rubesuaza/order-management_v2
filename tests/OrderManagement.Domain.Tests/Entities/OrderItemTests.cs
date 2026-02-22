using FluentAssertions;
using OrderManagement.Domain.Entities;
using OrderManagement.Domain.Exceptions;
using OrderManagement.Domain.ValueObjects;
using Xunit;

namespace OrderManagement.Domain.Tests.Entities;

public class OrderItemTests
{
    [Fact]
    public void OrderItem_ValidData_CreatesSuccessfully()
    {
        var item = new OrderItem(Guid.NewGuid(), 2, new Money(10.00m, "USD"));
        item.Quantity.Should().Be(2);
        item.UnitPrice.Amount.Should().Be(10.00m);
        item.LineTotal.Amount.Should().Be(20.00m);
    }

    [Fact]
    public void OrderItem_ZeroQuantity_ThrowsInvalidItemException()
    {
        var act = () => new OrderItem(Guid.NewGuid(), 0, new Money(10m));
        act.Should().Throw<InvalidItemException>()
            .WithMessage("*Quantity*must be strictly greater than zero*");
    }

    [Fact]
    public void OrderItem_NegativeQuantity_ThrowsInvalidItemException()
    {
        var act = () => new OrderItem(Guid.NewGuid(), -1, new Money(10m));
        act.Should().Throw<InvalidItemException>();
    }

    [Fact]
    public void OrderItem_NegativeUnitPrice_ThrowsInvalidItemException()
    {
        var act = () => new OrderItem(Guid.NewGuid(), 1, new Money(-5m));
        act.Should().Throw<InvalidItemException>()
            .WithMessage("*Unit price*cannot be negative*");
    }

    [Fact]
    public void OrderItem_LineTotal_CalculatesCorrectly()
    {
        var item = new OrderItem(Guid.NewGuid(), 3, new Money(2.50m, "USD"));
        item.LineTotal.Amount.Should().Be(7.50m);
    }
}
