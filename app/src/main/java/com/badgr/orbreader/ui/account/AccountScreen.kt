package com.badgr.orbreader.ui.account

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(vm: AccountViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val isPro   by vm.isPro.collectAsState()
    val activity = LocalContext.current as Activity

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Account",
                        color      = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {

                is AccountUiState.SignedIn -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Signed in as",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            state.email,
                            color      = MaterialTheme.colorScheme.onBackground,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        // ── Pro status badge ──────────────────────────────────
                        if (isPro) {
                            AssistChip(
                                onClick    = {},
                                label      = { Text("BADGR Pro") },
                                leadingIcon = {
                                    Icon(
                                        imageVector        = Icons.Default.Star,
                                        contentDescription = "Pro",
                                        modifier           = Modifier.size(16.dp)
                                    )
                                }
                            )
                        } else {
                            Text(
                                "Upgrade to Pro to unlock all features.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )

                            // ── Purchase buttons ──────────────────────────────
                            Button(
                                onClick  = { vm.launchSubscription(activity) },
                                modifier = Modifier.fillMaxWidth(),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Subscribe — Monthly", color = MaterialTheme.colorScheme.onPrimary)
                            }

                            OutlinedButton(
                                onClick  = { vm.launchLifetime(activity) },
                                modifier = Modifier.fillMaxWidth(),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Lifetime Access — One-time")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { vm.signOut() },
                            colors  = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Sign Out")
                        }
                    }
                }

                is AccountUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                is AccountUiState.SignedOut,
                is AccountUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            if (isSignUp) "Create Account" else "Sign In",
                            color      = MaterialTheme.colorScheme.onBackground,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        if (state is AccountUiState.Error) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text     = state.message,
                                    color    = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp),
                                    style    = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        OutlinedTextField(
                            value           = email,
                            onValueChange   = { email = it },
                            label           = { Text("Email") },
                            singleLine      = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier        = Modifier.fillMaxWidth(),
                            colors          = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor          = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value                = password,
                            onValueChange        = { password = it },
                            label                = { Text("Password") },
                            singleLine           = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier             = Modifier.fillMaxWidth(),
                            colors               = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor          = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick  = {
                                if (isSignUp) vm.signUp(email, password)
                                else          vm.signIn(email, password)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                if (isSignUp) "Create Account" else "Sign In",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        TextButton(
                            onClick = {
                                isSignUp = !isSignUp
                                vm.clearError()
                            }
                        ) {
                            Text(
                                if (isSignUp) "Already have an account? Sign In"
                                else          "No account? Create one",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
