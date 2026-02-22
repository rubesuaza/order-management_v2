using Microsoft.EntityFrameworkCore;
using OrderManagement.Application.Ports;
using OrderManagement.Domain.Entities;
using OrderManagement.Infrastructure.Mapping;
using OrderManagement.Infrastructure.Persistence;
using OrderManagement.Infrastructure.Persistence.Entities;

namespace OrderManagement.Infrastructure.Repositories;

/// <summary>
/// EF Core implementation of IOrderRepository.
/// </summary>
public sealed class OrderRepository : IOrderRepository
{
    private readonly OrderManagementDbContext _context;

    public OrderRepository(OrderManagementDbContext context)
    {
        _context = context;
    }

    public async Task AddAsync(Order order, CancellationToken cancellationToken = default)
    {
        var (orderEntity, itemEntities) = OrderMapper.ToPersistence(order);
        await _context.Orders.AddAsync(orderEntity, cancellationToken);
        await _context.OrderItems.AddRangeAsync(itemEntities, cancellationToken);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task<Order?> GetByIdAsync(Guid orderId, CancellationToken cancellationToken = default)
    {
        var entity = await _context.Orders
            .AsNoTracking()
            .Include(o => o.Items)
            .FirstOrDefaultAsync(o => o.Id == orderId, cancellationToken);

        return entity is null ? null : OrderMapper.ToDomain(entity);
    }

    public async Task UpdateAsync(Order order, CancellationToken cancellationToken = default)
    {
        var entity = await _context.Orders
            .Include(o => o.Items)
            .FirstOrDefaultAsync(o => o.Id == order.Id, cancellationToken);

        if (entity is null)
            throw new InvalidOperationException($"Order {order.Id} not found for update.");

        OrderMapper.UpdatePersistence(entity, order);
        await _context.SaveChangesAsync(cancellationToken);
    }
}
