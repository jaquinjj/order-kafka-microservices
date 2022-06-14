package com.mmert.controller;

import com.mmert.base.domain.Order;
import com.mmert.service.OrderCreatorService;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
    private AtomicInteger id = new AtomicInteger();
    private KafkaTemplate<Integer, Order> kafkaTemplate;
    private StreamsBuilderFactoryBean kafkaStreamsFactory;
    private OrderCreatorService orderCreatorService;

    public OrderController(
            KafkaTemplate<Integer, Order> kafkaTemplate,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            OrderCreatorService orderCreatorService
    )
    {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.orderCreatorService = orderCreatorService;
    }

    @PostMapping
    public Order create(@RequestBody Order order)
    {
        order.setId(id.incrementAndGet());
        kafkaTemplate.send("orders",order.getId(),order);
        LOG.info("Create new Order:{}",order);
        return order;
    }

    @PostMapping("/generator")
    public boolean generator()
    {
        orderCreatorService.initData();
        return true;
    }

    @GetMapping
    public List<Order> allData()
    {
        List<Order> orderList = new ArrayList<>();
        ReadOnlyKeyValueStore<Long,Order> kafkaStore = kafkaStreamsFactory
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        "orders",
                        QueryableStoreTypes.keyValueStore()
                ));
        KeyValueIterator<Long,Order> iterator = kafkaStore.all();
        iterator.forEachRemaining(x->orderList.add(x.value));
        return orderList;
    }


}
