package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.example.frontzephiro.api.UserApiService
import com.example.frontzephiro.databinding.ActivityLoginBinding
import com.example.frontzephiro.models.LoginRequest
import com.example.frontzephiro.models.LoginResponse
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // Inicializa la animación si la usas
        binding.animationLogin.setAnimation(R.raw.flower)
        binding.animationLogin.playAnimation()
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        binding.botonIniciarSesion.setOnClickListener {
            Log.d("LoginActivity", "🟢 Botón de login presionado")
            Toast.makeText(this, "🟢 Botón presionado", Toast.LENGTH_SHORT).show()

            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Log.d("LoginActivity", "🟢 Iniciando sesión con: $email")
                loginUser(email, password)
            } else {
                Log.e("LoginActivity", "❌ Campos vacíos")
                Toast.makeText(this, "❌ Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Cambio a pantalla de registro
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
            if (checkedId == R.id.btnRegister) {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val apiService = RetrofitClient.getClient().create(UserApiService::class.java)
        // Se utiliza LoginRequest en lugar de UserEntity
        val loginRequest = LoginRequest(mail = email, password = password)
        val call = apiService.login(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    saveToken(token)
                    Toast.makeText(applicationContext, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    // Redirige a HomeActivity
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Error de conexión. Revisa tu internet.", Toast.LENGTH_LONG).show()
                Log.e("LoginActivity", "Error de conexión: ${t.localizedMessage}")
            }
        })
    }

    private fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN", token)
        editor.apply()
    }
}
