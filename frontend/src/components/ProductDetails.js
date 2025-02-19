import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Link, AppBar, Toolbar, Button, TextField, MenuItem, Select, InputLabel, FormControl } from '@mui/material';

const ProductDetails = () => {
    const { productId } = useParams();
    const [product, setProduct] = useState(null);
    const [priceHistory, setPriceHistory] = useState([]);
    const [notificationType, setNotificationType] = useState('lastPrice'); 
    const [notificationPrice, setNotificationPrice] = useState(''); 
    const navigate = useNavigate();
    const backendUrl = process.env.REACT_APP_BACKEND_URL;

    const fetchProductDetails = async () => {
        const token = localStorage.getItem('token');  

        if (!token) {
            console.error('Brak tokena. Użytkownik nie jest zalogowany.');
            return;
        }

        try {
            const response = await fetch(`${backendUrl}/api/products/${productId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,  
                    'Content-Type': 'application/json',  
                },
            });

            if (!response.ok) {
                throw new Error(`Błąd HTTP: ${response.status}`);
            }

            const data = await response.json();
            setProduct(data);

            

        } catch (error) {
            console.error('Błąd podczas pobierania szczegółów produktu:', error);
        }
    };

    const fetchPriceHistory = async () => {
        const token = localStorage.getItem('token');  // Pobierz token JWT z localStorage

        if (!token) {
            console.error('Brak tokena. Użytkownik nie jest zalogowany.');
            return;
        }

        try {
            const response = await fetch(`${backendUrl}/api/products/${productId}/price-history`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,  // Dołącz token do nagłówka
                    'Content-Type': 'application/json',  // Ustaw nagłówki
                },
            });

            if (!response.ok) {
                throw new Error(`Błąd HTTP: ${response.status}`);
            }

            const data = await response.json();
            setPriceHistory(data);
        } catch (error) {
            console.error('Błąd podczas pobierania historii cen:', error);
        }
    };

    const fetchUserProduct = async () => {
        const token = localStorage.getItem('token');  // Pobierz token JWT z localStorage

        if (!token) {
            console.error('Brak tokena. Użytkownik nie jest zalogowany.');
            return;
        }

        try {
            const response = await fetch(`${backendUrl}/api/products/${productId}/user-product`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,  // Dołącz token do nagłówka
                    'Content-Type': 'application/json',  // Ustaw nagłówki
                },
            });
            const data = await response.json();

            if (data.notificationType) {
                setNotificationType(data.notificationType);
            }

            if (data.notificationType === 'BELOW_THRESHOLD' && data.notificationPrice) {
                setNotificationPrice(data.notificationPrice);
            }

        } catch (error) {
            console.error('Błąd podczas pobierania historii cen:', error);
        }
    };

    const handleNotificationChange = (event) => {
        setNotificationType(event.target.value);
    };

    const handleSaveNotification = async () => {
        const token = localStorage.getItem('token');  // Pobierz token JWT z localStorage
        if (!token) {
            console.error('Brak tokena. Użytkownik nie jest zalogowany.');
            return;
        }

        const notificationData = {
            notificationType,
            notificationPrice: notificationType === 'BELOW_THRESHOLD' ? notificationPrice : null,
        };

        try {
            const response = await fetch(`${backendUrl}/api/products/${productId}/set-notification`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,  // Dołącz token do nagłówka
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(notificationData),
            });

            if (!response.ok) {
                throw new Error('Błąd podczas zapisywania ustawień powiadomienia');
            }

            alert('Ustawienia powiadomienia zostały zapisane!');
        } catch (error) {
            console.error('Błąd podczas zapisywania powiadomienia:', error);
        }
    };

    const handleBack = () => {
        navigate(-1);  // Powrót do poprzedniej strony
    };

    useEffect(() => {
        fetchProductDetails();
        fetchPriceHistory();
        fetchUserProduct();
    }, [productId]);

    if (!product) {
        return <Typography variant="h6" align="center">Ładowanie...</Typography>;
    }

    return (
        <>
            <AppBar position="static" color="primary">  {/* Użycie koloru primary z motywu */}
                <Toolbar>
                    <Typography variant="h6" sx={{ flexGrow: 1 }}>
                        Szczegóły Produktu
                    </Typography>
                    <Button variant="contained" color="secondary" onClick={handleBack}>
                        Wstecz
                    </Button>
                </Toolbar>
            </AppBar>

            <Box sx={{ maxWidth: '800px', margin: '0 auto', padding: 3 }}>
                <Box sx={{ marginBottom: 3 }}>
                    <Typography variant="h5" component="div" gutterBottom>
                        {product.name}
                    </Typography>
                    <Typography variant="body1" gutterBottom>
                        Link do produktu:{" "}
                        <Link href={product.url} target="_blank" rel="noopener noreferrer">
                            {product.url}
                        </Link>
                    </Typography>
                    <Typography variant="h6" color="primary">
                        Aktualna cena: {product.lastPrice} PLN
                    </Typography>
                </Box>

                {}
                <Box sx={{ marginTop: 4 }}>
                    <FormControl fullWidth variant="outlined" sx={{ marginBottom: 2 }}>
                        <InputLabel id="notification-select-label">Warunek powiadomienia</InputLabel>
                        <Select
                            labelId="notification-select-label"
                            id="notification-select"
                            value={notificationType}
                            onChange={handleNotificationChange}
                            label="Warunek powiadomienia"
                        >
                            <MenuItem value="BELOW_LAST_PRICE">Spadek poniżej ostatniej ceny</MenuItem>
                            <MenuItem value="BELOW_THRESHOLD">Spadek poniżej określonej ceny</MenuItem>
                        </Select>
                    </FormControl>

                    {notificationType === 'BELOW_THRESHOLD' && (
                        <TextField
                            label="Ustal próg ceny"
                            variant="outlined"
                            fullWidth
                            type="number"
                            value={notificationPrice}
                            onChange={(e) => setNotificationPrice(e.target.value)}
                            sx={{ marginBottom: 2 }}
                        />
                    )}

                    <Button variant="contained" color="primary" onClick={handleSaveNotification} sx={{ mb: 4 }} >
                        Zapisz powiadomienie
                    </Button>
                </Box>

                {/* Historia cen */}
                <Typography variant="h5" gutterBottom>
                    Historia cen
                </Typography>
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell><Typography variant="subtitle1" fontWeight="bold">Data sprawdzenia</Typography></TableCell>
                                <TableCell><Typography variant="subtitle1" fontWeight="bold">Cena (PLN)</Typography></TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {priceHistory
                                .sort((a, b) => new Date(b.dateChecked) - new Date(a.dateChecked))  // Sortowanie od najnowszych do najstarszych
                                .map((price) => (
                                    <TableRow key={price.id}>
                                        <TableCell>{new Date(price.dateChecked).toLocaleString()}</TableCell>
                                        <TableCell>{price.priceValue} PLN</TableCell>
                                    </TableRow>
                                ))}
                        </TableBody>
                    </Table>
                </TableContainer>


            </Box>
        </>
    );
};

export default ProductDetails;