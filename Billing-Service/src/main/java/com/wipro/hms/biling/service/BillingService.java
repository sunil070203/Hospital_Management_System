package com.wipro.hms.biling.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.wipro.hms.biling.entity.Bill;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BillingService {
    
    @Autowired
    private BillRepository billRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public Bill createBill(Bill bill) {
        bill.setCreatedAt(LocalDateTime.now());
        calculateTotalAmount(bill);
        
        Bill savedBill = billRepository.save(bill);
        kafkaTemplate.send("billing-events", "BILL_CREATED", savedBill);
        
        return savedBill;
    }
    
    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }
    
    public Page<Bill> getBillsByPatientId(Long patientId, Pageable pageable) {
        return billRepository.findByPatientId(patientId, pageable);
    }
    
    public Bill updatePaymentStatus(Long id, Bill.PaymentStatus status, String paymentMethod) {
        Bill bill = getBillById(id);
        bill.setPaymentStatus(status);
        bill.setPaymentMethod(paymentMethod);
        
        if (status == Bill.PaymentStatus.PAID) {
            bill.setPaymentDate(LocalDateTime.now());
        }
        
        bill.setUpdatedAt(LocalDateTime.now());
        Bill updatedBill = billRepository.save(bill);
        
        kafkaTemplate.send("billing-events", "BILL_UPDATED", updatedBill);
        return updatedBill;
    }
    
    public BigDecimal getOutstandingBalance(Long patientId) {
        List<Bill> pendingBills = billRepository.findByPatientIdAndPaymentStatus(patientId, Bill.PaymentStatus.PENDING);
        return pendingBills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void calculateTotalAmount(Bill bill) {
        BigDecimal total = bill.getConsultationFee();
        
        if (bill.getMedicationCharges() != null) {
            total = total.add(bill.getMedicationCharges());
        }
        
        if (bill.getLabCharges() != null) {
            total = total.add(bill.getLabCharges());
        }
        
        if (bill.getOtherCharges() != null) {
            total = total.add(bill.getOtherCharges());
        }
        
        bill.setTotalAmount(total);
    }
}