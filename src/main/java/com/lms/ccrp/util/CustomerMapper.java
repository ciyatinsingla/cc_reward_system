package com.lms.ccrp.util;

import com.lms.ccrp.dto.CustomerDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.User;

public class CustomerMapper {

    public static Customer dtoToCustomer(CustomerDTO dto, User user) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setCreditCardNumber(dto.getCreditCardNumber());
        customer.setCreditCardType(dto.getCreditCardType());
        customer.setCardIssuanceDate(dto.getCardIssuanceDate());
        customer.setTotalPoints(0);
        customer.setUser(user);
        return customer;
    }
}
