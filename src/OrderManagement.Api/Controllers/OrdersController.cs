using Microsoft.AspNetCore.Mvc;
using OrderManagement.Application.DTOs;
using OrderManagement.Application.Services;

namespace OrderManagement.Api.Controllers;

/// <summary>
/// REST API for order management.
/// </summary>
[ApiController]
[Route("api/v1/orders")]
public class OrdersController : ControllerBase
{
    private readonly IOrderService _orderService;

    public OrdersController(IOrderService orderService)
    {
        _orderService = orderService;
    }

    /// <summary>
    /// Creates a new order with initial items.
    /// </summary>
    [HttpPost]
    [ProducesResponseType(typeof(CreateOrderResponse), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> CreateOrder(
        [FromBody] CreateOrderRequest request,
        CancellationToken cancellationToken)
    {
        var response = await _orderService.CreateOrderAsync(request, cancellationToken);
        return CreatedAtAction(nameof(GetOrder), new { orderId = response.OrderId }, response);
    }

    /// <summary>
    /// Retrieves order details by ID.
    /// </summary>
    [HttpGet("{orderId:guid}")]
    [ProducesResponseType(typeof(OrderDetailResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetOrder(Guid orderId, CancellationToken cancellationToken)
    {
        var response = await _orderService.GetOrderByIdAsync(orderId, cancellationToken);
        return response is null ? NotFound() : Ok(response);
    }

    /// <summary>
    /// Processes payment for an order (simulated), changing status to Paid.
    /// </summary>
    [HttpPost("{orderId:guid}/pay")]
    [ProducesResponseType(typeof(PayOrderResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    [ProducesResponseType(StatusCodes.Status409Conflict)]
    public async Task<IActionResult> PayOrder(Guid orderId, CancellationToken cancellationToken)
    {
        var response = await _orderService.PayOrderAsync(orderId, cancellationToken);
        if (response is null)
            return NotFound();
        return Ok(response);
    }
}
