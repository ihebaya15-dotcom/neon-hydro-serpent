package com.example.firebase

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    val isSignedIn: Boolean
        get() = _currentUser.value != null

    suspend fun signInWithGoogle(activityContext: Context): FirebaseUser? {
        _authError.value = null
        try {
            val serverClientId = context.getString(R.string.default_web_client_id)
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                _currentUser.value = authResult.user
                return authResult.user
            } else {
                _authError.value = "نوع الاعتماد غير مدعوم"
                return null
            }
        } catch (e: GetCredentialCancellationException) {
            // User cancelled the prompt, no error to show
            return null
        } catch (e: GetCredentialException) {
            _authError.value = "تعذر تسجيل الدخول: ${e.localizedMessage ?: "خطأ في الاتصال"}"
            return null
        } catch (e: Exception) {
            _authError.value = "فشل التحقق من الحساب: ${e.localizedMessage ?: "حدث خطأ"}"
            return null
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun clearError() {
        _authError.value = null
    }
}
