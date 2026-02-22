using OrderManagement.Domain.Enums;
using OrderManagement.Domain.Exceptions;
using OrderManagement.Domain.ValueObjects;

namespace OrderManagement.Domain.Entities;

public sealed class Order
{
    private const decimal MinimumAmountForPayment = 10.00m;
    private readonly List<OrderItem> _items = [];

    public Guid Id { get; }
    public OrderStatus Status { get; private set; }
    public DateTime CreatedAt { get; }
    public Guid CustomerId { get; }
    public IReadOnlyCollection<OrderItem> Items => _items.AsReadOnly();
    public Money TotalAmount => CalculateTotal();

    public Order(Guid id, Guid customerId, IEnumerable<OrderItem> items, DateTime? createdAt = null)
    {
        var itemsList = items.ToList();
        if (itemsList.Count == 0)
            throw new InvalidItemException("An order must have at least one item to be created.");

        EnsureSameCurrency(itemsList);
        Id = id;
        CustomerId = customerId;
        _items.AddRange(itemsList);
        Status = OrderStatus.Pending;
        CreatedAt = createdAt ?? DateTime.UtcNow;
    }

    public void MarkAsPaid()
    {
        if (Status != OrderStatus.Pending)
            throw new InvalidOrderStateException(
                $"Order can only be marked as paid when in Pending state. Current: {Status}.");

        var total = CalculateTotal();
        if (total.Amount < MinimumAmountForPayment)
            throw new InvalidOrderStateException(
                $"Order total must be at least {MinimumAmountForPayment:N2} USD to be processed. Current: {total.Amount:N2} {total.Currency}.");

        Status = OrderStatus.Paid;
    }

    public void Ship()
    {
        if (Status != OrderStatus.Paid)
            throw new InvalidOrderStateException(
                $"Order can only be shipped when in Paid state. Current: {Status}.");

        Status = OrderStatus.Shipped;
    }

    public void Deliver()
    {
        if (Status != OrderStatus.Shipped)
            throw new InvalidOrderStateException(
                $"Order can only be delivered when in Shipped state. Current: {Status}.");

        Status = OrderStatus.Delivered;
    }

    public void Cancel()
    {
        if (Status != OrderStatus.Pending && Status != OrderStatus.Paid)
            throw new InvalidOrderStateException(
                $"Order can only be cancelled when in Pending or Paid state. Current: {Status}.");

        Status = OrderStatus.Cancelled;
    }

    private Money CalculateTotal()
    {
        var firstCurrency = _items[0].UnitPrice.Currency;
        var total = Money.Zero(firstCurrency);
        foreach (var item in _items)
            total = total.Add(item.LineTotal);
        return total;
    }

    private static void EnsureSameCurrency(List<OrderItem> items)
    {
        if (items.Count < 2) return;

        var firstCurrency = items[0].UnitPrice.Currency;
        foreach (var item in items.Skip(1))
        {
            if (!string.Equals(firstCurrency, item.UnitPrice.Currency, StringComparison.OrdinalIgnoreCase))
                throw new CurrencyMismatchException(
                    $"All items must use the same currency. Found {firstCurrency} and {item.UnitPrice.Currency}.");
        }
    }
}
