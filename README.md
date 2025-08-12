# Real-Time Data Pipeline: A Tutorial

This project is a simplified, local, end-to-end real-time data pipeline that simulates the ingestion, processing, and visualization of data.

## High-Level Architecture

The pipeline is composed of several key components that work together to create a seamless flow of data from generation to visualization.

1.  **Data Simulator (Python)**: A Python script that generates mock data and sends it to our broker.
2.  **Broker (Java)**: The heart of our pipeline, responsible for ingesting and distributing messages.
3.  **REST API (Java)**: An interface for our broker, allowing producers to send data and consumers to receive it.
4.  **Stream Processor (Java + Apache Spark)**: A processing engine that consumes data from the broker, transforms it, and sends it to a data sink.
5.  **Data Sink (CSV)**: A simple CSV file where our processed data is stored.
6.  **Web Frontend (HTML/JS)**: A real-time dashboard to visualize the data flowing through the pipeline.

## Deep Dive into the Components

Let's take a closer look at each component and the files that make them up.

### 1. Data Simulator (`producer_simulator/simulate.py`)

This Python script is responsible for generating a continuous stream of mock stock price data.

-   **`generate_event()`**: This function creates a JSON object representing a stock price event. It includes a timestamp, a stock symbol (randomly chosen from a list), and a random price.
-   **`main()`**: This function runs in a continuous loop, generating events and sending them to the broker's REST API via a POST request. The stock symbol is used as a key to ensure that all events for the same stock go to the same partition.

### 2. The Broker (Java)

The broker is the central nervous system of our data pipeline. It's responsible for ingesting messages from producers and making them available to consumers.

-   **`Broker.java`**: This class manages topics and partitions.
    -   **`createTopic(String topicName, int partitionCount)`**: Creates a new topic with a specified number of partitions. A topic is a category or feed name to which messages are published.
    -   **`getPartitions(String topicName)`**: Retrieves the list of partitions for a given topic.
-   **`Partition.java`**: This class represents a single partition within a topic. A partition is an ordered, immutable sequence of messages.
    -   **`addMessage(Message msg)`**: Adds a message to the partition's queue and appends it to a log file on disk (`logs/partition-*.log`). This ensures data persistence.
    -   **`poll()`**: Retrieves the next message from the partition's queue.

### 3. REST API (`api/RestApiServer.java`)

This class exposes the broker's functionality through a RESTful API, built using the SparkJava framework.

-   **`main(String[] args)`**: The entry point of the application. It initializes the broker, starts the Spark processor, and defines the API endpoints.
-   **`POST /produce/:topic`**: This endpoint allows producers to send messages. It receives a topic name, an optional key, and the message body. The message is then sent to the broker.
-   **`GET /consume/:topic`**: This endpoint allows consumers to fetch messages. It takes a topic name and a consumer group ID. It ensures that each consumer group gets its own copy of the messages.

### 4. Stream Processor (`processing/SparkProcessor.java`)

This class uses Apache Spark Streaming to perform real-time data processing.

-   **`start()`**: This method sets up and starts the Spark Streaming job. It creates a custom receiver (`BrokerReceiver`) to consume messages from our broker. The stream is then processed:
    1.  The JSON message is parsed.
    2.  A filter is applied to keep only stocks with a price greater than 250.
    3.  The filtered data is written to a CSV file using our `CsvSink`.
-   **`stop()`**: Gracefully stops the Spark Streaming context.

### 5. Data Sink (`sink/CsvSink.java`)

This class is a simple data sink that writes processed data to a CSV file.

-   **`write(String record)`**: Appends a new record to the specified CSV file (`data/processed_stocks.csv`).

### 6. Web Frontend (`resources/public/`)

A simple web interface for visualizing the raw data being consumed from the broker in real-time.

-   **`index.html`**: The main HTML file that sets up the page structure.
-   **`style.css`**: Contains the styling for the web page.
-   **`app.js`**: This script polls the `/consume/stocks` endpoint every second. It then parses the received messages and updates a live feed and a real-time chart (using Chart.js) to display the stock prices.

## Data Flow Summary

1.  The **Python simulator** generates a stock price event.
2.  It sends the event to the **REST API's** `POST /produce/stocks` endpoint.
3.  The **Broker** receives the message and appends it to a partition log file.
4.  The **Web Frontend** polls the `GET /consume/stocks` endpoint and displays the raw message in a chart and a live feed.
5.  The **Spark Processor** consumes the message, filters it, and writes the processed record to the **CSV Sink**.

## How to Run the Pipeline

1.  **Build the Java application:**
    ```bash
    cd data-pipeline
    mvn clean package
    ```

2.  **Run the Java application (Broker, REST API, Spark Processor):**
    ```bash
    java -jar target/data-pipeline-1.0-SNAPSHOT.jar
    ```

3.  **Run the Python data simulator (in a separate terminal):**
    ```bash
    python producer_simulator/simulate.py
    ```

4.  **View the web frontend:**
    Open a web browser and navigate to `http://localhost:4567`. You should see the live data feed and chart updating in real-time.

5.  **Check the processed data:**
    After a few moments, you can inspect the `data/processed_stocks.csv` file to see the high-value stocks that have been filtered by the Spark job.
