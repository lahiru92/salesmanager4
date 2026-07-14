package com.example.salesmanager4.finance.payments.payable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.salesmanager4.finance.payments.AllocationLine;
import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;

/**
 * Unit tests for SupplierPaymentService with mocked repositories,
 * covering payment mapping and GRN allocation rules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Supplier payment service")
class SupplierPaymentServiceTest {

    @Mock SupplierPaymentRepository paymentRepository;
    @Mock SupplierPaymentAllocationRepository allocationRepository;

    @InjectMocks
    SupplierPaymentService service;

    @Captor
    ArgumentCaptor<List<SupplierPaymentAllocation>> allocationsCaptor;

    private SupplierPaymentRequest request;

    @BeforeEach
    void setUp() {
        request = new SupplierPaymentRequest();
        request.setSupplierId(7L);
        request.setPaymentMethod(PaymentType.CASH);
        request.setDirection(PaymentDirection.OUT);
        request.setTotalPaymentAmount(new BigDecimal("1000"));
        request.setPaymentDate(LocalDate.of(2026, 7, 10));

        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(inv -> {
            SupplierPayment payment = inv.getArgument(0);
            payment.setId(55L);
            return payment;
        });
    }

    private AllocationLine allocation(Long grnId, String amount) {
        AllocationLine line = new AllocationLine();
        line.setDocumentId(grnId);
        line.setAmount(amount != null ? new BigDecimal(amount) : null);
        return line;
    }

    @Test
    @DisplayName("Payment fields are mapped and the generated id is returned")
    void createPaymentMapsFields() {
        Long id = service.createPayment(request);

        assertThat(id).isEqualTo(55L);

        ArgumentCaptor<SupplierPayment> paymentCaptor = ArgumentCaptor.forClass(SupplierPayment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        SupplierPayment saved = paymentCaptor.getValue();
        assertThat(saved.getSupplierId()).isEqualTo(7L);
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentType.CASH);
        assertThat(saved.getDirection()).isEqualTo(PaymentDirection.OUT);
        assertThat(saved.getTotalPaymentAmount()).isEqualByComparingTo("1000");
        assertThat(saved.getPaymentDate()).isEqualTo(LocalDate.of(2026, 7, 10));
    }

    @Test
    @DisplayName("Valid allocation lines are saved against the new payment")
    void createPaymentWithAllocations() {
        request.setAllocations(List.of(allocation(1L, "600"), allocation(2L, "300")));

        service.createPaymentWithAllocations(request);

        verify(allocationRepository).saveAll(allocationsCaptor.capture());
        List<SupplierPaymentAllocation> saved = allocationsCaptor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).paymentId()).isEqualTo(55L);
        assertThat(saved.get(0).grnId()).isEqualTo(1L);
        assertThat(saved.get(0).allocatedAmount()).isEqualByComparingTo("600");
        assertThat(saved.get(1).grnId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Empty, zero and negative allocation lines are filtered out")
    void invalidAllocationsAreFiltered() {
        request.setAllocations(List.of(
                allocation(1L, "600"),     // valid
                allocation(2L, null),      // no amount
                allocation(3L, "0"),       // zero
                allocation(4L, "-50"),     // negative
                allocation(null, "100"))); // no document

        service.createPaymentWithAllocations(request);

        verify(allocationRepository).saveAll(allocationsCaptor.capture());
        assertThat(allocationsCaptor.getValue()).hasSize(1);
        assertThat(allocationsCaptor.getValue().get(0).grnId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("An unallocated payment saves no allocation rows")
    void unallocatedPaymentSavesNoAllocations() {
        request.setAllocations(List.of());

        service.createPaymentWithAllocations(request);

        verify(paymentRepository).save(any(SupplierPayment.class));
        verify(allocationRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createPaymentAndAllocate allocates the full amount to one GRN")
    void createPaymentAndAllocate() {
        service.createPaymentAndAllocate(9L, request);

        ArgumentCaptor<SupplierPaymentAllocation> captor = ArgumentCaptor.forClass(SupplierPaymentAllocation.class);
        verify(allocationRepository).save(captor.capture());
        assertThat(captor.getValue().paymentId()).isEqualTo(55L);
        assertThat(captor.getValue().grnId()).isEqualTo(9L);
        assertThat(captor.getValue().allocatedAmount()).isEqualByComparingTo("1000");
    }
}
