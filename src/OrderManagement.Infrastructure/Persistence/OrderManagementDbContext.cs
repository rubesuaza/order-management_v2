using Microsoft.EntityFrameworkCore;
using OrderManagement.Infrastructure.Persistence.Entities;

namespace OrderManagement.Infrastructure.Persistence;

/// <summary>
/// EF Core DbContext for Order Management.
/// </summary>
public sealed class OrderManagementDbContext : DbContext
{
    public OrderManagementDbContext(DbContextOptions<OrderManagementDbContext> options)
        : base(options)
    {
    }

    public DbSet<OrderEntity> Orders => Set<OrderEntity>();
    public DbSet<OrderItemEntity> OrderItems => Set<OrderItemEntity>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<OrderEntity>(entity =>
        {
            entity.ToTable("orders");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CustomerId).HasColumnName("customer_id").IsRequired();
            entity.Property(e => e.Status).HasColumnName("status").HasMaxLength(20).IsRequired();
            entity.Property(e => e.TotalAmount).HasColumnName("total_amount").HasPrecision(19, 2).IsRequired();
            entity.Property(e => e.Currency).HasColumnName("currency").HasMaxLength(3).IsRequired();
            entity.Property(e => e.CreatedAt).HasColumnName("created_at").IsRequired();

            entity.HasIndex(e => e.CustomerId);
            entity.HasIndex(e => e.Status);

            entity.HasMany(e => e.Items)
                .WithOne(i => i.Order)
                .HasForeignKey(i => i.OrderId)
                .IsRequired()
                .OnDelete(DeleteBehavior.Restrict);
            // No ON DELETE CASCADE per spec - maintain audit trail
        });

        modelBuilder.Entity<OrderItemEntity>(entity =>
        {
            entity.ToTable("order_items");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.OrderId).HasColumnName("order_id").IsRequired();
            entity.Property(e => e.ProductId).HasColumnName("product_id").IsRequired();
            entity.Property(e => e.Quantity).HasColumnName("quantity").IsRequired();
            entity.Property(e => e.UnitPrice).HasColumnName("unit_price").HasPrecision(19, 2).IsRequired();
        });
    }
}
