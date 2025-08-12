package com.datapipeline.processing;

import com.datapipeline.broker.Broker;
import com.datapipeline.consumer.BrokerReceiver;
import com.datapipeline.sink.CsvSink;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class SparkProcessor {
    private final Broker broker;
    private final JavaStreamingContext ssc;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final CsvSink csvSink = new CsvSink("data/processed_stocks.csv");

    public SparkProcessor(Broker broker) {
        this.broker = broker;
        SparkConf conf = new SparkConf().setAppName("DataPipelineProcessor").setMaster("local[*]");
        this.ssc = new JavaStreamingContext(conf, Durations.seconds(5));
    }

    public void start() {
        JavaDStream<String> stream = ssc.receiverStream(new BrokerReceiver(broker, "stocks", "spark-processor"));

        JavaDStream<JsonNode> jsonStream = stream.map(record -> objectMapper.readTree(record));
        
        JavaDStream<JsonNode> filteredStream = jsonStream.filter(node -> 
            node.has("price") && node.get("price").asDouble() > 250.0
        );

        filteredStream.foreachRDD(rdd -> {
            rdd.foreach(record -> {
                String csvRecord = String.format("%d,%s,%.2f",
                        record.get("timestamp").asLong(),
                        record.get("symbol").asText(),
                        record.get("price").asDouble());
                csvSink.write(csvRecord);
            });
        });

        ssc.start();
    }

    public void awaitTermination() throws InterruptedException {
        ssc.awaitTermination();
    }

    public void stop() {
        ssc.stop(true, true);
    }
}
