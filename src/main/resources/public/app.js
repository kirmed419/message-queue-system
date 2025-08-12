document.addEventListener('DOMContentLoaded', () => {
    const outputDiv = document.getElementById('output');
    const ctx = document.getElementById('dataChart').getContext('2d');

    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            datasets: [{
                label: 'Real-Time Data',
                data: [],
                borderColor: 'rgba(0, 255, 255, 0.8)',
                backgroundColor: 'rgba(0, 255, 255, 0.2)',
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'second',
                        displayFormats: {
                            second: 'HH:mm:ss'
                        }
                    },
                    ticks: {
                        color: '#e0e0e0'
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                },
                y: {
                    beginAtZero: false,
                    ticks: {
                        color: '#e0e0e0'
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                }
            },
            plugins: {
                legend: {
                    labels: {
                        color: '#e0e0e0'
                    }
                }
            }
        }
    });

    function pollMessages() {
        fetch('/consume/stocks?groupId=web-ui')
            .then(res => res.json())
            .then(messages => {
                messages.forEach(msg => {
                    try {
                        const data = JSON.parse(msg.value);
                        const p = document.createElement('p');
                        p.textContent = `[${new Date(data.timestamp * 1000).toLocaleTimeString()}] Symbol: ${data.symbol}, Value: ${data.price.toFixed(2)}`;
                        outputDiv.prepend(p);

                        chart.data.datasets[0].data.push({
                            x: new Date(data.timestamp * 1000),
                            y: data.price
                        });
                        
                        if(chart.data.datasets[0].data.length > 100) {
                            chart.data.datasets[0].data.shift();
                        }
                        chart.update('quiet');

                    } catch (e) {
                        console.error("Error parsing message:", e);
                    }
                });
            });
    }

    setInterval(pollMessages, 1000);
});
