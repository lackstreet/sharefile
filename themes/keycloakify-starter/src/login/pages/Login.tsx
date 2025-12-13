import { PageProps } from "../types.ts";
import { Button, Card, CardContent, Grid, TextField, Typography, Link, Box, Divider, createTheme } from "@mui/material";

const Login = (props: PageProps<"login.ftl">) => {
    const { kcContext, i18n, Template } = props;
    const { realm, url, messagesPerField } = kcContext;
    const { msgStr } = i18n;
    const colorPrimary = "#a2988e";
    const colorSecondary = "#fff6ed";

    const theme = createTheme({
        palette: {
            laminam: {
                main: "rgba(162,152,143,0.99)",
                light: "#fff6ed",
                dark: "#a2988e",
                contrastText: "#fff6ed"
            }
        }
    });

    return (
        <Template kcContext={kcContext} i18n={i18n} headerNode={null} classes={{ root: "kc-wetransfer-layout" }}>
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                minHeight="100vh"
                sx={{
                    background: `linear-gradient(135deg, ${colorSecondary} 70%,${colorPrimary}  30%)`,
                    padding: 2
                }}
            >
                <Card
                    sx={{
                        width: "100%",
                        maxWidth: 440,
                        boxShadow: "0 20px 60px rgba(0,0,0,0.3)",
                        borderRadius: "16px",
                        border: "none"
                    }}
                >
                    <CardContent sx={{ padding: "48px 40px" }}>
                        {/* Logo/Brand area */}
                        <Box textAlign="center" marginBottom="32px">
                            <Typography
                                variant="h4"
                                fontWeight={1000}
                                sx={{
                                    color: colorPrimary,
                                    marginBottom: "8px"
                                }}
                            >
                                Lamshare
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ fontSize: "14px" }}>
                                Sign in to share your files
                            </Typography>
                        </Box>

                        <form action={url.loginAction} method="post">
                            <Grid container spacing={2.5}>
                                <Grid size={12}>
                                    <TextField
                                        error={messagesPerField.existsError("username")}
                                        helperText={messagesPerField.getFirstError("username")}
                                        fullWidth
                                        name="username"
                                        placeholder="Enter your email"
                                        label={msgStr("email")}
                                        variant="outlined"
                                        autoComplete="username"
                                        sx={{
                                            "& .MuiOutlinedInput-root": {
                                                borderRadius: "8px",
                                                backgroundColor: "#fafafa",
                                                "&:hover": {
                                                    backgroundColor: "#fff"
                                                },
                                                "&.Mui-focused": {
                                                    backgroundColor: "#fff"
                                                }
                                            }
                                        }}
                                    />
                                </Grid>

                                <Grid size={12}>
                                    <TextField
                                        error={messagesPerField.existsError("password")}
                                        helperText={messagesPerField.getFirstError("password")}
                                        fullWidth
                                        name="password"
                                        placeholder="Enter your password"
                                        label={msgStr("password")}
                                        type="password"
                                        variant="outlined"
                                        autoComplete="current-password"
                                        sx={{
                                            "& .MuiOutlinedInput-root": {
                                                borderRadius: "8px",
                                                backgroundColor: "#fafafa",
                                                "&:hover": {
                                                    backgroundColor: "#f5f5f5"
                                                },
                                                "&.Mui-focused": {
                                                    backgroundColor: "#fff"
                                                }
                                            }
                                        }}
                                    />
                                </Grid>

                                <Grid size={12} sx={{ marginTop: "8px" }}>
                                    <Button
                                        type="submit"
                                        fullWidth
                                        variant="contained"
                                        sx={{
                                            padding: "14px 0",
                                            borderRadius: "8px",
                                            fontSize: "15px",
                                            fontWeight: 600,
                                            textTransform: "none",
                                            background: `linear-gradient(135deg, ${colorSecondary} 5%, ${colorPrimary} 0%)`,
                                            boxShadow: "0 4px 15px rgba(162,152,143)",
                                            transition: "all 0.3s ease",
                                            "&:hover": {
                                                boxShadow: "0 6px 20px rgba(162,152,143,0.99)",
                                                transform: "translateY(-2px)"
                                            }
                                        }}
                                    >
                                        {msgStr("doLogIn")}
                                    </Button>
                                </Grid>

                                {url.loginResetCredentialsUrl && (
                                    <Grid size={12} textAlign="center" marginTop="8px">
                                        <Link
                                            href={url.loginResetCredentialsUrl}
                                            underline="hover"
                                            sx={{
                                                color: `${colorPrimary}`,
                                                fontSize: "14px",
                                                fontWeight: 530,
                                                "&:hover": {
                                                    color: "#764ba2"
                                                }
                                            }}
                                        >
                                            {msgStr("doForgotPassword")}
                                        </Link>
                                    </Grid>
                                )}

                                {/* Divider */}
                                <Grid size={12} sx={{ marginTop: "24px", marginBottom: "8px" }}>
                                    <Divider sx={{ color: "text.secondary" }}>
                                        <Typography variant="body2" color="text.secondary" fontSize="13px">
                                            or
                                        </Typography>
                                    </Divider>
                                </Grid>

                                {/* Registration Button */}
                                {realm.registrationAllowed && url.registrationUrl && (
                                    <Grid size={12}>
                                        <Button
                                            href={url.registrationUrl}
                                            fullWidth
                                            variant="outlined"
                                            sx={{
                                                padding: "14px 0",
                                                borderRadius: "8px",
                                                fontSize: "15px",
                                                fontWeight: 600,
                                                textTransform: "none",
                                                borderColor: `rgba(162,152,143,0.4)`,
                                                color: `${colorPrimary}`,
                                                borderWidth: "2px",
                                                transition: "all 0.3s ease",
                                                "&:hover": {
                                                    borderWidth: "2px",
                                                    borderColor: `${colorPrimary}`,
                                                    backgroundColor: "#fff",
                                                    transform: "translateY(-2px)"
                                                }
                                            }}
                                        >
                                            Login with Laminam
                                        </Button>
                                    </Grid>
                                )}
                            </Grid>
                        </form>

                        {/* Footer text */}
                        {realm.registrationAllowed && (
                            <Box textAlign="center" marginTop="24px">
                                <Typography variant="body2" color="text.secondary" fontSize="13px">
                                    Don't have an account?{" "}
                                    <Link
                                        href={url.registrationUrl}
                                        sx={{
                                            color: "#667eea",
                                            fontWeight: 600,
                                            textDecoration: "none",
                                            "&:hover": {
                                                textDecoration: "underline",
                                                color: "#764ba2"
                                            }
                                        }}
                                    >
                                        Sign up free
                                    </Link>
                                </Typography>
                            </Box>
                        )}
                    </CardContent>
                </Card>
            </Box>
        </Template>
    );
};

export default Login;
