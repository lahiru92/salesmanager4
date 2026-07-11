package com.example.salesmanager4.grn;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the GRN request DTO calculations:
 * line subtotals, the grand total and the cash/cheque/credit balance check.
 */
@DisplayName("GRN DTO calculations")
class GrnRequestDtoTest {

    private GrnRequestLineDto line(String acceptedQty, String unitPrice) {
        GrnRequestLineDto line = new GrnRequestLineDto();
        line.setAcceptedQty(acceptedQty != null ? new BigDecimal(acceptedQty) : null);
        line.setUnitPrice(unitPrice != null ? new BigDecimal(unitPrice) : null);
        return line;
    }

    @Test
    @DisplayName("Line subtotal = accepted qty x unit price")
    void lineSubtotal() {
        assertThat(line("10", "25.50").getSubTotal()).isEqualByComparingTo("255.00");
    }

    @Test
    @DisplayName("Line subtotal is zero when price or quantity is missing")
    void lineSubtotalWithMissingValues() {
        assertThat(line("10", null).getSubTotal()).isEqualByComparingTo("0");
        assertThat(line(null, "25.50").getSubTotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Grand total sums all line subtotals")
    void grandTotal() {
        GrnRequestDto grn = new GrnRequestDto();
        grn.setItems(List.of(line("10", "25.50"), line("4", "12.25")));

        assertThat(grn.getGrandTotal()).isEqualByComparingTo("304.00");
    }

    @Test
    @DisplayName("Grand total is zero when there are no items")
    void grandTotalWithoutItems() {
        GrnRequestDto grn = new GrnRequestDto();

        assertThat(grn.getGrandTotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Balanced when cash + cheque + credit equals the grand total")
    void balancedPayments() {
        GrnRequestDto grn = new GrnRequestDto();
        grn.setItems(List.of(line("10", "100")));
        grn.setCash(new BigDecimal("200"));
        grn.setCheque(new BigDecimal("300"));
        grn.setCredit(new BigDecimal("500"));

        assertThat(grn.isBalanced()).isTrue();
    }

    @Test
    @DisplayName("Not balanced when payments do not cover the grand total")
    void unbalancedPayments() {
        GrnRequestDto grn = new GrnRequestDto();
        grn.setItems(List.of(line("10", "100")));
        grn.setCash(new BigDecimal("200"));
        grn.setCredit(new BigDecimal("500"));

        assertThat(grn.isBalanced()).isFalse();
    }

    @Test
    @DisplayName("Missing payment components are treated as zero")
    void nullPaymentComponentsTreatedAsZero() {
        GrnRequestDto grn = new GrnRequestDto();
        grn.setItems(List.of(line("10", "100")));
        grn.setCash(new BigDecimal("1000"));

        assertThat(grn.isBalanced()).isTrue();
    }
}
