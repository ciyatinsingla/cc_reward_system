package com.lms.ccrp.repository;

import com.lms.ccrp.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    Customer findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    @Query(value = "SELECT * FROM customer_details  WHERE isActive = true", nativeQuery = true)
    List<Customer> findAllActiveCustomerIds();
}
