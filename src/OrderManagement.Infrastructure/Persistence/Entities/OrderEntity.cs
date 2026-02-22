namespace OrderManagement.Infrastructure.Persistence.Entities;

/// <summary>
/// EF Core entity for the orders table.
/// </summary>
public sealed class OrderEntity
{
    public Guid Id { get; set; }
    public Guid CustomerId { get; set; }
    public string Status { get; set; } = null!;
    public decimal TotalAmount { get; set; }
    public string Currency { get; set; } = null!;
    public DateTime CreatedAt { get; set; }

    public ICollection<OrderItemEntity> Items { get; set; } = [];
}
