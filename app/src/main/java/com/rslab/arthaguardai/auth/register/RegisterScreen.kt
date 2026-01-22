package com.rslab.arthaguardai.auth.register

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rslab.arthaguardai.ui.theme.ArthaGuardAITheme
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var countryCode by remember { mutableStateOf("+91") }
    var phone by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf<String?>(null) }


    // Registration success → Login
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(
                context,
                "Registration successful. Please login.",
                Toast.LENGTH_LONG
            ).show()

            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
            viewModel.resetState()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC9C5C5))

    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Color.Black),
//            contentAlignment = Alignment.Center
        ){
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ArthaGuard AI",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 50.dp),
                )
                Text(
                    text = "Sing Up",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 45.dp)
                )

            }

        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = 180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DarkTextField("Full Name", fullName) { fullName = it }
                Spacer(Modifier.height(8.dp))

                DarkTextField("Username", userName) { userName = it }
                Spacer(Modifier.height(8.dp))

                DarkTextField("Email", email) { email = it }
                Spacer(Modifier.height(8.dp))

                DarkPasswordField(
                    label = "Password",
                    value = password,
                    visible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible },
                    onValueChange = { password = it }
                )

                Spacer(Modifier.height(8.dp))

                DarkPasswordField(
                    label = "Confirm Password",
                    value = confirmPassword,
                    visible = confirmPasswordVisible,
                    onToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                    onValueChange = { confirmPassword = it }
                )

                Spacer(Modifier.height(8.dp))

                PhoneRowDark(
                    countryCode = countryCode,
                    phone = phone,
                    onCountryChange = { countryCode = it },
                    onPhoneChange = { phone = it }
                )

                AnimatedErrorText(formError ?: uiState.errorMessage)

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        formError = validateForm(
                            fullName,
                            userName,
                            email,
                            password,
                            confirmPassword,
                            phone
                        )
                        if (formError != null) return@Button

                        viewModel.register(
                            fullName = fullName,
                            userName = userName,
                            email = email,
                            password = password,
                            phone = "$countryCode$phone",
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White,
                            strokeWidth = 2.dp)
                    } else {
                        Text("Sign Up", color = Color.White)
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Already have an account? Login",
                    color = Color.Gray,
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }
        }

    }
}


fun validateForm(
    fullName: String,
    userName: String,
    email: String,
    password: String,
    confirmPassword: String,
    phone: String
): String? {
    if (fullName.isBlank()) return "Full name is required"
    if (userName.isBlank()) return "Username is required"
    if (!email.contains("@")) return "Enter a valid email"
    if (password.length < 8) return "Password must be at least 8 characters"
    if (!password.any { it.isUpperCase() }) return "Add one uppercase letter"
    if (!password.any { it.isLowerCase() }) return "Add one lowercase letter"
    if (!password.any { it.isDigit() }) return "Add one number"
    if (!password.any { !it.isLetterOrDigit() }) return "Add one special character"
    if (password != confirmPassword) return "Passwords do not match"
    if (phone.length < 10) return "Enter a valid phone number"
    return null
}


@Composable
fun DarkPasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (visible)
                        Icons.Default.VisibilityOff
                    else
                        Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
fun PhoneRowDark(
    countryCode: String,
    phone: String,
    onCountryChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Row {
        OutlinedTextField(
            value = countryCode,
            onValueChange = onCountryChange,
            modifier = Modifier.width(90.dp),
            label = { Text("Code") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.width(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier.weight(1f),
            label = { Text("Phone") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
    }
}


@Composable
fun DarkTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
fun whiteFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.White,
    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

@Composable
fun AnimatedErrorText(error: String?) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = error ?: "",
            color = Color.Red,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview
@Composable
private fun RegisterScreenPreview() {
    ArthaGuardAITheme {
        RegisterScreen(navController = NavController(LocalContext.current))
    }

}
