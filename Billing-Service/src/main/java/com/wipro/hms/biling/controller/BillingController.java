package com.wipro.hms.biling.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.hms.biling.entity.Bill;
import com.wipro.hms.biling.service.BillingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "Billing Management", description = "APIs for managing bills and payments")
public class BillingController {
    
    @Autowired
    private BillingService billingService;
    
    @PostMapping
    @Operation(summary = "Create a new bill")
    public ResponseEntity<Bill> createBill(@Valid @RequestBody Bill bill) {
        Bill createdBill = billingService.createBill(bill);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<Bill> getBill(@PathVariable Long id) {
        Bill bill = billingService.getBillById(id);
        return ResponseEntity.ok(bill);
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get bills by patient ID")
    public ResponseEntity<Page<Bill>> getBillsByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Bill> bills = billingService.getBillsByPatientId(patientId, pageable);
        return ResponseEntity.ok(bills);
    }
    
    @PatchMapping("/{id}/payment")
    @Operation(summary = "Update payment status")
    public ResponseEntity<Bill> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam Bill.PaymentStatus status,
            @RequestParam(required = false) String paymentMethod) {
        Bill updatedBill = billingService.updatePaymentStatus(id, status, paymentMethod);
        return ResponseEntity.ok(updatedBill);
    }
    
    @GetMapping("/patient/{patientId}/outstanding")
    @Operation(summary = "Get outstanding balance for patient")
    public ResponseEntity<BigDecimal> getOutstandingBalance(@PathVariable Long patientId) {
        BigDecimal balance = billingService.getOutstandingBalance(patientId);
        return ResponseEntity.ok(balance);
    }
}


