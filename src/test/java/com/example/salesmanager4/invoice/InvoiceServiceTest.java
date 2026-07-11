package com.example.salesmanager4.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;
import com.example.salesmanager4.finance.payments.receivable.CustomerPaymentRequest;
import com.example.salesmanager4.finance.payments.receivable.CustomerPaymentService;
import com.example.salesmanager4.inventory.stock.StockTransactionService;
import com.example.salesmanager4.users.CurrentUserService;

/**
 * Unit tests for InvoiceService in isolation. All collaborators
 * (repositories, stock posting, customer payments, security) are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Invoice service")
class InvoiceServiceTest {

    @Mock InvoiceRepository invoiceRepository;
    @Mock InvoiceJdbcRepository invoiceJdbcRepository;
    @Mock CurrentUserService currentUserService;
    @Mock StockTransactionService stockTransactionService;
    @Mock CustomerPaymentService customerPaymentService;

    @InjectMocks
    InvoiceService invoiceService;

    @Captor
    ArgumentCaptor<CustomerPaymentRequest> paymentCaptor;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        InvoiceItem item = new InvoiceItem();
        item.setItemId(21L);
        item.setQuantity(new BigDecimal("12"));
        item.setFreeQty(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("100"));

        invoice = new Invoice();
        invoice.setId(5L);
        invoice.setStatus("DRAFT");
        invoice.setCustomerId(3L);
        invoice.setEmployeeId(42L); // the salesman
        invoice.setInvoiceDate(LocalDate.of(2026, 7, 10));
        invoice.setCash(new BigDecimal("600"));
        invoice.setCheque(new BigDecimal("400"));
        invoice.setCredit(new BigDecimal("400"));
        invoice.setItems(List.of(item));
    }

    @Test
    @DisplayName("Creating an invoice stores it as DRAFT with the current employee")
    void createInvoiceStoresDraft() {
        when(currentUserService.getEmployeeId()).thenReturn(42L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceRequestDto request = new InvoiceRequestDto();
        request.setCustomerId(3L);
        InvoiceRequestLineDto line = new InvoiceRequestLineDto();
        line.setItemId(21L);
        request.setItems(List.of(line));

        invoiceService.createInvoice(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getStatus()).isEqualTo("DRAFT");
        assertThat(invoiceCaptor.getValue().getEmployeeId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("Approving posts stock OUT for sold quantity plus free issue")
    void approvePostsStockOut() {
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        invoiceService.approveInvoice(5L);

        verify(invoiceRepository).setStatusById(5L, "APPROVED");
        verify(stockTransactionService).stockOut(eq(21L), eq(new BigDecimal("14")), eq("INVOICE"), eq(5L));
    }

    @Test
    @DisplayName("Missing free quantity is treated as zero when posting stock")
    void approveWithNullFreeQty() {
        invoice.getItems().get(0).setFreeQty(null);
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        invoiceService.approveInvoice(5L);

        verify(stockTransactionService).stockOut(eq(21L), eq(new BigDecimal("12")), eq("INVOICE"), eq(5L));
    }

    @Test
    @DisplayName("Approving creates allocated IN payments stamped with the salesman as collector")
    void approveCreatesCustomerPayments() {
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        invoiceService.approveInvoice(5L);

        verify(customerPaymentService, times(2)).createPaymentAndAllocate(eq(5L), paymentCaptor.capture());

        List<CustomerPaymentRequest> payments = paymentCaptor.getAllValues();

        assertThat(payments.get(0).getPaymentMethod()).isEqualTo(PaymentType.CASH);
        assertThat(payments.get(0).getTotalPaymentAmount()).isEqualByComparingTo("600");
        assertThat(payments.get(1).getPaymentMethod()).isEqualTo(PaymentType.CHEQUE);
        assertThat(payments.get(1).getTotalPaymentAmount()).isEqualByComparingTo("400");
        assertThat(payments).allSatisfy(p -> {
            assertThat(p.getCustomerId()).isEqualTo(3L);
            assertThat(p.getDirection()).isEqualTo(PaymentDirection.IN);
            assertThat(p.getCollectedBy()).isEqualTo(42L); // cash balancing attribution
        });
    }

    @Test
    @DisplayName("Approving skips payments when cash and cheque portions are zero")
    void approveWithoutCashOrCheque() {
        invoice.setCash(BigDecimal.ZERO);
        invoice.setCheque(null);
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        invoiceService.approveInvoice(5L);

        verify(customerPaymentService, never()).createPaymentAndAllocate(anyLong(), any());
    }

    @Test
    @DisplayName("Only DRAFT invoices can be approved")
    void approveNonDraftFails() {
        invoice.setStatus("APPROVED");
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.approveInvoice(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only DRAFT");

        verify(stockTransactionService, never()).stockOut(any(), any(), any(), any());
    }
}
