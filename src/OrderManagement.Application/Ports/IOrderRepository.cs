using OrderManagement.Domain.Entities;

namespace OrderManagement.Application.Ports;

/// <summary>
/// Port for persisting and retrieving Order aggregates.
/// </summary>
public interface IOrderRepository
{
    Task AddAsync(Order order, CancellationToken cancellationToken = default);
    Task<Order?> GetByIdAsync(Guid orderId, CancellationToken cancellationToken = default);
    Task UpdateAsync(Order order, CancellationToken cancellationToken = default);
}
