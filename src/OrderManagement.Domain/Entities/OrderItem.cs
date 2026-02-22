using OrderManagement.Domain.Exceptions;
using OrderManagement.Domain.ValueObjects;

namespace OrderManagement.Domain.Entities;

public sealed class OrderItem
{
    public Guid ProductId { get; }
    public int Quantity { get; }
    public Money UnitPrice { get; }

    public Money LineTotal => UnitPrice.Multiply(Quantity);

    public OrderItem(Guid productId, int quantity, Money unitPrice)
    {
        if (quantity <= 0)
            throw new InvalidItemException("Quantity must be strictly greater than zero.");

        if (unitPrice.Amount < 0)
            throw new InvalidItemException("Unit price cannot be negative.");

        ProductId = productId;
        Quantity = quantity;
        UnitPrice = unitPrice;
    }
}
