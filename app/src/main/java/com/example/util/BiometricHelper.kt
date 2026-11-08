package com.example.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun isBiometricAvailable(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            // Check BIOMETRIC_STRONG or BIOMETRIC_WEAK
            val res = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            res == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "تأیید هویت",
        subtitle: String = "برای دسترسی، اثر انگشت خود را تأیید کنید",
        negativeButtonText: String = "انصراف",
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        if (!isBiometricAvailable(activity)) {
            // If biometric hardware or enrolled fingerprint is not available, proceed safely
            onSuccess()
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setConfirmationRequired(false)
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Error code 13 = ERROR_NEGATIVE_BUTTON, 10 = ERROR_USER_CANCELED
                        onError?.invoke(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            android.util.Log.w("BiometricHelper", "Biometric authentication failed to start: ${e.message}", e)
            onError?.invoke(e.localizedMessage ?: "خطا در احراز هویت بیومتریک")
        }
    }
}
