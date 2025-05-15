package com.eiman.servifast.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.GenericResponse
import com.eiman.servifast.api.models.RegisterRequest
import com.eiman.servifast.utils.LocaleHelper
import com.eiman.servifast.utils.ValidationUtils.isValidEmail
import com.eiman.servifast.utils.ValidationUtils.isValidPassword
import com.eiman.servifast.utils.ValidationUtils.isValidPhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SigninActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val lang   = shared.getString("language", "es") ?: "es"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        findViewById<ImageButton>(R.id.btnEspanol).setOnClickListener {
            prefs.edit().putString("language", "es").apply()
            LocaleHelper.setLocale(this, "es")
            recreate()
        }
        findViewById<ImageButton>(R.id.btnIngles).setOnClickListener {
            prefs.edit().putString("language", "en").apply()
            LocaleHelper.setLocale(this, "en")
            recreate()
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val name     = findViewById<EditText>(R.id.inputName).text.toString().trim()
            val lastname = findViewById<EditText>(R.id.inputLastname).text.toString().trim()
            val email    = findViewById<EditText>(R.id.inputEmail).text.toString().trim()
            val phone    = findViewById<EditText>(R.id.inputPhone).text.toString().trim()
            val password = findViewById<EditText>(R.id.inputPassword).text.toString().trim()

            val rbParticular = findViewById<RadioButton>(R.id.rbParticular)
            val tipoUsuario  = if (rbParticular.isChecked) "particular" else "profesional"

            if (name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidPhone(phone)) {
                Toast.makeText(this, "Teléfono debe tener 9 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidPassword(password)) {
                Toast.makeText(
                    this,
                    "Contraseña débil: mínimo 8 caracteres, un número y un símbolo",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                nombre       = name,
                apellidos    = lastname,
                email        = email,
                telefono     = phone,
                password     = password,
                tipo_usuario = tipoUsuario
            )

            RetrofitClient.instance.register(request)
                .enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@SigninActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@SigninActivity, "Error al registrar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        Toast.makeText(this@SigninActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
