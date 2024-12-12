package com.example.assignment.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Sealed class to represent authentication results. */
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/** AuthViewModel to manage Google and Phone sign-in logic. */
class AuthViewModel(private val context: Context) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val tag = "AuthViewModel"

    var phoneNumber by mutableStateOf("")
    private var verificationId: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    /**
     * Logs out the user.
     */
    fun logout() {
        auth.signOut()
        Log.d(tag, "User logged out.")
    }

    /**
     * Sign in with Google credentials.
     */
    suspend fun signInWithGoogle(navController: NavHostController): AuthResult = withContext(Dispatchers.IO) {
        try {
            val webClientId = getGoogleClientId(context) ?: return@withContext AuthResult.Error("Failed to retrieve Web Client ID.")

            // Build a Google Sign-In option
            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            // Use CredentialManager for Google Sign-In
            val credentialRequest = androidx.credentials.GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = androidx.credentials.CredentialManager.create(context)
            val response = credentialManager.getCredential(context as Activity, credentialRequest)

            // Check the credential response
            val googleIdToken = if (response.credential is CustomCredential) {
                (response.credential.data ?: return@withContext AuthResult.Error("No ID Token received."))
            } else {
                return@withContext AuthResult.Error("Invalid credential type.")
            }

            // Use Firebase to sign in with the Google token
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken.toString(), null)
            auth.signInWithCredential(firebaseCredential).await()

            Log.d(tag, "Google sign-in successful.")
            withContext(Dispatchers.Main) {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            }
            return@withContext AuthResult.Success
        } catch (e: Exception) {
            Log.e(tag, "Error during Google sign-in", e)
            return@withContext AuthResult.Error(e.localizedMessage ?: "An unknown error occurred.")
        }
    }

    /**
     * Retrieve the Google Web Client ID from `google-services.json`.
     */
    private fun getGoogleClientId(context: Context): String? {
        return try {
            // Open the `google-services.json` file
            val inputStream = context.assets.open("google-services.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            // Access the `client` array
            val clientArray = jsonObject.getJSONArray("client")

            // Loop through the `client` array to find the Web Client ID
            for (i in 0 until clientArray.length()) {
                val oauthClients = clientArray.getJSONObject(i).getJSONArray("oauth_client")

                // Loop through `oauth_client` to find `client_type: 3`
                for (j in 0 until oauthClients.length()) {
                    val oauthClient = oauthClients.getJSONObject(j)
                    if (oauthClient.getInt("client_type") == 3) { // Web Client Type
                        return oauthClient.getString("client_id") // Return the Web Client ID
                    }
                }
            }

            Log.e("AuthViewModel", "No Web Client ID found in google-services.json")
            null
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error parsing google-services.json", e)
            null
        }
    }

    /**
     * Send OTP to the user's phone number, ensuring proper formatting for Firebase.
     */
    suspend fun sendPhoneOTP(inputPhoneNumber: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            // Ensure the phone number has the `+91` prefix for India.
            val formattedPhoneNumber = formatPhoneNumber(inputPhoneNumber)
                ?: return@withContext AuthResult.Error("Invalid phone number. Please use +91 or 10-digit number.")

            verificationId = null // Reset verification ID for a fresh OTP.
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhoneNumber) // Use the formatted phone number.
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration.
                .setActivity(context as? Activity ?: return@withContext AuthResult.Error("Context must be an Activity"))
                .setCallbacks(phoneAuthCallbacks()) // Set callbacks for verification.
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options) // Send the OTP.
            Log.d(tag, "OTP sent successfully to $formattedPhoneNumber.")
            return@withContext AuthResult.Success
        } catch (e: Exception) {
            Log.e(tag, "Failed to send OTP", e)
            return@withContext AuthResult.Error(e.localizedMessage ?: "Error sending OTP.")
        }
    }

    /**
     * Helper function to format the phone number with country code.
     */
    private fun formatPhoneNumber(inputPhoneNumber: String): String? {
        val sanitizedNumber = inputPhoneNumber.replace("\\s".toRegex(), "") // Remove spaces.
        return when {
            sanitizedNumber.startsWith("+91") && sanitizedNumber.length == 13 -> sanitizedNumber
            sanitizedNumber.length == 10 && sanitizedNumber.all { it.isDigit() } -> "+91$sanitizedNumber"
            else -> null // Invalid number.
        }
    }

    /**
     * Verify the OTP entered by user.
     */
    suspend fun verifyOTP(enteredOtp: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val id = verificationId ?: return@withContext AuthResult.Error("No verification ID found.")

            val credential = PhoneAuthProvider.getCredential(id, enteredOtp)
            auth.signInWithCredential(credential).await()

            Log.d(tag, "OTP successfully verified.")
            verificationId = null // Clear verification ID after successful verification.
            return@withContext AuthResult.Success
        } catch (e: Exception) {
            Log.e(tag, "Failed to verify OTP", e)
            return@withContext AuthResult.Error(e.localizedMessage ?: "Error verifying OTP.")
        }
    }

    /**
     * Handle PhoneAuthProvider callbacks.
     */
    private fun phoneAuthCallbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "Phone verification completed successfully.")
                    } else {
                        Log.e(tag, "Failed to auto-verify phone number.", task.exception)
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(tag, "Phone number verification failed.", e)
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                resendingToken = token // Save the resending token for future use.
                Log.d(tag, "OTP sent to the phone number. Verification ID saved.")
            }
        }
    }
}
