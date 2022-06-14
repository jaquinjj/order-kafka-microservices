package com.mmert.service;

import com.mmert.base.domain.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderCreatorService {
    private static Random RAND = new Random();
    private AtomicInteger id = new AtomicInteger();
    private Executor executor;
    private KafkaTemplate<Long, Order> kafkaTemplate;

    public OrderCreatorService(Executor executor, KafkaTemplate<Long, Order> kafkaTemplate){
        this.executor = executor;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public void initData()
    {
        for (int i = 0; i < 10000; i++) {
            int randNum = RAND.nextInt(6)+1;
            Order o = new Order(
                    id.incrementAndGet(),
                    "Str"+i,
                    RAND.nextInt(6)+1,
                    RAND.nextInt(6)+1,
                    randNum,
                    100*randNum,
                    null,
                    ""
                    );
        }
    }
}
