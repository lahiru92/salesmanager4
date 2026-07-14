package com.example.salesmanager4.invoice;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the invoice request DTO calculations:
 * discount-aware line subtotals, the grand total and the payment balance check.
 */
@DisplayName("Invoice DTO calculations")
class InvoiceRequestDtoTest {

    private InvoiceRequestLineDto line(String qty, String unitPrice, String discount) {
        InvoiceRequestLineDto line = new InvoiceRequestLineDto();
        line.setQuantity(qty != null ? new BigDecimal(qty) : null);
        line.setUnitPrice(unitPrice != null ? new BigDecimal(unitPrice) : null);
        line.setDiscount(discount != null ? new BigDecimal(discount) : null);
        return line;
    }

    @Test
    @DisplayName("Line subtotal = qty x price - discount")
    void lineSubtotalWithDiscount() {
        assertThat(line("10", "100", "50").getSubTotal()).isEqualByComparingTo("950.00");
    }

    @Test
    @DisplayName("Missing discount is treated as zero")
    void lineSubtotalWithoutDiscount() {
        assertThat(line("10", "100", null).getSubTotal()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Line subtotal is zero when qty or price is missing")
    void lineSubtotalWithMissingValues() {
        assertThat(line(null, "100", null).getSubTotal()).isEqualByComparingTo("0");
        assertThat(line("10", null, null).getSubTotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Grand total sums all line subtotals")
    void grandTotal() {
        InvoiceRequestDto invoice = new InvoiceRequestDto();
        invoice.setItems(List.of(line("10", "100", "50"), line("2", "25", null)));

        assertThat(invoice.getGrandTotal()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Balanced when cash + cheque + credit equals the grand total")
    void balancedPayments() {
        InvoiceRequestDto invoice = new InvoiceRequestDto();
        invoice.setItems(List.of(line("10", "100", null)));
        invoice.setCash(new BigDecimal("400"));
        invoice.setCheque(new BigDecimal("100"));
        invoice.setCredit(new BigDecimal("500"));

        assertThat(invoice.isBalanced()).isTrue();
    }

    @Test
    @DisplayName("Not balanced when payments exceed the grand total")
    void unbalancedPayments() {
        InvoiceRequestDto invoice = new InvoiceRequestDto();
        invoice.setItems(List.of(line("10", "100", null)));
        invoice.setCash(new BigDecimal("1200"));

        assertThat(invoice.isBalanced()).isFalse();
    }
}
