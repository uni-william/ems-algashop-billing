package com.algaworks.algashop.billing.application.invoice.management;

import com.algaworks.algashop.billing.domain.model.DomainException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.*;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceManagementApplicationService {

    private final PaymentGatewayService paymentGatewayService;
    private final InvoicingService invoicingService;
    private final InvoiceRepository invoiceRepository;
    private final CreditCardRepository creditCardRepository;

    @Transactional
    public UUID generate(GenerateInvoiceInput input) {
        PaymentSettingsInput paymentSettings = input.getPaymentSettings();
        verifyCreditCardId(paymentSettings.getCreditCardId());

        Payer payer = convertToPayer(input.getPayer());
        Set<LineItem> items = convertToLineItems(input.getItems());

        Invoice invoice = invoicingService.issue(input.getOrderId(), input.getCustomerId(), payer, items);
        invoice.changePaymentSettings(paymentSettings.getMethod(), paymentSettings.getCreditCardId());

        invoiceRepository.saveAndFlush(invoice);
        return invoice.getId();

    }

    private Set<LineItem> convertToLineItems(Set<LineItemInput> itemsInput) {
        Set<LineItem> lineItems = new LinkedHashSet<>();
        int itemNumber = 1;
        for (LineItemInput itemInput : itemsInput) {
            lineItems.add(LineItem.builder()
                            .number(itemNumber)
                            .name(itemInput.getName())
                            .amount(itemInput.getAmount())
                    .build());
            itemNumber++;
        }
        return lineItems;
    }

    private Payer convertToPayer(PayerData payerData) {
        AddressData addressData = payerData.getAddress();
        return Payer.builder()
                .fullName(payerData.getFullName())
                .email(payerData.getEmail())
                .document(payerData.getDocument())
                .phone(payerData.getPhone())
                .address(Address.builder()
                        .city(addressData.getCity())
                        .state(addressData.getState())
                        .neighborhood(addressData.getNeighborhood())
                        .complement(addressData.getComplement())
                        .zipCode(addressData.getZipCode())
                        .street(addressData.getStreet())
                        .number(addressData.getNumber())
                        .build())
                .build();
    }

    private void verifyCreditCardId(UUID creditCardId) {
        if (creditCardId != null && !creditCardRepository.existsById(creditCardId)) {
            throw  new CreditCardNotFoundException();
        }

    }
}
