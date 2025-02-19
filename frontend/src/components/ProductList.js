import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, CircularProgress, Box
} from '@mui/material';
import { useTheme } from '@mui/material/styles'; 

const ProductList = ({ userId }) => {
    const [products, setProducts] = useState([]);
    const [loadingPrice, setLoadingPrice] = useState(null);
    const navigate = useNavigate();
    const theme = useTheme(); 
    const backendUrl = process.env.REACT_APP_BACKEND_URL;

    const fetchProducts = async () => {
        const token = localStorage.getItem('token');

        if (!token) {
            console.error('Brak tokena. Użytkownik nie jest zalogowany.');
            navigate('/login'); 
            return;
        }

        try {
            const response = await fetch(`${backendUrl}/api/products/user`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            
            if (response.status === 401 || response.status === 403) {
                console.error('Nieautoryzowany dostęp. Użytkownik musi się zalogować ponownie.');
                localStorage.removeItem('token');  // Usuń nieważny token
                navigate('/login');  // Przekierowanie na stronę logowania
                return;
            }

            // Sprawdzenie innych błędów
            if (!response.ok) {
                throw new Error(`Błąd HTTP: ${response.status}`);
            }

            // Przetwarzanie poprawnej odpowiedzi
            const data = await response.json();
            setProducts(data);

        } catch (error) {
            console.error('Błąd podczas pobierania produktów:', error);
            localStorage.removeItem('token');  
            navigate('/welocme');  
            return;
        }
    };


    useEffect(() => {
        fetchProducts();
    }, [userId]);

    const handleDelete = async (productId) => {
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('Brak tokena. Użytkownik nie jest zalogowany.');
            navigate('/login');  
            return;
        }

        const response = await fetch(`${backendUrl}/api/products/${productId}?userId=${userId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });

        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('token');
            navigate('/login'); // Przekierowanie w przypadku błędu uwierzytelnienia
            return;
        }

        if (response.ok) {
            setProducts(products.filter(product => product.id !== productId));
        } else {
            console.error('Błąd podczas usuwania produktu');
        }
    };

    const handleCheckPrice = async (productId) => {
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('Brak tokena. Użytkownik nie jest zalogowany.');
            navigate('/login');  // Przekieruj na stronę logowania, jeśli brak tokena
            return;
        }

        setLoadingPrice(productId);

        const response = await fetch(`${ backendUrl }/api/products/${productId}/check-price`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });

        if (response.status === 401 || response.status === 403 || !response.ok) {
            localStorage.removeItem('token');
            navigate('/login'); // Przekierowanie w przypadku błędu uwierzytelnienia
            console.log ('Tutaj powinno zadziałac przekierowanie');
            return;
        }

        if (response.ok) {
            console.log(`Cena dla produktu o ID ${productId} i została zaktualizowana.`);
            fetchProducts();
        } else {
           console.error('Błąd podczas sprawdzania ceny produktu');
        }

        setLoadingPrice(null);
    };

    const handleDetailsClick = (productId) => {
        navigate(`/products/${productId}`);
    };

    return (
        <TableContainer component={Paper}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Nazwa produktu</TableCell>
                        <TableCell>Link do produktu</TableCell>
                        <TableCell>Cena</TableCell>
                        <TableCell>Akcje</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {products.map(product => (
                        <TableRow key={product.id}>
                            <TableCell>{product.name}</TableCell>
                            <TableCell>
                                <a href={product.url} target="_blank" rel="noopener noreferrer">
                                    {product.url}
                                </a>
                            </TableCell>
                            <TableCell>{product.lastPrice} PLN</TableCell>
                            <TableCell>
                                {loadingPrice === product.id ? (
                                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '36px' }}>
                                        <CircularProgress size={30} />
                                    </Box>
                                ) : (
                                    <Button
                                        variant="contained"
                                        color="primary"
                                        onClick={() => handleCheckPrice(product.id)}
                                    >
                                        Sprawdź cenę
                                    </Button>
                                )}
                            </TableCell>
                            <TableCell>
                                <Button
                                    variant="contained"
                                    color="secondary"
                                    onClick={() => handleDetailsClick(product.id)}
                                >
                                    Szczegóły
                                </Button>
                            </TableCell>
                            <TableCell>
                                <Button
                                    variant="contained"
                                    color="error"
                                    onClick={() => handleDelete(product.id)}
                                >
                                    Usuń
                                </Button>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export default ProductList;
