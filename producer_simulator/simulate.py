import requests
import json
import time
import random

BROKER_URL = "http://localhost:4567/produce/stocks"

def generate_event():
    return {
        "timestamp": int(time.time()),
        "symbol": random.choice(["AAPL", "GOOGL", "MSFT", "AMZN"]),
        "price": round(random.uniform(100, 500), 2)
    }

def main():
    while True:
        event = generate_event()
        key = event["symbol"]
        headers = {'Content-Type': 'application/json'}
        
        try:
            response = requests.post(f"{BROKER_URL}?key={key}", data=json.dumps(event), headers=headers)
            response.raise_for_status()
            print(f"Sent event: {event}, Response: {response.text}")
        except requests.exceptions.RequestException as e:
            print(f"Error sending event: {e}")
            
        time.sleep(random.uniform(0.5, 2.0))

if __name__ == "__main__":
    main()
