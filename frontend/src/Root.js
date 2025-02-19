import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import App from './App';  
import WelcomePage from './components/WelcomePage';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm';
import ProductDetails from './components/ProductDetails';
import AccountSettings from './components/AccountSettings';  
import { ThemeProvider } from '@mui/material/styles';  
import theme from './theme/theme';  
export default function Root() {
    return (
        <ThemeProvider theme={theme}>  {/* Otoczenie ca³ej aplikacji ThemeProvider */}
            <Router>
                <Routes>
                    <Route path="/" element={<WelcomePage />} />
                    <Route path="/main" element={<App />} />
                    <Route path="/login" element={<LoginForm onLoginSuccess={() => { }} />} />
                    <Route path="/register" element={<RegisterForm />} />
                    <Route path="/products/:productId" element={<ProductDetails />} />
                    <Route path="/account" element={<AccountSettings />} />  
                </Routes>
            </Router>
        </ThemeProvider>
    );
}