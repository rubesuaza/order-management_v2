using OrderManagement.Application.Ports;

namespace OrderManagement.Infrastructure.Persistence;

/// <summary>
/// EF Core implementation of IUnitOfWork using the DbContext.
/// DbContext lifecycle is managed by DI; this adapter delegates SaveChanges only.
/// </summary>
public sealed class UnitOfWork : IUnitOfWork
{
    private readonly OrderManagementDbContext _context;

    public UnitOfWork(OrderManagementDbContext context)
    {
        _context = context;
    }

    public Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
    {
        return _context.SaveChangesAsync(cancellationToken);
    }

    public ValueTask DisposeAsync() => ValueTask.CompletedTask;
}
