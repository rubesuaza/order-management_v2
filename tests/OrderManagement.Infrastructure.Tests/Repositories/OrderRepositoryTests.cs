using FluentAssertions;
using Microsoft.EntityFrameworkCore;
using OrderManagement.Domain.Entities;
using OrderManagement.Domain.Enums;
using OrderManagement.Domain.ValueObjects;
using OrderManagement.Infrastructure.Persistence;
using OrderManagement.Infrastructure.Repositories;
using Xunit;

namespace OrderManagement.Infrastructure.Tests.Repositories;

public class OrderRepositoryTests : IDisposable
{
    private readonly OrderManagementDbContext _context;
    private readonly OrderRepository _sut;

    public OrderRepositoryTests()
    {
        var options = new DbContextOptionsBuilder<OrderManagementDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        _context = new OrderManagementDbContext(options);
        _sut = new OrderRepository(_context);
    }

    public void Dispose() => _context.Dispose();

    [Fact]
    public async Task AddAsync_Order_PersistsToDatabase()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var productId = Guid.NewGuid();
        var item = new OrderItem(productId, 2, new Money(10m, "USD"));
        var order = new Order(orderId, customerId, [item]);

        // Act
        await _sut.AddAsync(order);
        await _context.SaveChangesAsync();

        // Assert
        var entity = await _context.Orders
            .Include(o => o.Items)
            .FirstOrDefaultAsync(o => o.Id == orderId);

        entity.Should().NotBeNull();
        entity!.CustomerId.Should().Be(customerId);
        Enum.Parse<OrderStatus>(entity.Status).Should().Be(OrderStatus.Pending);
        entity.TotalAmount.Should().Be(20m);
        entity.Currency.Should().Be("USD");
        entity.Items.Should().HaveCount(1);
        entity.Items.First().ProductId.Should().Be(productId);
        entity.Items.First().Quantity.Should().Be(2);
        entity.Items.First().UnitPrice.Should().Be(10m);
    }

    [Fact]
    public async Task GetByIdAsync_OrderExists_ReturnsOrder()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var productId = Guid.NewGuid();
        var item = new OrderItem(productId, 3, new Money(5m, "USD"));
        var order = new Order(orderId, customerId, [item]);
        await _sut.AddAsync(order);
        await _context.SaveChangesAsync();

        // Act
        var result = await _sut.GetByIdAsync(orderId);

        // Assert
        result.Should().NotBeNull();
        result!.Id.Should().Be(orderId);
        result.CustomerId.Should().Be(customerId);
        result.Status.Should().Be(OrderStatus.Pending);
        result.TotalAmount.Amount.Should().Be(15m);
        result.Items.Should().HaveCount(1);
        result.Items.First().ProductId.Should().Be(productId);
    }

    [Fact]
    public async Task GetByIdAsync_OrderNotFound_ReturnsNull()
    {
        // Act
        var result = await _sut.GetByIdAsync(Guid.NewGuid());

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task UpdateAsync_Order_UpdatesPersistedEntity()
    {
        // Arrange
        var orderId = Guid.NewGuid();
        var customerId = Guid.NewGuid();
        var item = new OrderItem(Guid.NewGuid(), 2, new Money(10m, "USD"));
        var order = new Order(orderId, customerId, [item]);
        await _sut.AddAsync(order);
        await _context.SaveChangesAsync();

        var loadedOrder = await _sut.GetByIdAsync(orderId);
        loadedOrder!.MarkAsPaid();

        // Act
        await _sut.UpdateAsync(loadedOrder);
        await _context.SaveChangesAsync();

        // Assert
        var entity = await _context.Orders.FindAsync(orderId);
        entity.Should().NotBeNull();
        Enum.Parse<OrderStatus>(entity!.Status).Should().Be(OrderStatus.Paid);
    }
}
