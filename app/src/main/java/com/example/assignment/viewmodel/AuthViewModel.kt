package com.example.assignment.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** * Sealed class for representing Auth Results. */
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/** * AuthViewModel to manage sign-in/authentication logic. */
class AuthViewModel(
    private val context: Context
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val tag = "AuthViewModel"
    private val credentialManager: CredentialManager = CredentialManager.create(context)
    var phoneNumber by mutableStateOf("")
    var verificationId: String? = null

    fun logout() {
        Log.d("AuthViewModel", "User logged out.")
        auth.signOut() // Implement Firebase sign-out logic here if needed.
    }

    /** * Sign-In with Google using CredentialManager. */
    suspend fun signInWithGoogle(navController: NavHostController): AuthResult = withContext(Dispatchers.IO) {
        try {
            // Fetch the web client ID from google-services.json directly here.
            val webClientId = getGoogleClientId(context) ?: return@withContext AuthResult.Error("Failed to retrieve client ID.")

            // Create a GetGoogleIdOption for the credential request.
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId) // Use web client ID from google services JSON here.
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()

            // Build the credential request.
            val getCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Perform the credential request using CredentialManager (this requires proper setup).
            val credentialResponse = credentialManager.getCredential(context as Activity, getCredentialRequest)

            // Extract the credential if valid.
            if (credentialResponse.credential is CustomCredential && credentialResponse.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)
                val idToken = googleIdTokenCredential.idToken ?: return@withContext AuthResult.Error("No ID token found.")

                // Use the token to sign in with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).await()

                Log.d(tag, "Google Sign-In successful.")
                return@withContext AuthResult.Success
            } else {
                return@withContext AuthResult.Error("Invalid credential type.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error during Google Sign-In", e)
            return@withContext AuthResult.Error(e.localizedMessage ?: "Unknown error occurred.")
        }
    }

    private fun getGoogleClientId(context: Context): String? {
        return try {
            // Open and read the google-services.json file
            val inputStream = context.assets.open("google-services.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            // Access the client array
            val clientArray = jsonObject.getJSONArray("client")

            // Loop through client array to find the correct client ID
            for (i in 0 until clientArray.length()) {
                val oauthClientArray = clientArray.getJSONObject(i).getJSONArray("oauth_client")

                // Look for the OAuth client with type 3 (web client)
                for (j in 0 until oauthClientArray.length()) {
                    val oauthClient = oauthClientArray.getJSONObject(j)
                    if (oauthClient.getInt("client_type") == 3) { // Check for web client type
                        return oauthClient.getString("client_id") // Return the client ID
                    }
                }
            }

            Log.e("AuthViewModel", "No valid OAuth clients found.")
            null
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to read google-services.json", e)
            null
        }
    }

    /** * Send OTP to the user's phone number. */
    suspend fun sendPhoneOTP(phoneNumber: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            verificationId = null // Reset verification ID before sending a new OTP.

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber) // Phone number to verify.
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration.
                .setActivity(context as? Activity ?: return@withContext AuthResult.Error("Context must be an Activity"))
                .setCallbacks(phoneAuthCallbacks()) // Set callbacks for verification.
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options) // Send the OTP.

            Log.d(tag, "OTP sent successfully.")
            return@withContext AuthResult.Success
        } catch (e: Exception) {
            Log.e(tag, "Failed to send OTP", e)
            return@withContext AuthResult.Error(e.localizedMessage ?: "Error sending OTP")
        }
    }

    /** * Verify the OTP entered by user. */
    suspend fun verifyOTP(enteredOtp: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val id = verificationId ?: return@withContext AuthResult.Error("No verification ID found")

            // Create credential using the verification ID and entered OTP.
            val credential = PhoneAuthProvider.getCredential(id, enteredOtp)

            // Sign in with the credential.
            auth.signInWithCredential(credential).await()

            Log.d(tag, "OTP successfully verified.")
            verificationId = null // Reset verification ID after successful verification.
            return@withContext AuthResult.Success
        } catch (e: Exception) {
            Log.e(tag, "Failed to verify OTP", e)
            return@withContext AuthResult.Error(e.localizedMessage ?: "Error verifying OTP.")
        }
    }

    /** * Handle PhoneAuthProvider callbacks. */
    private fun phoneAuthCallbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Automatically sign in the user with the received credential.
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "Phone verification completed successfully.")
                    } else {
                        Log.e(tag, "Failed to auto-verify phone number.", task.exception)
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(tag, "Phone number verification failed", e)

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        Log.e(tag, "Invalid phone number format.")
                    }
                    is FirebaseTooManyRequestsException -> {
                        Log.e(tag, "Quota exceeded. Please try again later.")
                    }
                    else -> {
                        Log.e(tag, "Unknown error occurred during verification.")
                    }
                }
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id // Save the verification ID for later use.
                Log.d(tag, "OTP sent to the phone number.")
            }
        }
    }
}