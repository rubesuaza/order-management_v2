using OrderManagement.Application.DTOs;
using OrderManagement.Application.Exceptions;
using OrderManagement.Application.Ports;
using OrderManagement.Domain.Entities;
using OrderManagement.Domain.Enums;
using OrderManagement.Domain.Exceptions;
using OrderManagement.Domain.ValueObjects;

namespace OrderManagement.Application.Services;

/// <summary>
/// Application service that orchestrates order use cases.
/// </summary>
public sealed class OrderService : IOrderService
{
    private readonly IOrderRepository _orderRepository;
    private readonly IUnitOfWork _unitOfWork;

    public OrderService(IOrderRepository orderRepository, IUnitOfWork unitOfWork)
    {
        _orderRepository = orderRepository;
        _unitOfWork = unitOfWork;
    }

    public async Task<CreateOrderResponse> CreateOrderAsync(CreateOrderRequest request, CancellationToken cancellationToken = default)
    {
        try
        {
            var items = request.Items
                .Select(i => new OrderItem(i.ProductId, i.Quantity, new Money(i.UnitPrice)))
                .ToList();

            var order = new Order(Guid.NewGuid(), request.CustomerId, items);
            await _orderRepository.AddAsync(order, cancellationToken);
            await _unitOfWork.SaveChangesAsync(cancellationToken);

            var total = order.TotalAmount;
            return new CreateOrderResponse(
                order.Id,
                order.Status.ToString(),
                total.Amount,
                order.CreatedAt);
        }
        catch (InvalidItemException ex)
        {
            throw new ApplicationValidationException(ex.Message);
        }
        catch (CurrencyMismatchException ex)
        {
            throw new ApplicationValidationException(ex.Message);
        }
    }

    public async Task<OrderDetailResponse?> GetOrderByIdAsync(Guid orderId, CancellationToken cancellationToken = default)
    {
        var order = await _orderRepository.GetByIdAsync(orderId, cancellationToken);
        if (order is null) return null;

        var items = order.Items
            .Select(i => new OrderItemDetailResponse(
                i.ProductId,
                i.Quantity,
                i.UnitPrice.Amount,
                i.LineTotal.Amount))
            .ToList();

        var total = order.TotalAmount;
        return new OrderDetailResponse(
            order.Id,
            order.CustomerId,
            order.Status.ToString(),
            items,
            total.Amount,
            total.Currency);
    }

    public async Task<PayOrderResponse?> PayOrderAsync(Guid orderId, CancellationToken cancellationToken = default)
    {
        var order = await _orderRepository.GetByIdAsync(orderId, cancellationToken);
        if (order is null) return null;

        try
        {
            order.MarkAsPaid();
            await _orderRepository.UpdateAsync(order, cancellationToken);
            await _unitOfWork.SaveChangesAsync(cancellationToken);

            return new PayOrderResponse(order.Id, order.Status.ToString());
        }
        catch (InvalidOrderStateException ex)
        {
            throw new ApplicationConflictException(ex.Message);
        }
    }
}
