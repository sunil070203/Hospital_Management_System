package com.wipro.hms.doctor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wipro.hms.doctor.entity.Doctor;

import io.swagger.v3.oas.models.examples.Example;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    // now save(Doctor) is available
	
	Page<T> findAll(Example<S> example, Pageable pageable);
	
	

}	