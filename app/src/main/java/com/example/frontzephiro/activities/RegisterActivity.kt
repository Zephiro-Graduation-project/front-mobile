package com.example.frontzephiro.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.example.frontzephiro.api.UserApiService
import com.example.frontzephiro.databinding.ActivityRegisterBinding
import com.example.frontzephiro.models.UserEntity
import com.example.frontzephiro.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animación de registro
        binding.animationRegister.setAnimation(R.raw.flower)
        binding.animationRegister.playAnimation()

        // Botón para ir al Login
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
            if (checkedId == R.id.btnLogin) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }

        // Mostrar DatePicker al tocar el campo de fecha
        binding.birthdateInput.setOnClickListener { showDatePicker() }
        binding.birthdatePicker.setOnClickListener { showDatePicker() }

        // Manejo del botón de registro
        binding.botonIniciarSesion.setOnClickListener {
            Log.d("RegisterActivity", "🟢 Botón de registro presionado")
            Toast.makeText(this, "🟢 Botón presionado", Toast.LENGTH_SHORT).show()

            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val birthdateText = binding.birthdateInput.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && birthdateText.isNotEmpty()) {
                try {
                    // Convertir la fecha de String a Date
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val birthdate: Date = dateFormat.parse(birthdateText)!!

                    Log.d("RegisterActivity", "🟢 Registrando usuario: $name, $email, ${dateFormat.format(birthdate)}")
                    registerUser(name, email, password, birthdate)
                } catch (e: Exception) {
                    Log.e("RegisterActivity", "❌ Formato de fecha incorrecto", e)
                    Toast.makeText(this, "❌ Formato de fecha incorrecto", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("RegisterActivity", "❌ Campos vacíos")
                Toast.makeText(this, "❌ Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.birthdateInput.setText(dateFormat.format(calendar.time)) // Seteamos la fecha en el input
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun registerUser(name: String, email: String, password: String, birthdate: Date) {
        val apiService = RetrofitClient.getClient().create(UserApiService::class.java)
        val user = UserEntity(name, email, password, birthdate) // ✅ Birthdate sigue siendo Date

        // 🔥 Log para depuración
        val gson = Gson()
        val jsonBody = gson.toJson(user)
        Log.d("RegisterActivity", "📡 Enviando JSON: $jsonBody")

        apiService.register(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()?.string()
                Log.d("RegisterActivity", "📡 Respuesta del servidor: $responseBody")

                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "✅ Registro exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("RegisterActivity", "❌ Error en el registro: Código ${response.code()}, Respuesta: $responseBody")

                    // 🟢 Cambio en los Toasts: Mensaje más claro
                    Toast.makeText(applicationContext, "❌ No se pudo registrar. Verifica los datos.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("RegisterActivity", "❌ Error de conexión en registro", t)

                // 🟢 Cambio en los Toasts: Mensaje más claro
                Toast.makeText(applicationContext, "❌ Error de conexión. Intenta de nuevo más tarde.", Toast.LENGTH_LONG).show()
            }
        })
    }
}
