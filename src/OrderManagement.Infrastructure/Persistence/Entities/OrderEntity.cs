namespace OrderManagement.Infrastructure.Persistence.Entities;

/// <summary>
/// EF Core entity for the orders table.
/// </summary>
public sealed class OrderEntity
{
    public Guid Id { get; set; }
    public Guid CustomerId { get; set; }
    public string Status { get; set; } = string.Empty;
    public decimal TotalAmount { get; set; }
    public string Currency { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }

    public ICollection<OrderItemEntity> Items { get; set; } = [];
}
