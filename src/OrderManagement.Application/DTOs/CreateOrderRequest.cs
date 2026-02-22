namespace OrderManagement.Application.DTOs;

public record CreateOrderRequest(Guid CustomerId, IReadOnlyList<CreateOrderItemRequest> Items);

public record CreateOrderItemRequest(Guid ProductId, int Quantity, decimal UnitPrice);
