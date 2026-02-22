namespace OrderManagement.Domain.ValueObjects;

public readonly record struct Address(string Street, string City, string ZipCode, string Country);
