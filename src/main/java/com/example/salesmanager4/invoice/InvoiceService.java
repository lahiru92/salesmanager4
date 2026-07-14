package com.example.salesmanager4.invoice;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;
import com.example.salesmanager4.finance.payments.receivable.CustomerPaymentRequest;
import com.example.salesmanager4.finance.payments.receivable.CustomerPaymentService;
import com.example.salesmanager4.inventory.stock.StockTransactionService;
import com.example.salesmanager4.users.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceJdbcRepository invoiceJdbcRepository;
    private final CurrentUserService currentUserService;
    private final StockTransactionService stockTransactionService;

    private final CustomerPaymentService customerPaymentService;

    public void createInvoice(InvoiceRequestDto invoiceRequest) {

        Long employeeId = currentUserService.getEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        };

        Invoice invoice = mapToInvoice(invoiceRequest);
        invoice.setStatus("DRAFT");
        invoice.setEmployeeId(employeeId);

        log.info("Creating Invoice: " + invoice);

        invoiceRepository.save(invoice);
    }

    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public InvoiceRequestDto findRequestDtoById(Long id) {
        InvoiceRequestDto invoiceRequestDto = invoiceRepository.findRequestDtoById(id);
        List<InvoiceRequestLineDto> items = invoiceRepository.findInvoiceRequestLineDtoById(id);
        invoiceRequestDto.setItems(items);
        return invoiceRequestDto;
    }

    @Transactional
    public void approveInvoice(Long id) {

        Invoice invoice = findById(id);

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new RuntimeException("Only DRAFT invoices can be approved");
        }

        // Save Invoice
        invoiceRepository.setStatusById(id, "APPROVED");

        // Update stock for each item (sold qty + free issue both leave the store)
        invoice.getItems().forEach(item -> {
            BigDecimal freeQty = item.getFreeQty() != null ? item.getFreeQty() : BigDecimal.ZERO;
            stockTransactionService.stockOut(
                item.getItemId(),
                item.getQuantity().add(freeQty),
                "INVOICE",
                id
            );
        });

        if (invoice.getCash() != null && invoice.getCash().compareTo(BigDecimal.ZERO) > 0)  {

            CustomerPaymentRequest cashPayment = new CustomerPaymentRequest(
                invoice.getCustomerId(),
                invoice.getId(),
                PaymentType.CASH,
                PaymentDirection.IN,
                invoice.getCash(),
                null,
                null,
                null,
                null,
                invoice.getInvoiceDate()
            );
            cashPayment.setCollectedBy(invoice.getEmployeeId());

            customerPaymentService.createPaymentAndAllocate(invoice.getId(), cashPayment);
        }

        if (invoice.getCheque() != null && invoice.getCheque().compareTo(BigDecimal.ZERO) > 0)  {

            CustomerPaymentRequest chequePayment = new CustomerPaymentRequest(
                invoice.getCustomerId(),
                invoice.getId(),
                PaymentType.CHEQUE,
                PaymentDirection.IN,
                invoice.getCheque(),
                null,
                null,
                null,
                null,
                invoice.getInvoiceDate()
            );
            chequePayment.setCollectedBy(invoice.getEmployeeId());

            customerPaymentService.createPaymentAndAllocate(invoice.getId(), chequePayment);
        }


    }

    public Page<InvoiceListResponseDto> listInvoices(InvoiceListRequestDto requestDto, Pageable pageable) {
        return invoiceJdbcRepository.findAllByPage(requestDto, pageable);
    }

    private Invoice mapToInvoice(InvoiceRequestDto invoiceRequest) {
        Invoice invoice = new Invoice();

        if (invoiceRequest.getId() != null) {
            invoice.setId(invoiceRequest.getId());
        }

        invoice.setStatus(invoiceRequest.getStatus());
        invoice.setInvoiceDate(invoiceRequest.getInvoiceDate());
        invoice.setCustomerId(invoiceRequest.getCustomerId());
        invoice.setEmployeeId(invoiceRequest.getEmployeeId());
        invoice.setCash(invoiceRequest.getCash() != null ? invoiceRequest.getCash() : BigDecimal.ZERO);
        invoice.setCredit(invoiceRequest.getCredit() != null ? invoiceRequest.getCredit() : BigDecimal.ZERO);
        invoice.setCheque(invoiceRequest.getCheque() != null ? invoiceRequest.getCheque() : BigDecimal.ZERO);
        invoice.setCreditDue(invoiceRequest.getCreditDue());
        invoice.setTotal(invoiceRequest.getGrandTotal() != null ? invoiceRequest.getGrandTotal() : BigDecimal.ZERO);

        invoice.setItems(invoiceRequest.getItems().stream()
                .map(itemDto -> {
                    InvoiceItem item = new InvoiceItem();
                    item.setItemId(itemDto.getItemId());
                    item.setItemName(itemDto.getItemName());
                    item.setQuantity(itemDto.getQuantity());
                    item.setFreeQty(itemDto.getFreeQty());
                    item.setUnitPrice(itemDto.getUnitPrice());
                    item.setDiscount(itemDto.getDiscount());
                    return item;
                })
                .toList());

        return invoice;
    }
}
