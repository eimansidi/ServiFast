package com.eiman.servifast.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^\\d{9}$"))
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{8,}$")
        return regex.matches(password)
    }
}
