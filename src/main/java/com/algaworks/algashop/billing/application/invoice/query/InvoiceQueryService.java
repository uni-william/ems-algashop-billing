package com.algaworks.algashop.billing.application.invoice.query;

public interface InvoiceQueryService {
    InvoiceOutout findByOrderId(String orderId);
}
