using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace OrderManagement.Infrastructure.Config;

/// <summary>
/// Extension methods for registering Infrastructure layer services.
/// </summary>
public static class InfrastructureServiceExtensions
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        // TODO: Add DbContext, Repositories, and other infrastructure registrations
        // services.AddDbContext<OrderManagementDbContext>(options =>
        //     options.UseNpgsql(configuration.GetConnectionString("DefaultConnection")));
        return services;
    }
}
