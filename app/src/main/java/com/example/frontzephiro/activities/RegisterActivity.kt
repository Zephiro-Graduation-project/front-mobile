package com.example.frontzephiro.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.example.frontzephiro.api.InventoryApiService
import com.example.frontzephiro.api.UserApiService
import com.example.frontzephiro.databinding.ActivityRegisterBinding
import com.example.frontzephiro.models.LoginRequest
import com.example.frontzephiro.models.LoginResponse
import com.example.frontzephiro.models.UserEntity
import com.example.frontzephiro.network.RetrofitClient
import com.example.frontzephiro.utils.EncryptionUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val calendar = Calendar.getInstance()
    private val secretKey = "1234567890123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.animationRegister.setAnimation(R.raw.flower)
        binding.animationRegister.playAnimation()

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
            if (checkedId == R.id.btnLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }

        binding.birthdateInput.setOnClickListener { showDatePicker() }
        binding.birthdatePicker.setOnClickListener { showDatePicker() }

        binding.botonIniciarSesion.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val birthdateText = binding.birthdateInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || birthdateText.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val birthdate = fmt.parse(birthdateText)!!
                registerUser(name, email, password, birthdate)
            } catch (e: Exception) {
                Toast.makeText(this, "Formato de fecha incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, y, m, d ->
                calendar.set(y, m, d)
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.birthdateInput.setText(fmt.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun registerUser(name: String, email: String, password: String, birthdate: Date) {
        val apiUser = RetrofitClient.getClient().create(UserApiService::class.java)
        val encrypted = EncryptionUtils.encryptAES(password, secretKey)
        val user = UserEntity(name, email, encrypted, birthdate)

        apiUser.register(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    autoLoginAndSetup(email, password)
                } else {
                    Toast.makeText(this@RegisterActivity,
                        "No se pudo registrar. Verifica los datos.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@RegisterActivity,
                    "Error de conexión. Intenta más tarde.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun autoLoginAndSetup(email: String, plainPassword: String) {
        val apiLogin = RetrofitClient.getClient().create(UserApiService::class.java)
        val encrypted = EncryptionUtils.encryptAES(plainPassword, secretKey)
        val loginReq = LoginRequest(mail = email, password = encrypted)

        apiLogin.login(loginReq).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, resp: Response<LoginResponse>) {
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                    prefs.putString("TOKEN", body.token)
                    prefs.putString("USER_NAME", body.name)
                    prefs.putString("USER_ID", body.id)
                    prefs.putString("email", email)
                    prefs.apply()

                    val invApi = RetrofitClient
                        .getAuthenticatedArtifactClient(this@RegisterActivity)
                        .create(InventoryApiService::class.java)
                    invApi.addInventory(body.id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, r: Response<Void>) {
                            startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                            finish()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                            finish()
                        }
                    })
                } else {
                    Toast.makeText(this@RegisterActivity,
                        "Registro OK, pero no pudimos iniciar sesión.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity,
                    "Usuario registrado, pero fallo auto‑login: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
