package com.wipro.hms.billing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wipro.hms.billing.entity.Bill;
import com.wipro.hms.billing.repository.BillRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BillingService {

    @Autowired
    private BillRepository billRepository;

    public Bill createBill(Bill bill) {
        bill.setCreatedAt(LocalDateTime.now());
        return billRepository.save(bill);
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }

    public Page<Bill> getBillsByPatientId(Long patientId, Pageable pageable) {
        return billRepository.findByPatientId(patientId, pageable);
    }

    public Bill updatePaymentStatus(Long id, Bill.PaymentStatus status, String paymentMethod) {
        Bill bill = getBillById(id);
        bill.setPaymentStatus(status);
        bill.setPaymentMethod(paymentMethod);
        bill.setPaymentDate(LocalDateTime.now());
        return billRepository.save(bill);
    }

    public BigDecimal getOutstandingBalance(Long patientId) {
        List<Bill> pendingBills = billRepository.findByPatientIdAndPaymentStatus(patientId, Bill.PaymentStatus.PENDING);
        return pendingBills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
