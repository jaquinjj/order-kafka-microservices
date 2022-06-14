package com.mmert;

 import com.mmert.base.domain.Order;
 import com.mmert.service.OrderService;
 import org.apache.kafka.clients.admin.NewTopic;
 import org.apache.kafka.common.serialization.Serdes;
 import org.apache.kafka.streams.StreamsBuilder;
 import org.apache.kafka.streams.kstream.*;
 import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
 import org.apache.kafka.streams.state.Stores;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.context.annotation.Bean;
 import org.springframework.kafka.annotation.EnableKafkaStreams;
 import org.springframework.kafka.config.TopicBuilder;
 import org.springframework.kafka.support.serializer.JsonSerde;
 import org.springframework.scheduling.annotation.EnableAsync;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

 import java.time.Duration;
 import java.util.concurrent.Executor;

@SpringBootApplication
@EnableKafkaStreams
@EnableAsync
public class OrderApp {
    private static final Logger LOG = LoggerFactory.getLogger(OrderApp.class);


    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class,args);
    }

    @Bean
    public NewTopic orders()
    {
        return TopicBuilder.name("orders")
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public NewTopic payments()
    {
        return TopicBuilder.name("payment-orders")
                .partitions(3)
                .compact()
                .build();
    }


    @Bean
    public NewTopic stocks()
    {
        return TopicBuilder.name("stock-orders")
                .partitions(3)
                .compact()
                .build();
    }

    @Autowired
    OrderService orderService;

    @Bean
    public KStream<Integer, Order> stream(StreamsBuilder builder) {
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        KStream<Integer, Order> stream = builder
                .stream("payment-orders", Consumed.with(Serdes.Integer(), orderSerde));

        stream.join(
                        builder.stream("stock-orders"),
                        orderService::verify,
                        JoinWindows.of(Duration.ofSeconds(10)),
                        StreamJoined.with(Serdes.Integer(), orderSerde, orderSerde))
                .peek((k, o) -> LOG.info("Output: {}", o))
                .to("orders");

        return stream;
    }

    @Bean
    public KTable<Integer, Order> table(StreamsBuilder builder) {
        KeyValueBytesStoreSupplier store =
                Stores.persistentKeyValueStore("orders");
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        KStream<Integer, Order> stream = builder
                .stream("orders", Consumed.with(Serdes.Integer(), orderSerde));
        return stream.toTable(Materialized.<Integer, Order>as(store)
                .withKeySerde(Serdes.Integer())
                .withValueSerde(orderSerde));
    }


    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("kafkaSender-");
        executor.initialize();
        return executor;
    }

}