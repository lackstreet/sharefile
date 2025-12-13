import { PageProps } from "../types.ts";
import { Button, Card, CardContent, Grid, TextField, Typography, Link, Box, Divider, Avatar } from "@mui/material";
import { CloudUpload } from "@mui/icons-material";

const Login = (props: PageProps<"login.ftl">) => {
    const { kcContext, i18n, Template } = props;
    const { realm, url, messagesPerField } = kcContext;

    return (
        <Template kcContext={kcContext} i18n={i18n} >
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="auto" padding={0.1}>
                {/* Login Card  */}
                <Card
                    sx={{
                        width: "100%",
                        maxWidth: 420,
                        background: "#ffffff"
                    }}
                >
                    <CardContent sx={{ padding: "36px 32px" }}>
                        {/* Logo/Icon */}
                        <Box display="flex" justifyContent="center" marginBottom="20px">
                            <Avatar
                                sx={{
                                    width: 64,
                                    height: 64,
                                    background: "linear-gradient(135deg, #fff 38%, #a2988e 0%)",
                                    boxShadow: "0 4px 16px rgba(162, 152, 142, 0.3)"
                                }}
                            >
                                <CloudUpload sx={{ fontSize: 32, color: "#fff6ed" }} />
                            </Avatar>
                        </Box>

                        {/* Brand name */}
                        <Typography
                            textAlign="center"
                            marginBottom="28px"
                            variant="h5"
                            fontWeight={700}
                            sx={{
                                background: "linear-gradient(135deg, #a2988e 0%, #6d6560 100%)",
                                WebkitBackgroundClip: "text",
                                WebkitTextFillColor: "transparent",
                                backgroundClip: "text"
                            }}
                        >
                            Lamshare{" "}
                            <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ fontSize: "13px", marginTop: "8px" }}>
                                share your files with Laminam
                            </Typography>
                        </Typography>
                        <form action={url.loginAction} method="post">
                            <Grid container spacing={2}>
                                {/* Email Field */}
                                <Grid size={12}>
                                    <TextField
                                        error={messagesPerField.existsError("username")}
                                        helperText={messagesPerField.getFirstError("username")}
                                        fullWidth
                                        size="small"
                                        name="username"
                                        placeholder="Enter your email"
                                        label="Email"
                                        variant="outlined"
                                        autoComplete="username"
                                        autoFocus
                                        sx={{
                                            "& .MuiOutlinedInput-root": {
                                                borderRadius: "10px",
                                                backgroundColor: "#fafafa",
                                                "& fieldset": {
                                                    borderColor: "#e0e0e0"
                                                },
                                                "&:hover fieldset": {
                                                    borderColor: "#a2988e"
                                                },
                                                "&.Mui-focused": {
                                                    backgroundColor: "#fff",
                                                    "& fieldset": {
                                                        borderColor: "#a2988e"
                                                    }
                                                }
                                            },
                                            "& .MuiInputLabel-root.Mui-focused": {
                                                color: "#a2988e"
                                            }
                                        }}
                                    />
                                </Grid>

                                {/* Password Field */}
                                <Grid size={12}>
                                    <TextField
                                        error={messagesPerField.existsError("password")}
                                        helperText={messagesPerField.getFirstError("password")}
                                        fullWidth
                                        size="small"
                                        name="password"
                                        placeholder="Enter your password"
                                        label="Password"
                                        type="password"
                                        variant="outlined"
                                        autoComplete="current-password"
                                        sx={{
                                            "& .MuiOutlinedInput-root": {
                                                borderRadius: "10px",
                                                backgroundColor: "#fafafa",
                                                "& fieldset": {
                                                    borderColor: "#e0e0e0"
                                                },
                                                "&:hover fieldset": {
                                                    borderColor: "#a2988e"
                                                },
                                                "&.Mui-focused": {
                                                    backgroundColor: "#fff",
                                                    "& fieldset": {
                                                        borderColor: "#a2988e"
                                                    }
                                                }
                                            },
                                            "& .MuiInputLabel-root.Mui-focused": {
                                                color: "#a2988e"
                                            }
                                        }}
                                    />
                                </Grid>

                                {/* Sign In Button */}
                                <Grid size={12} sx={{ marginTop: "4px" }}>
                                    <Button
                                        type="submit"
                                        fullWidth
                                        variant="contained"
                                        sx={{
                                            padding: "12px 0",
                                            borderRadius: "10px",
                                            fontSize: "15px",
                                            fontWeight: 600,
                                            textTransform: "none",
                                            background: "linear-gradient(135deg, #a2988e 90%, #fff 20%)",
                                            boxShadow: "0 4px 16px rgba(162, 152, 142, 0.35)",
                                            transition: "all 0.3s ease",
                                            "&:hover": {
                                                background: "linear-gradient(135deg, #a2988e 91%, #fff 10%)",
                                                boxShadow: "0 6px 20px rgba(162, 152, 142, 0.5)",
                                                transform: "translateY(-1px)"
                                            }
                                        }}
                                    >
                                        Sign In
                                    </Button>
                                </Grid>

                                {/* Forgot Password Link */}
                                {url.loginResetCredentialsUrl && (
                                    <Grid size={12} textAlign="center">
                                        <Link
                                            href={url.loginResetCredentialsUrl}
                                            underline="hover"
                                            sx={{
                                                color: "#a2988e",
                                                fontSize: "13px",
                                                fontWeight: 500,
                                                "&:hover": {
                                                    color: "#8a7f75"
                                                }
                                            }}
                                        >
                                            Forgot password?
                                        </Link>
                                    </Grid>
                                )}

                                {/* Divider */}
                                {realm.registrationAllowed && url.registrationUrl && (
                                    <Grid size={12} sx={{ marginTop: "16px", marginBottom: "4px" }}>
                                        <Divider>
                                            <Typography variant="body2" color="text.secondary" fontSize="12px">
                                                or
                                            </Typography>
                                        </Divider>
                                    </Grid>
                                )}

                                {/* Registration Button */}
                                {realm.registrationAllowed && url.registrationUrl && (
                                    <Grid size={12}>
                                        <Button
                                            href={url.registrationUrl}
                                            fullWidth
                                            variant="outlined"
                                            sx={{
                                                padding: "12px 0",
                                                borderRadius: "10px",
                                                fontSize: "15px",
                                                fontWeight: 600,
                                                textTransform: "none",
                                                borderColor: "#a2988e",
                                                color: "#a2988e",
                                                borderWidth: "1.5px",
                                                "&:hover": {
                                                    borderWidth: "1.5px",
                                                    borderColor: "#8a7f75",
                                                    backgroundColor: "rgba(162, 152, 142, 0.04)",
                                                    transform: "translateY(-1px)"
                                                }
                                            }}
                                        >
                                            Login with Laminam
                                        </Button>
                                    </Grid>
                                )}
                            </Grid>
                        </form>
                    </CardContent>
                </Card>
            </Box>
        </Template>
    );
};

export default Login;
