using FluentAssertions;
using OrderManagement.Domain.ValueObjects;
using Xunit;

namespace OrderManagement.Domain.Tests.ValueObjects;

public class AddressTests
{
    [Fact]
    public void Address_StoresAllProperties()
    {
        var address = new Address("123 Main St", "New York", "10001", "USA");
        address.Street.Should().Be("123 Main St");
        address.City.Should().Be("New York");
        address.ZipCode.Should().Be("10001");
        address.Country.Should().Be("USA");
    }
}
