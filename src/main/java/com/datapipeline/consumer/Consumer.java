package com.datapipeline.consumer;

import com.datapipeline.broker.Broker;
import com.datapipeline.broker.Partition;
import com.datapipeline.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer {
    private final Broker broker;
    private final String topic;
    private final String groupId;
    private final Map<Integer, Long> offsets = new ConcurrentHashMap<>();

    public Consumer(Broker broker, String topic, String groupId) {
        this.broker = broker;
        this.topic = topic;
        this.groupId = groupId;
        List<Partition> partitions = broker.getPartitions(topic);
        if (partitions != null) {
            for (Partition partition : partitions) {
                offsets.putIfAbsent(partition.getId(), 0L);
            }
        }
    }

    public List<Message> poll() {
        List<Message> messages = new ArrayList<>();
        List<Partition> partitions = broker.getPartitions(topic);
        if (partitions != null) {
            for (Partition partition : partitions) {
                Message msg = partition.poll();
                if (msg != null) {
                    messages.add(msg);
                    offsets.compute(partition.getId(), (k, v) -> v + 1);
                }
            }
        }
        return messages;
    }

    public long getOffset(int partitionId) {
        return offsets.getOrDefault(partitionId, 0L);
    }
}
