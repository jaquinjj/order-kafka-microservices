package com.mmert.service;

import com.mmert.base.domain.Order;
import com.mmert.base.domain.OrderConfirm;
import com.mmert.domain.Customer;
import com.mmert.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderManagerService {
    private static final String SOURCE = "payment";
    private static final Logger LOG = LoggerFactory.getLogger(OrderManagerService.class);
    private CustomerRepository customerRepository;
    private KafkaTemplate<Integer, Order> kafkaTemplate;

    public OrderManagerService(CustomerRepository customerRepository,KafkaTemplate<Integer, Order> kafkaTemplate)
    {
        this.customerRepository = customerRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void reserve(Order order)
    {
        Customer customer = customerRepository.findById(order.getCustomerId()).orElseThrow();
        LOG.info("Founded Customer:{}",customer);
        if (customer.getAvailableCount() > order.getPrice())
        {
            order.setStatus(OrderConfirm.ACCEPT);
            customer.setReservedCount(customer.getReservedCount()+order.getPrice());
            customer.setAvailableCount(customer.getAvailableCount()-order.getPrice());

        }
        else {
            order.setStatus(OrderConfirm.REJECT);
        }

        order.setSource(SOURCE);
        customerRepository.save(customer);
        kafkaTemplate.send("payment-orders",order.getId(),order);
        LOG.info("Sent Payment:{}",order);
    }

    public void verify(Order order)
    {
        Customer customer = customerRepository.findById(order.getCustomerId()).orElseThrow();
        LOG.info("Found:{}",customer);
        if (order.getStatus().equals(OrderConfirm.CONFIRMED)) {
            customer.setReservedCount(customer.getReservedCount()-order.getPrice());
            customerRepository.save(customer);
        } else if (order.getStatus().equals(OrderConfirm.ROLLBACK) && !order.getSource().equals(SOURCE) ) {
            customer.setReservedCount(customer.getReservedCount()-order.getPrice());
            customer.setAvailableCount(customer.getAvailableCount()+order.getPrice());
            customerRepository.save(customer);
        }
    }
}
