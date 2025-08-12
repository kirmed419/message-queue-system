package com.datapipeline.broker;

import com.datapipeline.model.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Partition {
    private final int id;
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    private final Path logFile;

    public Partition(int id) {
        this.id = id;
        this.logFile = Paths.get("logs/partition-" + id + ".log");
        try {
            Files.createDirectories(logFile.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Could not create log directory", e);
        }
    }

    public void addMessage(Message msg) {
        queue.add(msg);
        try {
            Files.writeString(logFile, msg.getValue() + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message poll() {
        return queue.poll();
    }

    public int getId() {
        return id;
    }
}
