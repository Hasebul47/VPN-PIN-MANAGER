package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.auth.FirebaseAuthRepository
import com.example.ui.theme.PolishErrorContainer
import com.example.ui.theme.PolishOnErrorContainer
import com.example.ui.theme.PolishOnSuccessContainer
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSuccessContainer
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
    authRepository: FirebaseAuthRepository,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordMode by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("auth_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PolishPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showForgotPasswordMode) {
                            Icons.Default.Lock
                        } else if (selectedTab == 0) {
                            Icons.Default.AccountCircle
                        } else {
                            Icons.Default.PersonAdd
                        },
                        contentDescription = null,
                        tint = PolishPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (showForgotPasswordMode) {
                        "পাসওয়ার্ড রিসেট"
                    } else if (selectedTab == 0) {
                        "অ্যাকাউন্টে লগইন করুন"
                    } else {
                        "নতুন অ্যাকাউন্ট খুলুন"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (showForgotPasswordMode) {
                        "আপনার নিবন্ধিত ইমেইল দিন, পাসওয়ার্ড রিসেট লিঙ্ক পাঠানো হবে"
                    } else if (selectedTab == 0) {
                        "আপনার পিন এবং সাবস্ক্রিপশন সিঙ্ক করতে লগইন করুন"
                    } else {
                        "সহজেই সমস্ত ডিভাইস থেকে ডেটা সুরক্ষিত রাখতে নিবন্ধন করুন"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PolishTextSecondary,
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // If not in forgot password mode, show tabs
                if (!showForgotPasswordMode) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = PolishSurfaceVariant,
                        contentColor = PolishPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                                successMessage = null
                            },
                            text = {
                                Text(
                                    text = "লগইন (Sign In)",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                                successMessage = null
                            },
                            text = {
                                Text(
                                    text = "নিবন্ধন (Register)",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Error Banner
                errorMessage?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = PolishErrorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = PolishOnErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = PolishOnErrorContainer,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Success Banner
                successMessage?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = PolishSuccessContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PolishOnSuccessContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = PolishOnSuccessContainer,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Name field (Registration only)
                if (selectedTab == 1 && !showForgotPasswordMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("পূর্ণ নাম (Full Name)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishOutline
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("ইমেইল (Email Address)") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = PolishPrimary)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = if (showForgotPasswordMode) ImeAction.Done else ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishOutline
                    )
                )

                // Password fields (when not in forgot password mode)
                if (!showForgotPasswordMode) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("পাসওয়ার্ড (Password)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PolishPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    tint = PolishTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishOutline
                        )
                    )

                    // Confirm Password (Registration only)
                    if (selectedTab == 1) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            label = { Text("পাসওয়ার্ড নিশ্চিত করুন (Confirm Password)") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = PolishPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                        tint = PolishTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PolishPrimary,
                                unfocusedBorderColor = PolishOutline
                            )
                        )
                    }

                    // Forgot Password link (Login tab only)
                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showForgotPasswordMode = true
                                    errorMessage = null
                                    successMessage = null
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "পাসওয়ার্ড ভুলে গেছেন?",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PolishPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (showForgotPasswordMode) {
                            if (email.isBlank()) {
                                errorMessage = "অনুগ্রহ করে আপনার ইমেইল অ্যাড্রেস লিখুন।"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authRepository.sendPasswordReset(email)
                                isLoading = false
                                result.fold(
                                    onSuccess = {
                                        successMessage = "পাসওয়ার্ড রিসেট লিঙ্ক ইমেইলে পাঠানো হয়েছে। আপনার ইনবক্স চেক করুন।"
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.message ?: "ব্যর্থ হয়েছে।"
                                    }
                                )
                            }
                        } else if (selectedTab == 0) {
                            // Login
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "ইমেইল এবং পাসওয়ার্ড উভয়ই আবশ্যক।"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authRepository.signInWithEmail(email, password)
                                isLoading = false
                                result.fold(
                                    onSuccess = {
                                        onAuthSuccess()
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.message ?: "লগইন ব্যর্থ হয়েছে।"
                                    }
                                )
                            }
                        } else {
                            // Registration
                            if (name.isBlank()) {
                                errorMessage = "অনুগ্রহ করে আপনার পূর্ণ নাম লিখুন।"
                                return@Button
                            }
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "ইমেইল এবং পাসওয়ার্ড পূরণ করুন।"
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে (কমপক্ষে ৬ অক্ষর)।"
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorMessage = "উভয় পাসওয়ার্ড এক হতে হবে।"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authRepository.signUpWithEmail(name, email, password)
                                isLoading = false
                                result.fold(
                                    onSuccess = {
                                        onAuthSuccess()
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.message ?: "নিবন্ধন ব্যর্থ হয়েছে।"
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (showForgotPasswordMode) {
                                "রিসেট লিঙ্ক পাঠান"
                            } else if (selectedTab == 0) {
                                "লগইন করুন"
                            } else {
                                "নিবন্ধন সম্পন্ন করুন"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (showForgotPasswordMode) {
                    TextButton(
                        onClick = {
                            showForgotPasswordMode = false
                            errorMessage = null
                            successMessage = null
                        }
                    ) {
                        Text(
                            text = "লগইন পেজে ফিরে যান",
                            color = PolishPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "অতিথি হিসেবে চালিয়ে যান (Guest Mode)",
                            color = PolishTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
