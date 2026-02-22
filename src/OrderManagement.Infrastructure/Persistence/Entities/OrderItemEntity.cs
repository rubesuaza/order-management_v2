namespace OrderManagement.Infrastructure.Persistence.Entities;

/// <summary>
/// EF Core entity for the order_items table.
/// </summary>
public sealed class OrderItemEntity
{
    public Guid Id { get; set; }
    public Guid OrderId { get; set; }
    public Guid ProductId { get; set; }
    public int Quantity { get; set; }
    public decimal UnitPrice { get; set; }

    public OrderEntity Order { get; set; } = null!;
}
