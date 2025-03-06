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
import android.util.Base64
import com.example.frontzephiro.utils.EncryptionUtils
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        binding.animationLogin.setAnimation(R.raw.flower)
        binding.animationLogin.playAnimation()
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        binding.botonIniciarSesion.setOnClickListener {

            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Log.e("LoginActivity", "Campos vacíos")
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
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
        // Define una clave secreta compartida (asegúrate de que sea la misma en el back-end)
        val secretKey = "1234567890123456"  // Ejemplo: 16 caracteres para AES-128

        // Encriptar la contraseña
        val encryptedPassword = EncryptionUtils.encryptAES(password, secretKey)

        // Crear la solicitud con la contraseña encriptada
        val loginRequest = LoginRequest(mail = email, password = encryptedPassword)
        val call = apiService.login(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    //esto es para guardar en el cache el token, nombre y id
                    saveUserData(loginResponse.token, loginResponse.name, loginResponse.id)
                    Toast.makeText(applicationContext, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Error de conexión. Revisa tu internet.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveUserData(token: String, name: String, id: Long) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN", token)
        editor.putString("USER_NAME", name)
        editor.putLong("USER_ID", id)
        editor.apply()
    }

    //para  poder recuperar la data en caualquier parte de la app usar
    /*
    val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", null)
    val userName = sharedPreferences.getString("USER_NAME", null)
    val userId = sharedPreferences.getLong("USER_ID", -1L)
     */
}
