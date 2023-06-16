package com.rohit.batchprocessingdemo.repository;

import com.rohit.batchprocessingdemo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository  extends JpaRepository<Customer,Integer> {
}