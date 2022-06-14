package com.mmert;

import com.mmert.base.domain.Order;
import com.mmert.domain.Customer;
import com.mmert.repository.CustomerRepository;
import com.mmert.service.OrderManagerService;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.PostConstruct;
import java.util.Random;

@SpringBootApplication
@EnableKafka
public class PaymentApp {
    public static final Logger LOG = LoggerFactory.getLogger(PaymentApp.class);
    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class,args);
    }

    @Autowired
    OrderManagerService orderManagerService;

    @KafkaListener(id="orders", topics = "orders",groupId = "payment")
    public void onEvent(Order order)
    {
        System.out.println("testtt"+order.getId());
        LOG.info("Received:{}",order);
        if(order.getStatus().equals("NEW"))
            orderManagerService.reserve(order);
        else
            orderManagerService.verify(order);
    }

    @Autowired
    private CustomerRepository customerRepository;

    @PostConstruct
    public void initData()
    {
        Random r = new Random();
        Faker fdg = new Faker();

        for (int i = 0; i < 100; i++) {
            int count = r.nextInt(1000);
            Customer customer = new Customer(null,fdg.name().name(),fdg.name().lastName(),count,0);
            customerRepository.save(customer);
        }

    }

}