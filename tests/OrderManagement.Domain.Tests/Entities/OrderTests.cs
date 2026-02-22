using FluentAssertions;
using OrderManagement.Domain.Entities;
using OrderManagement.Domain.Enums;
using OrderManagement.Domain.Exceptions;
using OrderManagement.Domain.ValueObjects;
using Xunit;

namespace OrderManagement.Domain.Tests.Entities;

public class OrderTests
{
    private static OrderItem CreateItem(int quantity = 2, decimal unitPrice = 5m) =>
        new(Guid.NewGuid(), quantity, new Money(unitPrice, "USD"));

    [Fact]
    public void Order_WithValidItems_CreatesSuccessfully()
    {
        var items = new[] { CreateItem(2, 5m), CreateItem(1, 10m) };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);

        order.Status.Should().Be(OrderStatus.Pending);
        order.Items.Should().HaveCount(2);
        order.TotalAmount.Amount.Should().Be(20m); // 2*5 + 1*10
    }

    [Fact]
    public void Order_EmptyItems_ThrowsInvalidItemException()
    {
        var act = () => new Order(Guid.NewGuid(), Guid.NewGuid(), []);
        act.Should().Throw<InvalidItemException>()
            .WithMessage("*at least one item*");
    }

    [Fact]
    public void Order_ItemsWithDifferentCurrencies_ThrowsCurrencyMismatchException()
    {
        var items = new[]
        {
            new OrderItem(Guid.NewGuid(), 1, new Money(10m, "USD")),
            new OrderItem(Guid.NewGuid(), 1, new Money(10m, "EUR"))
        };
        var act = () => new Order(Guid.NewGuid(), Guid.NewGuid(), items);
        act.Should().Throw<CurrencyMismatchException>()
            .WithMessage("*same currency*");
    }

    [Fact]
    public void MarkAsPaid_WhenTotalBelow10_ThrowsInvalidOrderStateException()
    {
        var items = new[] { CreateItem(1, 5m) }; // Total = 5 USD
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);

        var act = () => order.MarkAsPaid();
        act.Should().Throw<InvalidOrderStateException>()
            .WithMessage("*at least*10*");
    }

    [Fact]
    public void MarkAsPaid_WhenTotalAtLeast10_Succeeds()
    {
        var items = new[] { CreateItem(2, 5m) }; // Total = 10 USD
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);

        order.MarkAsPaid();
        order.Status.Should().Be(OrderStatus.Paid);
    }

    [Fact]
    public void MarkAsPaid_WhenNotPending_ThrowsInvalidOrderStateException()
    {
        var items = new[] { CreateItem(5, 5m) }; // Total = 25 USD
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);
        order.MarkAsPaid();

        var act = () => order.MarkAsPaid();
        act.Should().Throw<InvalidOrderStateException>()
            .WithMessage("*Pending*");
    }

    [Fact]
    public void Ship_WhenPaid_Succeeds()
    {
        var items = new[] { CreateItem(5, 5m) };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);
        order.MarkAsPaid();

        order.Ship();
        order.Status.Should().Be(OrderStatus.Shipped);
    }

    [Fact]
    public void Ship_WhenNotPaid_ThrowsInvalidOrderStateException()
    {
        var items = new[] { CreateItem(5, 5m) };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);

        var act = () => order.Ship();
        act.Should().Throw<InvalidOrderStateException>()
            .WithMessage("*Paid*");
    }

    [Fact]
    public void Cancel_WhenPending_Succeeds()
    {
        var items = new[] { CreateItem(5, 5m) };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);

        order.Cancel();
        order.Status.Should().Be(OrderStatus.Cancelled);
    }

    [Fact]
    public void Cancel_WhenPaid_Succeeds()
    {
        var items = new[] { CreateItem(5, 5m) };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);
        order.MarkAsPaid();

        order.Cancel();
        order.Status.Should().Be(OrderStatus.Cancelled);
    }

    [Fact]
    public void Cancel_WhenShipped_ThrowsInvalidOrderStateException()
    {
        var items = new[] { CreateItem(5, 5m) };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);
        order.MarkAsPaid();
        order.Ship();

        var act = () => order.Cancel();
        act.Should().Throw<InvalidOrderStateException>()
            .WithMessage("*Pending*Paid*");
    }

    [Fact]
    public void Deliver_WhenShipped_Succeeds()
    {
        var items = new[] { CreateItem(5, 5m) };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);
        order.MarkAsPaid();
        order.Ship();

        order.Deliver();
        order.Status.Should().Be(OrderStatus.Delivered);
    }

    [Fact]
    public void TotalAmount_CalculatesCorrectly()
    {
        var items = new[]
        {
            CreateItem(3, 2.50m),
            CreateItem(2, 10m)
        };
        var order = new Order(Guid.NewGuid(), Guid.NewGuid(), items);
        // 3*2.50 + 2*10 = 7.50 + 20 = 27.50
        order.TotalAmount.Amount.Should().Be(27.50m);
    }
}
