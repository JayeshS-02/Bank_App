package com.bank.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CustomerTest {

    // NEGATIVE
    @Test
    void constructorShouldRejectNullId() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(null, "Alice"));
    }

    // NEGATIVE
    @Test
    void constructorShouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Customer("C001", "   "));
    }

    // POSITIVE
    @Test
    void gettersShouldReturnTrimmedValues() {
        Customer c = new Customer("  C001  ", "  Alice  ");
        assertEquals("C001", c.getCustomerId());
        assertEquals("Alice", c.getName());
    }

    // POSITIVE
    @Test
    void setNameShouldTrimAndUpdate() {
        Customer c = new Customer("C001", "Alice");
        c.setName("  Alicia  ");
        assertEquals("Alicia", c.getName());
    }

    // POSITIVE:
    @Test
    void customerIdRemainsUnchangedAfterNameUpdate() {
        Customer c = new Customer("C777", "Bob");
        c.setName("Robert");
        assertEquals("C777", c.getCustomerId());
        assertEquals("Robert", c.getName());
    }
}