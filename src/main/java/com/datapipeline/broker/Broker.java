package com.datapipeline.broker;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Broker {
    private final ConcurrentHashMap<String, List<Partition>> topics = new ConcurrentHashMap<>();

    public void createTopic(String topicName, int partitionCount) {
        topics.putIfAbsent(topicName, new CopyOnWriteArrayList<>(
                IntStream.range(0, partitionCount)
                        .mapToObj(Partition::new)
                        .collect(Collectors.toList())
        ));
    }

    public List<Partition> getPartitions(String topicName) {
        return topics.get(topicName);
    }
}
