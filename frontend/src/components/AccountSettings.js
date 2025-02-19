import React, { useState } from 'react';
import { TextField, Button, Box, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';  // Import useNavigate

const AccountSettings = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();  // Hook do nawigacji

    const handleSave = async (e) => {
        e.preventDefault();

        try {
            const token = localStorage.getItem('token');  // Pobierz token JWT z localStorage
            const response = await fetch('http://localhost:8080/api/users/account', {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,  // Dołącz token do nagłówka
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) {
                throw new Error('Błąd podczas zapisywania zmian');
            }

            alert('Dane zostały zaktualizowane!');
        } catch (error) {
            console.error('Błąd podczas zapisywania danych:', error);
            alert('Nie udało się zaktualizować danych.');
        }
    };

    const handleBack = () => {
        navigate(-1);  // Powrót do poprzedniej strony
    };

    return (
        <Box sx={{ width: '100%', maxWidth: 500, mx: 'auto', mt: 4 }}>
            <Typography variant="h4" gutterBottom>
                Ustawienia konta
            </Typography>
            <form onSubmit={handleSave}>
                <TextField
                    label="E-mail"
                    variant="outlined"
                    fullWidth
                    margin="normal"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
                <TextField
                    label="Hasło"
                    variant="outlined"
                    fullWidth
                    margin="normal"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <Button type="submit" variant="contained" color="primary" fullWidth sx={{ mt: 2 }}>
                    Zapisz
                </Button>
                <Button 
                    variant="outlined" 
                    color="secondary" 
                    fullWidth 
                    sx={{ mt: 2 }} 
                    onClick={handleBack}
                >
                    Wstecz
                </Button>
            </form>
        </Box>
    );
};

export default AccountSettings;
