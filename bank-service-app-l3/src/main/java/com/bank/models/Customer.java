package com.bank.models;

public class Customer {
	private final String customerId;
	private String name;

	public Customer(String customerId, String name) {
		if (customerId == null || customerId.trim().isEmpty())
			throw new IllegalArgumentException("customerId cannot be null/blank");

		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("name cannot be null/blank");

		this.customerId = customerId.trim();
		this.name = name.trim();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("name cannot be null/blank");
		this.name = name.trim(); 
	}

	public String getCustomerId() {
		return customerId;
	}
	

}