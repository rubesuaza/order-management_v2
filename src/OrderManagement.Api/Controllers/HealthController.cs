using Microsoft.AspNetCore.Mvc;

namespace OrderManagement.Api.Controllers;

/// <summary>
/// Health check endpoint for monitoring.
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class HealthController : ControllerBase
{
    [HttpGet]
    public IActionResult Get() => Ok(new { Status = "Healthy", Timestamp = DateTime.UtcNow });
}
