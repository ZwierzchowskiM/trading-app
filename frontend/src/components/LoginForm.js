import React, { useState } from 'react';
import { TextField, Button, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';  // Dodaj import useNavigate

const LoginForm = ({ onLoginSuccess }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const backendUrl = process.env.REACT_APP_BACKEND_URL;

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            
            const response = await fetch(`${backendUrl}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                throw new Error('Błąd podczas logowania');
            }

            const token = await response.text();
            console.log('otrzymany token JWT:', token);
            localStorage.setItem('token', token);  
            onLoginSuccess(); 
            navigate('/main');  
        } catch (error) {
            setError('Nieprawidłowe dane logowania');
            console.log('Error:', error);
        }
    };

    const handleRegisterClick = () => {
        navigate('/register');  // Przekierowanie do strony rejestracji
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <TextField
                label="Nazwa użytkownika"
                variant="outlined"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                sx={{ marginBottom: 2, maxWidth: '400px', width: '100%' }}
            />
            <TextField
                label="Hasło"
                variant="outlined"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                sx={{ marginBottom: 2, maxWidth: '400px', width: '100%' }}
            />
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <Button
                type="submit"
                variant="contained"
                color="primary"
                sx={{ maxWidth: '400px', width: '100%', marginBottom: 2 }}
            >
                Zaloguj się
            </Button>
            <Button
                variant="outlined"
                color="secondary"
                sx={{ maxWidth: '400px', width: '100%' }}
                onClick={handleRegisterClick}
            >
                Rejestracja
            </Button>
        </Box>
    );
};

export default LoginForm;
