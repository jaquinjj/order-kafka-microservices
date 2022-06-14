package com.mmert.service;

import com.mmert.base.domain.Order;
import com.mmert.base.domain.OrderConfirm;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public Order verify(Order fromPayment,Order fromStock)
    {
        Order order = new Order(fromPayment.getId(),
                fromPayment.getName(),
                fromPayment.getProductId(),
                fromPayment.getCustomerId(),
                fromPayment.getSalesCount(),
                fromPayment.getPrice(),
                fromPayment.getStatus(),
                fromPayment.getSource());
        String paymentConfirm = fromPayment.getStatus();
        String stockConfirm = fromStock.getStatus();

        if(paymentConfirm.equals(OrderConfirm.ACCEPT) && stockConfirm.equals(OrderConfirm.ACCEPT))
        {
            order.setStatus(OrderConfirm.CONFIRMED);
        } else if (paymentConfirm.equals(OrderConfirm.REJECT)&&stockConfirm.equals(OrderConfirm.REJECT)) {
            order.setStatus(OrderConfirm.REJECTED);
        } else if (paymentConfirm.equals(OrderConfirm.REJECT) || stockConfirm.equals(OrderConfirm.REJECT)) {
            String source = paymentConfirm.equals(OrderConfirm.REJECT) ? "PAYMENT":"STOCK";
            order.setStatus(OrderConfirm.ROLLBACK);
            order.setSource(source);
        }

    return order;
    }


}
