package com.example.salesmanager4.grn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.example.salesmanager4.finance.payments.payable.SupplierPaymentRequest;
import com.example.salesmanager4.finance.payments.payable.SupplierPaymentService;
import com.example.salesmanager4.inventory.stock.StockTransactionService;
import com.example.salesmanager4.users.CurrentUserService;

/**
 * Unit tests for GrnService in isolation. All collaborators
 * (repositories, stock posting, supplier payments, security) are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GRN service")
class GrnServiceTest {

    @Mock GrnRepository grnRepository;
    @Mock GrnJdbcRepository grnJdbcRepository;
    @Mock CurrentUserService currentUserService;
    @Mock StockTransactionService stockTransactionService;
    @Mock SupplierPaymentService supplierPaymentService;

    @InjectMocks
    GrnService grnService;

    @Captor
    ArgumentCaptor<SupplierPaymentRequest> paymentCaptor;

    private Grn grn;

    @BeforeEach
    void setUp() {
        GrnItem item = new GrnItem();
        item.setItemId(11L);
        item.setReceivedQty(new BigDecimal("10"));
        item.setRejectedQty(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("50"));

        grn = new Grn();
        grn.setId(1L);
        grn.setStatus("DRAFT");
        grn.setSupplierId(7L);
        grn.setReceivedDate(LocalDate.of(2026, 7, 10));
        grn.setCash(new BigDecimal("500"));
        grn.setCheque(new BigDecimal("300"));
        grn.setCredit(new BigDecimal("200"));
        grn.setItems(List.of(item));
    }

    @Test
    @DisplayName("Creating a GRN stores it as DRAFT with the current employee")
    void createGrnStoresDraft() {
        when(currentUserService.getEmployeeId()).thenReturn(99L);
        when(grnRepository.save(any(Grn.class))).thenAnswer(inv -> inv.getArgument(0));

        GrnRequestDto request = new GrnRequestDto();
        request.setSupplierId(7L);
        request.setReceivedDate(LocalDate.of(2026, 7, 10));
        GrnRequestLineDto line = new GrnRequestLineDto();
        line.setItemId(11L);
        request.setItems(List.of(line));

        grnService.createGrn(request);

        ArgumentCaptor<Grn> grnCaptor = ArgumentCaptor.forClass(Grn.class);
        verify(grnRepository).save(grnCaptor.capture());
        assertThat(grnCaptor.getValue().getStatus()).isEqualTo("DRAFT");
        assertThat(grnCaptor.getValue().getEmployeeId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("Creating a GRN fails when the user has no employee record")
    void createGrnWithoutEmployeeFails() {
        when(currentUserService.getEmployeeId()).thenReturn(null);

        assertThatThrownBy(() -> grnService.createGrn(new GrnRequestDto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee ID not found");

        verify(grnRepository, never()).save(any());
    }

    @Test
    @DisplayName("Approving posts stock IN with the accepted quantity (received - rejected)")
    void approvePostsStockIn() {
        when(grnRepository.findById(1L)).thenReturn(Optional.of(grn));

        grnService.approveGrn(1L);

        verify(grnRepository).setStatusById(1L, "APPROVED");
        verify(stockTransactionService).stockIn(
                eq(11L), eq(new BigDecimal("8")), eq(new BigDecimal("50")), eq("GRN"), eq(1L));
    }

    @Test
    @DisplayName("Approving creates allocated OUT payments for the cash and cheque portions")
    void approveCreatesSupplierPayments() {
        when(grnRepository.findById(1L)).thenReturn(Optional.of(grn));

        grnService.approveGrn(1L);

        verify(supplierPaymentService, org.mockito.Mockito.times(2))
                .createPaymentAndAllocate(eq(1L), paymentCaptor.capture());

        List<SupplierPaymentRequest> payments = paymentCaptor.getAllValues();

        assertThat(payments.get(0).getPaymentMethod()).isEqualTo(PaymentType.CASH);
        assertThat(payments.get(0).getTotalPaymentAmount()).isEqualByComparingTo("500");
        assertThat(payments.get(1).getPaymentMethod()).isEqualTo(PaymentType.CHEQUE);
        assertThat(payments.get(1).getTotalPaymentAmount()).isEqualByComparingTo("300");
        assertThat(payments).allSatisfy(p -> {
            assertThat(p.getSupplierId()).isEqualTo(7L);
            assertThat(p.getDirection()).isEqualTo(PaymentDirection.OUT);
        });
    }

    @Test
    @DisplayName("Approving skips payments when cash and cheque portions are zero")
    void approveWithoutCashOrCheque() {
        grn.setCash(BigDecimal.ZERO);
        grn.setCheque(null);
        when(grnRepository.findById(1L)).thenReturn(Optional.of(grn));

        grnService.approveGrn(1L);

        verify(supplierPaymentService, never()).createPaymentAndAllocate(anyLong(), any());
    }

    @Test
    @DisplayName("Only DRAFT GRNs can be approved")
    void approveNonDraftFails() {
        grn.setStatus("APPROVED");
        when(grnRepository.findById(1L)).thenReturn(Optional.of(grn));

        assertThatThrownBy(() -> grnService.approveGrn(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only DRAFT");

        verify(grnRepository, never()).setStatusById(anyLong(), any());
        verify(stockTransactionService, never()).stockIn(any(), any(), any(), any(), any());
    }
}
