import { Box, Card, Typography } from "@mui/material";
import { CustomTemplateProps } from "./types.ts";

const Template = (props: CustomTemplateProps) => {
   const { children, kcContext } = props;
   const {realm} = kcContext;
    return (
        <Box display="flex" alignItems="center" justifyContent="center" height="100vh">
            <Box width = "400px">
                    <Card>{children}</Card>
                <Box display="flex">
                    <Typography variant="body2" color="text.secondary" sx={{ fontSize: "12px", marginTop: "16px", textAlign: "center", width: "100%" }}>
                        &copy; {new Date().getFullYear()} Sharefile. All rights reserved.
                    </Typography>
                </Box>
            </Box>
        </Box>
    );
};

export {Template}