package com.mmert.service;

import com.mmert.base.domain.Order;
import com.mmert.base.domain.OrderConfirm;
import com.mmert.domain.Product;
import com.mmert.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderManagerService {
    private final String SOURCE="stock";
    private static final Logger LOG = LoggerFactory.getLogger(OrderManagerService.class);
    private ProductRepository productRepository;
    private KafkaTemplate<Integer, Order> kafkaTemplate;

    public OrderManagerService(ProductRepository productRepository, KafkaTemplate<Integer, Order> kafkaTemplate)
    {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void reserve(Order order)
    {
        Product product = productRepository.findById(order.getProductId()).orElseThrow();
        if (order.getSalesCount() < product.getAvailableCount())
        {
            order.setStatus(OrderConfirm.ACCEPT);
            product.setReservedCount(product.getReservedCount()+order.getSalesCount());
            product.setAvailableCount(product.getAvailableCount()-order.getSalesCount());
        }
        else
        {
            order.setStatus(OrderConfirm.REJECT);
        }

        order.setSource(SOURCE);
        productRepository.save(product);
        kafkaTemplate.send("stock-orders",order.getId(),order);
        LOG.info("Sended Order from stock service:{}",order);
    }


    public void verify(Order order)
    {
        Product product = productRepository.findById(order.getProductId()).orElseThrow();

        if(order.getStatus().equals(OrderConfirm.CONFIRMED))
        {
            product.setReservedCount(product.getReservedCount()-order.getSalesCount());
            productRepository.save(product);
        } else if (order.getStatus().equals(OrderConfirm.CONFIRMED) && !order.getSource().equals(SOURCE)) {
            product.setReservedCount(product.getReservedCount()-order.getSalesCount());
            product.setAvailableCount(product.getAvailableCount()+order.getSalesCount());

            productRepository.save(product);
        }

    }
}
