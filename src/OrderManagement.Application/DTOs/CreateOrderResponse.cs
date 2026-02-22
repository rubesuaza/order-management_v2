namespace OrderManagement.Application.DTOs;

public record CreateOrderResponse(Guid OrderId, string Status, decimal TotalAmount, DateTime CreatedAt);
