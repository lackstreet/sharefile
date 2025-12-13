import "@mui/material/styles";

declare module "@mui/material/styles" {
    interface Palette {
        laminam: Palette["primary"];
    }
    interface PaletteOptions {
        laminam?: PaletteOptions["primary"];
    }
}

declare module "@mui/material/TextField" {
    interface TextFieldPropsColorOverrides {
        laminam: true;
    }
}
