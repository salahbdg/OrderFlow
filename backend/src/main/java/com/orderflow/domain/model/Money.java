package com.orderflow.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object representing a monetary amount with currency.
 *
 * WHY A VALUE OBJECT?
 * Money has no identity. €10 is €10 — it doesn't have an ID.
 * Two Money objects with the same amount and currency ARE equal.
 * Value Objects are always immutable — every operation returns a NEW instance.
 *
 * WHY BigDecimal AND NOT double?
 * Never use double for money. double cannot represent 0.1 exactly in binary.
 * Try this in any Java REPL: System.out.println(0.1 + 0.2) → 0.30000000000000004
 * That's a bug in a financial system. BigDecimal is exact.
 */
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    // Private constructor — force use of factory methods
    private Money(BigDecimal amount, Currency currency) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (currency == null) throw new IllegalArgumentException("Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        // Always store with exactly 2 decimal places
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    // Factory method — reads better than 'new Money(...)'
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public static Money ofEuros(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }

    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, Currency.getInstance(currencyCode));
    }

    // Operations return NEW instances — immutability
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot operate on different currencies: "
                + this.currency + " vs " + other.currency
            );
        }
    }

    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }
    public String getCurrencyCode() { return currency.getCurrencyCode(); }

    // Value Objects use value-based equality, NOT reference equality
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return Objects.equals(amount, money.amount) &&
               Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency.getCurrencyCode();
    }
}