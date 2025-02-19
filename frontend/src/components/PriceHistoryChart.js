import React, { useEffect, useState } from 'react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

// Rejestracja skali i komponentów
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

const PriceHistoryChart = ({ productId }) => {
    const [priceHistory, setPriceHistory] = useState([]);

    const fetchPriceHistory = async () => {
        const response = await fetch(`http://localhost:8080/api/products/${productId}/price-history`);
        const data = await response.json();
        setPriceHistory(data);
    };

    useEffect(() => {
        fetchPriceHistory();
    }, [productId]);

    const data = {
        labels: priceHistory.map(price => new Date(price.dateChecked).toLocaleDateString()),
        datasets: [
            {
                label: 'Cena',
                data: priceHistory.map(price => price.priceValue),
                fill: false,
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1,
            },
        ],
    };

    return (
        <div>
            <h3>Historia cen</h3>
            <Line data={data} />
        </div>
    );
};

export default PriceHistoryChart;