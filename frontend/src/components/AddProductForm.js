import React, { useState } from 'react';
import { TextField, Button, Box, CircularProgress } from '@mui/material';

const AddProductForm = ({ onProductAdded }) => {
    const [productUrl, setProductUrl] = useState('');
    const [loading, setLoading] = useState(false); 
    const backendUrl = process.env.REACT_APP_BACKEND_URL;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);  

        const token = localStorage.getItem('token'); 

        try {
            const response = await fetch(`${ backendUrl }/api/products/`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,  
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ url: productUrl }), 
            });

            if (response.ok) {
                console.log('Produkt dodany!');
                setProductUrl('');
                onProductAdded();
            } else {
                console.log('Błąd podczas dodawania produktu');
            }
        } catch (error) {
            console.error('Wystąpił błąd:', error);
        } finally {
            setLoading(false);  
        }
    };

    return (
        <Box
            component="form"
            onSubmit={handleSubmit}
            sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: '400px', margin: '0 auto' }}
        >
            <TextField
                label="URL produktu"
                variant="outlined"
                value={productUrl}
                onChange={(e) => setProductUrl(e.target.value)}
                required
            />
            <Button type="submit" variant="contained" color="primary" disabled={loading}>
                {loading ? <CircularProgress size={24} /> : 'Dodaj produkt'}
            </Button>
        </Box>
    );
};

export default AddProductForm;
