namespace OrderManagement.Application.DTOs;

public record OrderDetailResponse(
    Guid OrderId,
    Guid CustomerId,
    string Status,
    IReadOnlyList<OrderItemDetailResponse> Items,
    decimal TotalAmount,
    string Currency);

public record OrderItemDetailResponse(Guid ProductId, int Quantity, decimal UnitPrice, decimal LineTotal);
