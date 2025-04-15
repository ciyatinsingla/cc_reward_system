package com.lms.ccrp.service;

import com.lms.ccrp.dto.CustomerDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.util.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenRepository jwtTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    public Long createCustomer(CustomerDTO customerDTO, User user) {
        Optional<Customer> existingCustomer = customerRepository.findByUserId(user.getId());
        if (existingCustomer.isPresent())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Customer already exists");
        Customer customer = CustomerMapper.dtoToCustomer(customerDTO, user);
        return customerRepository.save(customer).getCustomerId();
    }
}
