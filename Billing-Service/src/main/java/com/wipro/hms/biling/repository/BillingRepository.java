package com.wipro.hms.biling.repository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wipro.hms.biling.entity.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    /**
     * Find all bills for a specific patient with pagination
     */
    Page<Bill> findByPatientId(Long patientId, Pageable pageable);

    /**
     * Find bills by patient ID and payment status
     */
    List<Bill> findByPatientIdAndPaymentStatus(Long patientId, Bill.PaymentStatus paymentStatus);

    /**
     * Find bills by appointment ID
     */
    List<Bill> findByAppointmentId(Long appointmentId);

    /**
     * Find bills by payment status with pagination
     */
    Page<Bill> findByPaymentStatus(Bill.PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find bills within a date range
     */
    @Query("SELECT b FROM Bill b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Bill> findBillsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find bills by patient ID within a date range
     */
    @Query("SELECT b FROM Bill b WHERE b.patientId = :patientId AND b.createdAt BETWEEN :startDate AND :endDate")
    List<Bill> findByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total revenue by payment status
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.paymentStatus = :paymentStatus")
    BigDecimal getTotalRevenueByStatus(@Param("paymentStatus") Bill.PaymentStatus paymentStatus);

    /**
     * Calculate total revenue within a date range
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.paymentStatus = 'PAID' AND b.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Count bills by payment status
     */
    Long countByPaymentStatus(Bill.PaymentStatus paymentStatus);

    /**
     * Find the most recent bills for a patient
     */
    List<Bill> findTop5ByPatientIdOrderByCreatedAtDesc(Long patientId);

    /**
     * Find bills with total amount greater than specified value
     */
    List<Bill> findByTotalAmountGreaterThan(BigDecimal amount);

    /**
     * Find bills with total amount between specified values
     */
    List<Bill> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find bills by payment method
     */
    List<Bill> findByPaymentMethod(String paymentMethod);

    /**
     * Check if a bill exists for an appointment
     */
    Boolean existsByAppointmentId(Long appointmentId);
}
