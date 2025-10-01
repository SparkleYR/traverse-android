package com.example.kotlin_traverse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kotlin_traverse.viewmodel.AuthMode
import com.example.kotlin_traverse.viewmodel.AuthState

@Composable
fun AuthScreen(
    state: AuthState,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (username: String, email: String, password: String, githubUsername: String?, githubRepo: String?, githubBranch: String?) -> Unit,
    onToggleMode: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var githubUsername by rememberSaveable { mutableStateOf("") }
    var githubRepo by rememberSaveable { mutableStateOf("") }
    var githubBranch by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.mode) {
        password = ""
        if (state.mode == AuthMode.Login) {
            username = ""
            githubUsername = ""
            githubRepo = ""
            githubBranch = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (state.mode == AuthMode.Login) "Sign in" else "Create your account",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (state.mode == AuthMode.Register) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        if (state.mode == AuthMode.Register) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = githubUsername,
                onValueChange = { githubUsername = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("GitHub username (optional)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = githubRepo,
                onValueChange = { githubRepo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("GitHub repo (optional)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = githubBranch,
                onValueChange = { githubBranch = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("GitHub branch (optional)") },
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (state.mode == AuthMode.Login) {
                    onLogin(email.trim(), password)
                } else {
                    onRegister(
                        username.trim(),
                        email.trim(),
                        password,
                        githubUsername.trim().ifBlank { null },
                        githubRepo.trim().ifBlank { null },
                        githubBranch.trim().ifBlank { null }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank() && (state.mode == AuthMode.Login || username.isNotBlank())
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.mode == AuthMode.Login) "Sign in" else "Register")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onToggleMode) {
            Text(
                if (state.mode == AuthMode.Login) "Need an account? Register" else "Already have an account? Sign in"
            )
        }
        state.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
