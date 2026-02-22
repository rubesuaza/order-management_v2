namespace OrderManagement.Application.Ports;

/// <summary>
/// Port for managing transactional boundaries across repositories.
/// </summary>
public interface IUnitOfWork : IAsyncDisposable
{
    Task<int> SaveChangesAsync(CancellationToken cancellationToken = default);
}
