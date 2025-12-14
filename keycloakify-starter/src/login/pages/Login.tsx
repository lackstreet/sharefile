import { PageProps } from "../types.ts";
import { Button, Card, CardContent, Grid, TextField, Typography, Link, Box, Divider, Avatar } from "@mui/material";
import { CloudUpload } from "@mui/icons-material";

const Login = (props: PageProps<"login.ftl">) => {
    const { kcContext, i18n, Template } = props;
    const { realm, url, messagesPerField } = kcContext;

    return (
        <Template kcContext={kcContext} i18n={i18n}>
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
                                    background: "linear-gradient(135deg, #003b5c 38%, #003b5c 0%)",
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
                                background: "linear-gradient(135deg, #003b5c 0%, #6d6560 100%)",
                                WebkitBackgroundClip: "text",
                                WebkitTextFillColor: "transparent",
                                backgroundClip: "text"
                            }}
                        >
                            Lamshare{" "}
                            <Typography
                                variant="body2"
                                textAlign="center"
                                sx={{
                                    background: "linear-gradient(135deg, #a2988e 100%, #fff 0%)",
                                    WebkitBackgroundClip: "text",
                                    backgroundClip: "text",
                                    fontSize: "13px",
                                    marginTop: "4px"
                                }}
                            >
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
                                                        borderColor: "#003b5c"
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
                                                    borderColor: "#003b5c"
                                                },
                                                "&.Mui-focused": {
                                                    backgroundColor: "#fff",
                                                    "& fieldset": {
                                                        borderColor: "#003b5c"
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
                                            fontWeight: 550,
                                            textTransform: "none",
                                            background: "linear-gradient(135deg, #003b5c 90%, rgba(162, 152, 142, 0.10) 20%)",
                                            boxShadow: "0 4px 16px rgba(162, 152, 142, 0.35)",
                                            transition: "all 0.3s ease",
                                            "&:hover": {
                                                background: "linear-gradient(135deg, #003b5c 90.2%, #fff 10%)",
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
                                                borderColor: "#003b5c",
                                                color: "#003b5c",
                                                borderWidth: "1.5px",
                                                background: "#fefefe",
                                                "&:hover": {
                                                    borderWidth: "1.5px",
                                                    borderColor: "#003b5c",
                                                    backgroundColor: "#fff",
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
