using OrderManagement.Domain.Entities;
using OrderManagement.Domain.Enums;
using OrderManagement.Domain.ValueObjects;
using OrderManagement.Infrastructure.Persistence.Entities;

namespace OrderManagement.Infrastructure.Mapping;

/// <summary>
/// Maps between Domain Order/OrderItem and Infrastructure OrderEntity/OrderItemEntity.
/// </summary>
public static class OrderMapper
{
    public static Order ToDomain(OrderEntity entity)
    {
        var items = entity.Items
            .Select(i => new OrderItem(i.ProductId, i.Quantity, new Money(i.UnitPrice, entity.Currency)))
            .ToList();

        var status = Enum.Parse<OrderStatus>(entity.Status);
        return Order.Reconstitute(entity.Id, entity.CustomerId, items, status, entity.CreatedAt);
    }

    public static (OrderEntity OrderEntity, List<OrderItemEntity> ItemEntities) ToPersistence(Order domain)
    {
        var total = domain.TotalAmount;
        var currency = total.Currency;

        var orderEntity = new OrderEntity
        {
            Id = domain.Id,
            CustomerId = domain.CustomerId,
            Status = domain.Status.ToString(),
            TotalAmount = total.Amount,
            Currency = currency,
            CreatedAt = domain.CreatedAt
        };

        var itemEntities = domain.Items
            .Select((item, _) => new OrderItemEntity
            {
                Id = Guid.NewGuid(),
                OrderId = domain.Id,
                ProductId = item.ProductId,
                Quantity = item.Quantity,
                UnitPrice = item.UnitPrice.Amount
            })
            .ToList();

        return (orderEntity, itemEntities);
    }

    public static void UpdatePersistence(OrderEntity entity, Order domain)
    {
        entity.Status = domain.Status.ToString();
        var total = domain.TotalAmount;
        entity.TotalAmount = total.Amount;
        entity.Currency = total.Currency;
    }
}
