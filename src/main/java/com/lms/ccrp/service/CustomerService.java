package com.lms.ccrp.service;

import com.lms.ccrp.dto.CustomerDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.util.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        Customer customer = CustomerMapper.dtoToCustomer(customerDTO, user);
        return customerRepository.save(customer).getCustomerId();
    }
}
