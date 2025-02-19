import { createTheme, darken } from '@mui/material/styles';

const theme = createTheme({
    palette: {
        primary: {
            main: '#1976D2', // B³êkitny kolor primary
        },
        secondary: {
            main: '#6c757d', // Szary na "Szczegó³y"
        },
        error: {
            main: '#8B0000', // Bordowy na "Usuñ"
        },
    },
    components: {
        MuiButton: {
            styleOverrides: {
                containedPrimary: {
                    color: '#FFFFFF', // Ustawienie bia³ego tekstu na przyciskach primary
                    '&:hover': {
                        backgroundColor: darken('#1976D2', 0.2), // Automatyczne przyciemnianie koloru primary
                    },
                },
                containedSecondary: {
                    color: '#FFFFFF',
                    '&:hover': {
                        backgroundColor: darken('#6c757d', 0.2), // Automatyczne przyciemnianie koloru secondary
                    },
                },
                containedError: {
                    color: '#FFFFFF',
                    '&:hover': {
                        backgroundColor: darken('#8B0000', 0.2), // Automatyczne przyciemnianie koloru error
                    },
                },
            },
        },
    },
});

export default theme;
