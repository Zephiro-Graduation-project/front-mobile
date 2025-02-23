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
import com.example.frontzephiro.models.LoginResponse
import com.example.frontzephiro.models.UserEntity
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

        // Animación de login
        binding.animationLogin.setAnimation(R.raw.flower)
        binding.animationLogin.playAnimation()
        setContentView(binding.root)

        // Inicializar SharedPreferences para guardar el token
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Manejar clic en el botón de login
        binding.btnLogin.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Cambiar a pantalla de registro
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
        val call = apiService.login(UserEntity(null, email, password, null))

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    saveToken(token)
                    Toast.makeText(applicationContext, "Login exitoso", Toast.LENGTH_SHORT).show()

                    // Ir a la pantalla principal después del login
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Error en las credenciales", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Error de conexión", t)
                Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN", token)
        editor.apply()
    }
}
