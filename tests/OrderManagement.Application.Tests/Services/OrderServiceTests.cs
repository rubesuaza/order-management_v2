using FluentAssertions;
using Moq;
using OrderManagement.Application.DTOs;
using OrderManagement.Application.Ports;
using OrderManagement.Application.Services;
using OrderManagement.Domain.Entities;
using OrderManagement.Domain.Enums;
using OrderManagement.Domain.Exceptions;
using OrderManagement.Domain.ValueObjects;
using Xunit;

namespace OrderManagement.Application.Tests.Services;

public class OrderServiceTests
{
    private readonly Mock<IOrderRepository> _orderRepositoryMock;
    private readonly Mock<IUnitOfWork> _unitOfWorkMock;
    private readonly OrderService _sut;

    public OrderServiceTests()
    {
        _orderRepositoryMock = new Mock<IOrderRepository>();
        _unitOfWorkMock = new Mock<IUnitOfWork>();
        _unitOfWorkMock.Setup(u => u.SaveChangesAsync(It.IsAny<CancellationToken>())).ReturnsAsync(1);
        _sut = new OrderService(_orderRepositoryMock.Object, _unitOfWorkMock.Object);
    }

    [Fact]
    public async Task CreateOrderAsync_ValidRequest_ReturnsCreateOrderResponse()
    {
        // Arrange
        var customerId = Guid.NewGuid();
        var productId = Guid.NewGuid();
        var request = new CreateOrderRequest(
            customerId,
            [new CreateOrderItemRequest(productId, 2, 10.00m)]);

        Order? capturedOrder = null;
        _orderRepositoryMock
            .Setup(r => r.AddAsync(It.IsAny<Order>(), It.IsAny<CancellationToken>()))
            .Callback<Order, CancellationToken>((o, _) => capturedOrder = o)
            .Returns(Task.CompletedTask);

        // Act
        var result = await _sut.CreateOrderAsync(request);

        // Assert
        result.Should().NotBeNull();
        result.OrderId.Should().NotBeEmpty();
        result.Status.Should().Be(OrderStatus.Pending.ToString());
        result.TotalAmount.Should().Be(20.00m); // 2 * 10
        result.CreatedAt.Should().BeCloseTo(DateTime.UtcNow, TimeSpan.FromSeconds(5));

        capturedOrder.Should().NotBeNull();
        capturedOrder!.CustomerId.Should().Be(customerId);
        capturedOrder.Items.Should().HaveCount(1);
        capturedOrder.Items.First().ProductId.Should().Be(productId);
        capturedOrder.Items.First().Quantity.Should().Be(2);
        capturedOrder.Items.First().UnitPrice.Amount.Should().Be(10.00m);

        _orderRepositoryMock.Verify(r => r.AddAsync(It.IsAny<Order>(), It.IsAny<CancellationToken>()), Times.Once);
        _unitOfWorkMock.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task CreateOrderAsync_EmptyItems_ThrowsInvalidItemException()
    {
        // Arrange
        var request = new CreateOrderRequest(Guid.NewGuid(), []);

        // Act
        var act = async () => await _sut.CreateOrderAsync(request);

        // Assert
        await act.Should().ThrowAsync<InvalidItemException>()
            .WithMessage("*at least one item*");
        _orderRepositoryMock.Verify(r => r.AddAsync(It.IsAny<Order>(), It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task CreateOrderAsync_MultipleItems_CalculatesTotalCorrectly()
    {
        // Arrange
        var customerId = Guid.NewGuid();
        var request = new CreateOrderRequest(
            customerId,
            [
                new CreateOrderItemRequest(Guid.NewGuid(), 3, 2.50m),
                new CreateOrderItemRequest(Guid.NewGuid(), 2, 10.00m)
            ]);

        // Act
        var result = await _sut.CreateOrderAsync(request);

        // Assert - 3*2.50 + 2*10 = 7.50 + 20 = 27.50
        result.TotalAmount.Should().Be(27.50m);
    }

    [Fact]
    public async Task GetOrderByIdAsync_OrderExists_ReturnsOrderDetailResponse()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var productId = Guid.NewGuid();
        var item = new OrderItem(productId, 2, new Money(10m, "USD"));
        var order = new Order(orderId, customerId, [item]);

        _orderRepositoryMock
            .Setup(r => r.GetByIdAsync(orderId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(order);

        // Act
        var result = await _sut.GetOrderByIdAsync(orderId);

        // Assert
        result.Should().NotBeNull();
        result!.OrderId.Should().Be(orderId);
        result.CustomerId.Should().Be(customerId);
        result.Status.Should().Be(OrderStatus.Pending.ToString());
        result.TotalAmount.Should().Be(20m);
        result.Currency.Should().Be("USD");
        result.Items.Should().HaveCount(1);
        result.Items[0].ProductId.Should().Be(productId);
        result.Items[0].Quantity.Should().Be(2);
        result.Items[0].UnitPrice.Should().Be(10m);
        result.Items[0].LineTotal.Should().Be(20m);

        _orderRepositoryMock.Verify(r => r.GetByIdAsync(orderId, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task GetOrderByIdAsync_OrderNotFound_ReturnsNull()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        _orderRepositoryMock
            .Setup(r => r.GetByIdAsync(orderId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((Order?)null);

        // Act
        var result = await _sut.GetOrderByIdAsync(orderId);

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task PayOrderAsync_OrderExistsAndValid_MarksAsPaidAndReturnsResponse()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var item = new OrderItem(Guid.NewGuid(), 2, new Money(10m, "USD")); // Total 20 >= 10
        var order = new Order(orderId, customerId, [item]);

        _orderRepositoryMock
            .Setup(r => r.GetByIdAsync(orderId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(order);
        _orderRepositoryMock
            .Setup(r => r.UpdateAsync(It.IsAny<Order>(), It.IsAny<CancellationToken>()))
            .Returns(Task.CompletedTask);

        // Act
        var result = await _sut.PayOrderAsync(orderId);

        // Assert
        result.Should().NotBeNull();
        result!.OrderId.Should().Be(orderId);
        result.Status.Should().Be(OrderStatus.Paid.ToString());
        order.Status.Should().Be(OrderStatus.Paid);

        _orderRepositoryMock.Verify(r => r.GetByIdAsync(orderId, It.IsAny<CancellationToken>()), Times.Once);
        _orderRepositoryMock.Verify(r => r.UpdateAsync(order, It.IsAny<CancellationToken>()), Times.Once);
        _unitOfWorkMock.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task PayOrderAsync_OrderNotFound_ReturnsNull()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        _orderRepositoryMock
            .Setup(r => r.GetByIdAsync(orderId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((Order?)null);

        // Act
        var result = await _sut.PayOrderAsync(orderId);

        // Assert
        result.Should().BeNull();
        _orderRepositoryMock.Verify(r => r.UpdateAsync(It.IsAny<Order>(), It.IsAny<CancellationToken>()), Times.Never);
    }
}
