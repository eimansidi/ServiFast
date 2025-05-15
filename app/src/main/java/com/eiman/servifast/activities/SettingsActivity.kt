package com.eiman.servifast.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.GenericResponse
import com.eiman.servifast.api.models.UpdateUserRequest
import com.eiman.servifast.utils.LocaleHelper
import com.eiman.servifast.utils.ValidationUtils.isValidEmail
import com.eiman.servifast.utils.ValidationUtils.isValidPhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPhone: EditText
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var rbParticular: RadioButton
    private lateinit var rbProfessional: RadioButton
    private lateinit var btnChangeLanguage: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnChangePassword: Button

    private lateinit var prefs: SharedPreferences
    private var originalEmail = ""
    private var originalPhone = ""
    private var originalUserType = "particular"
    private var originalLanguage = "es"
    private var currentLanguage = "es"

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val lang = sharedPrefs.getString("language", "es") ?: "es"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        inputEmail = findViewById(R.id.inputEmail)
        inputPhone = findViewById(R.id.inputPhone)
        userTypeGroup = findViewById(R.id.userTypeGroup)
        rbParticular = findViewById(R.id.rbParticular)
        rbProfessional = findViewById(R.id.rbProfessional)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        originalEmail = prefs.getString("user_email", "") ?: ""
        originalPhone = prefs.getString("user_phone", "") ?: ""
        originalUserType = prefs.getString("user_type", "particular") ?: "particular"
        originalLanguage = prefs.getString("language", "es") ?: "es"
        currentLanguage = originalLanguage

        inputEmail.setText(originalEmail)
        inputPhone.setText(originalPhone)
        if (originalUserType == "profesional") rbProfessional.isChecked = true else rbParticular.isChecked = true

        btnChangeLanguage.text = if (currentLanguage == "es") "Español" else "Inglés"
        btnChangeLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "es") "en" else "es"
            btnChangeLanguage.text = if (currentLanguage == "es") "Español" else "Inglés"
        }

        btnSave.setOnClickListener {
            if (!hasChanges()) {
                Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newEmail = inputEmail.text.toString().trim()
            val newPhone = inputPhone.text.toString().trim()
            if (newEmail.isNotEmpty() && !isValidEmail(newEmail)) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPhone.isNotEmpty() && !isValidPhone(newPhone)) {
                Toast.makeText(this, "Teléfono debe tener 9 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Confirmar cambios")
                .setMessage("¿Quieres guardar los cambios?")
                .setPositiveButton("Sí") { _, _ -> saveChanges() }
                .setNegativeButton("No", null)
                .show()
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
        btnBack.setOnClickListener { handleBack() }
    }

    override fun onBackPressed() { handleBack() }

    private fun hasChanges(): Boolean {
        val emailChanged = inputEmail.text.toString() != originalEmail
        val phoneChanged = inputPhone.text.toString() != originalPhone
        val userTypeChanged = (rbParticular.isChecked && originalUserType != "particular") ||
                (rbProfessional.isChecked && originalUserType != "profesional")
        val languageChanged = currentLanguage != originalLanguage
        return emailChanged || phoneChanged || userTypeChanged || languageChanged
    }

    private fun saveChanges() {
        val userId = prefs.getString("user_identifier", null) ?: return
        val updatedEmail = if (inputEmail.text.toString() != originalEmail) inputEmail.text.toString() else null
        val updatedPhone = if (inputPhone.text.toString() != originalPhone) inputPhone.text.toString() else null
        val updatedUserType = when {
            rbParticular.isChecked && originalUserType != "particular" -> "particular"
            rbProfessional.isChecked && originalUserType != "profesional" -> "profesional"
            else -> null
        }
        val languageChanged = currentLanguage != originalLanguage
        val request = UpdateUserRequest(
            email = updatedEmail,
            telefono = updatedPhone,
            tipo_usuario = updatedUserType,
            user = userId
        )
        RetrofitClient.instance.updateUser(request)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        prefs.edit().apply {
                            updatedEmail?.let { putString("user_email", it) }
                            updatedPhone?.let { putString("user_phone", it) }
                            updatedUserType?.let { putString("user_type", it) }
                            if (languageChanged) putString("language", currentLanguage)
                            apply()
                        }
                        if (languageChanged) {
                            LocaleHelper.setLocale(this@SettingsActivity, currentLanguage)
                        }
                        Toast.makeText(this@SettingsActivity, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@SettingsActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun handleBack() {
        if (hasChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Cambios sin guardar")
                .setMessage("Tienes cambios sin guardar. ¿Deseas salir sin guardar?")
                .setPositiveButton("Sí") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        } else {
            finish()
        }
    }
}
