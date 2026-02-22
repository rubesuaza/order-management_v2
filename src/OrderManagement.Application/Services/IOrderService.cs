using OrderManagement.Application.DTOs;

namespace OrderManagement.Application.Services;

/// <summary>
/// Application port for order operations.
/// </summary>
public interface IOrderService
{
    Task<CreateOrderResponse> CreateOrderAsync(CreateOrderRequest request, CancellationToken cancellationToken = default);
    Task<OrderDetailResponse?> GetOrderByIdAsync(Guid orderId, CancellationToken cancellationToken = default);
    Task<PayOrderResponse?> PayOrderAsync(Guid orderId, CancellationToken cancellationToken = default);
}
