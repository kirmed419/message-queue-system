package com.datapipeline.producer;

import com.datapipeline.broker.Broker;
import com.datapipeline.model.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Producer {
    private final Broker broker;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public Producer(Broker broker) {
        this.broker = broker;
    }

    public void send(String topic, String key, String value) {
        executor.submit(() -> {
            int partition = key == null ? 0 : Math.abs(key.hashCode() % broker.getPartitions(topic).size());
            broker.getPartitions(topic).get(partition).addMessage(new Message(value));
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
