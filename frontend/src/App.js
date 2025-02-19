import React from 'react';
import { AppBar, Toolbar, Button, Box, Typography } from '@mui/material';
import AddProductForm from './components/AddProductForm';
import ProductList from './components/ProductList';
import LoginForm from './components/LoginForm';
import { useNavigate } from 'react-router-dom';
import theme from './theme/theme';
import { ThemeProvider } from '@mui/material/styles';

function App() {
    const [productsUpdated, setProductsUpdated] = React.useState(false);
    const [isLoggedIn, setIsLoggedIn] = React.useState(false);
    const navigate = useNavigate();

    const handleProductAdded = () => {
        setProductsUpdated(!productsUpdated);
    };

    React.useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            setIsLoggedIn(true);
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        setIsLoggedIn(false);
        navigate('/');
    };

    const handleAccountSettings = () => {
        navigate('/account');  // Przejście do strony "Moje konto"
    };

    return (
        <ThemeProvider theme={theme}>
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6" sx={{ flexGrow: 1 }}>
                        Moje produkty
                    </Typography>
                    {isLoggedIn && (
                        <>
                            <Button variant="contained" color="primary" onClick={handleAccountSettings} sx={{ marginRight: 2 }}>
                                Moje konto
                            </Button>
                            <Button variant="contained" color="secondary" onClick={handleLogout}>
                                Wyloguj się
                            </Button>
                        </>
                    )}
                </Toolbar>
            </AppBar>

            <Box sx={{ padding: 3 }}>
                {isLoggedIn ? (
                    <>
                        <AddProductForm onProductAdded={handleProductAdded} />
                        <ProductList key={productsUpdated} />
                    </>
                ) : (
                    <LoginForm onLoginSuccess={() => setIsLoggedIn(true)} />
                )}
            </Box>
        </ThemeProvider>
    );
}

export default App;
