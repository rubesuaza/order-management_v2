using FluentAssertions;
using OrderManagement.Domain.Entities;
using OrderManagement.Domain.Enums;
using OrderManagement.Domain.ValueObjects;
using OrderManagement.Infrastructure.Mapping;
using OrderManagement.Infrastructure.Persistence.Entities;
using Xunit;

namespace OrderManagement.Infrastructure.Tests.Mapping;

public class OrderMapperTests
{
    [Fact]
    public void ToDomain_OrderEntity_ReturnsOrderWithCorrectProperties()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var productId = Guid.NewGuid();
        var createdAt = DateTime.UtcNow.AddHours(-1);

        var entity = new OrderEntity
        {
            Id = orderId,
            CustomerId = customerId,
            Status = OrderStatus.Pending.ToString(),
            TotalAmount = 25.50m,
            Currency = "USD",
            CreatedAt = createdAt,
            Items =
            [
                new OrderItemEntity
                {
                    Id = Guid.NewGuid(),
                    OrderId = orderId,
                    ProductId = productId,
                    Quantity = 2,
                    UnitPrice = 10.00m
                },
                new OrderItemEntity
                {
                    Id = Guid.NewGuid(),
                    OrderId = orderId,
                    ProductId = Guid.NewGuid(),
                    Quantity = 1,
                    UnitPrice = 5.50m
                }
            ]
        };

        // Act
        var order = OrderMapper.ToDomain(entity);

        // Assert
        order.Should().NotBeNull();
        order.Id.Should().Be(orderId);
        order.CustomerId.Should().Be(customerId);
        order.Status.Should().Be(OrderStatus.Pending);
        order.CreatedAt.Should().Be(createdAt);
        order.Items.Should().HaveCount(2);
        order.TotalAmount.Amount.Should().Be(25.50m); // 2*10 + 1*5.50 = 25.50
        order.TotalAmount.Currency.Should().Be("USD");

        var firstItem = order.Items.First(i => i.ProductId == productId);
        firstItem.Quantity.Should().Be(2);
        firstItem.UnitPrice.Amount.Should().Be(10.00m);
    }

    [Fact]
    public void ToPersistence_Order_ReturnsOrderEntityAndItemEntities()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var productId = Guid.NewGuid();
        var item = new OrderItem(productId, 3, new Money(5.25m, "USD"));
        var order = new Order(orderId, customerId, [item]);

        // Act
        var (orderEntity, itemEntities) = OrderMapper.ToPersistence(order);

        // Assert
        orderEntity.Should().NotBeNull();
        orderEntity.Id.Should().Be(orderId);
        orderEntity.CustomerId.Should().Be(customerId);
        orderEntity.Status.Should().Be(OrderStatus.Pending.ToString());
        orderEntity.TotalAmount.Should().Be(15.75m); // 3 * 5.25
        orderEntity.Currency.Should().Be("USD");

        itemEntities.Should().HaveCount(1);
        itemEntities[0].OrderId.Should().Be(orderId);
        itemEntities[0].ProductId.Should().Be(productId);
        itemEntities[0].Quantity.Should().Be(3);
        itemEntities[0].UnitPrice.Should().Be(5.25m);
        itemEntities[0].Id.Should().NotBeEmpty();
    }

    [Fact]
    public void UpdatePersistence_Order_UpdatesEntityStatusAndTotal()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var item = new OrderItem(Guid.NewGuid(), 2, new Money(10m, "USD"));
        var order = new Order(orderId, customerId, [item]);
        order.MarkAsPaid();

        var entity = new OrderEntity
        {
            Id = orderId,
            CustomerId = customerId,
            Status = OrderStatus.Pending.ToString(),
            TotalAmount = 20m,
            Currency = "USD",
            CreatedAt = order.CreatedAt
        };

        // Act
        OrderMapper.UpdatePersistence(entity, order);

        // Assert
        entity.Status.Should().Be(OrderStatus.Paid.ToString());
        entity.TotalAmount.Should().Be(20m);
        entity.Currency.Should().Be("USD");
    }

    [Fact]
    public void ToDomain_ToPersistence_RoundTripPreservesData()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var productId = Guid.NewGuid();
        var item = new OrderItem(productId, 4, new Money(7.50m, "EUR"));
        var originalOrder = Order.Reconstitute(orderId, customerId, [item], OrderStatus.Shipped, DateTime.UtcNow);

        var (orderEntity, itemEntities) = OrderMapper.ToPersistence(originalOrder);
        orderEntity.Items = itemEntities;

        // Act
        var roundTrippedOrder = OrderMapper.ToDomain(orderEntity);

        // Assert
        roundTrippedOrder.Id.Should().Be(originalOrder.Id);
        roundTrippedOrder.CustomerId.Should().Be(originalOrder.CustomerId);
        roundTrippedOrder.Status.Should().Be(originalOrder.Status);
        roundTrippedOrder.TotalAmount.Amount.Should().Be(originalOrder.TotalAmount.Amount);
        roundTrippedOrder.TotalAmount.Currency.Should().Be(originalOrder.TotalAmount.Currency);
        roundTrippedOrder.Items.Should().HaveCount(1);
        roundTrippedOrder.Items.First().ProductId.Should().Be(productId);
        roundTrippedOrder.Items.First().Quantity.Should().Be(4);
        roundTrippedOrder.Items.First().UnitPrice.Amount.Should().Be(7.50m);
    }
}
