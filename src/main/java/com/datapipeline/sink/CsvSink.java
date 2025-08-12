package com.datapipeline.sink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CsvSink {
    private final Path path;

    public CsvSink(String filePath) {
        this.path = Paths.get(filePath);
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.writeString(path, "timestamp,symbol,price\n", StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize CSV sink", e);
        }
    }

    public void write(String record) {
        try {
            Files.writeString(path, record + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
