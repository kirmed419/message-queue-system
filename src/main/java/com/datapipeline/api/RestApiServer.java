package com.datapipeline.api;

import com.datapipeline.broker.Broker;
import com.datapipeline.consumer.Consumer;
import com.datapipeline.model.Message;
import com.datapipeline.producer.Producer;
import com.datapipeline.processing.SparkProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;

public class RestApiServer {
    private static final Broker broker = new Broker();
    private static final Producer producer = new Producer(broker);
    private static final ConcurrentHashMap<String, Consumer> consumers = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SparkProcessor sparkProcessor = new SparkProcessor(broker);


    public static void main(String[] args) {
        staticFiles.location("/public");

        broker.createTopic("stocks", 3);

        sparkProcessor.start();

        post("/produce/:topic", (req, res) -> {
            String topic = req.params(":topic");
            String key = req.queryParams("key");
            String body = req.body();

            if (broker.getPartitions(topic) == null) {
                res.status(404);
                return "Topic not found";
            }

            producer.send(topic, key, body);
            return "Message received";
        });

        get("/consume/:topic", (req, res) -> {
            String topic = req.params(":topic");
            String groupId = req.queryParams("groupId");

            if (broker.getPartitions(topic) == null) {
                res.status(404);
                return "Topic not found";
            }

            Consumer consumer = consumers.computeIfAbsent(groupId, g -> new Consumer(broker, topic, g));
            List<Message> messages = consumer.poll();

            res.type("application/json");
            return objectMapper.writeValueAsString(messages);
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            producer.shutdown();
            sparkProcessor.stop();
        }));
    }
}
