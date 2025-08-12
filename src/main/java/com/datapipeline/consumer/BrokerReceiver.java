package com.datapipeline.consumer;

import com.datapipeline.broker.Broker;
import com.datapipeline.model.Message;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import java.util.List;

public class BrokerReceiver extends Receiver<String> {
    private final Broker broker;
    private final String topic;
    private final String groupId;
    private transient Thread receivingThread;

    public BrokerReceiver(Broker broker, String topic, String groupId) {
        super(StorageLevel.MEMORY_AND_DISK_2());
        this.broker = broker;
        this.topic = topic;
        this.groupId = groupId;
    }

    @Override
    public void onStart() {
        receivingThread = new Thread(this::receive);
        receivingThread.start();
    }

    @Override
    public void onStop() {
        if (receivingThread != null) {
            receivingThread.interrupt();
        }
    }

    private void receive() {
        try {
            Consumer consumer = new Consumer(broker, topic, groupId);
            while (!isStopped()) {
                List<Message> messages = consumer.poll();
                for (Message message : messages) {
                    store(message.getValue());
                }
                Thread.sleep(500); // Poll every 500ms
            }
        } catch (InterruptedException e) {
            // Receiver stopped, clean up
        }
    }
}
