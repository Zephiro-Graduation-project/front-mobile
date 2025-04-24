// RegisterActivity.kt
package com.example.frontzephiro.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.example.frontzephiro.api.UserApiService
import com.example.frontzephiro.api.InventoryApiService
import com.example.frontzephiro.api.GardenApiService
import com.example.frontzephiro.databinding.ActivityRegisterBinding
import com.example.frontzephiro.models.UserEntity
import com.example.frontzephiro.models.LoginRequest
import com.example.frontzephiro.models.LoginResponse
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
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthdate = dateFormat.parse(birthdateText)!!
            registerUser(name, email, password, birthdate)
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, y, m, d ->
                calendar.set(y, m, d)
                val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.birthdateInput.setText(df.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun registerUser(name: String, email: String, password: String, birthdate: Date) {
        val apiService = RetrofitClient.getClient().create(UserApiService::class.java)
        val secretKey = "1234567890123456"
        val encryptedPassword = EncryptionUtils.encryptAES(password, secretKey)
        val user = UserEntity(name, email, encryptedPassword, birthdate)

        apiService.register(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "No se pudo registrar. Verifica los datos.", Toast.LENGTH_LONG).show()
                    return
                }
                Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()

                val loginService = RetrofitClient.getClient().create(UserApiService::class.java)
                val loginReq = LoginRequest(mail = email, password = encryptedPassword)
                loginService.login(loginReq).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, resp: Response<LoginResponse>) {
                        if (!resp.isSuccessful || resp.body() == null) {
                            Toast.makeText(this@RegisterActivity, "Fallo en login automático", Toast.LENGTH_LONG).show()
                            return
                        }
                        val lr = resp.body()!!
                        saveUserData(lr.token, lr.name, lr.id, email)

                        val invService = RetrofitClient
                            .getAuthenticatedArtifactClient(this@RegisterActivity)
                            .create(InventoryApiService::class.java)

                        invService.addInventory(lr.id).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, r: Response<Void>) {
                                if (r.isSuccessful) {
                                    val gardenService = RetrofitClient
                                        .getAuthenticatedArtifactClient(this@RegisterActivity)
                                        .create(GardenApiService::class.java)
                                    gardenService.addGarden(lr.id).enqueue(object : Callback<Void> {
                                        override fun onResponse(c2: Call<Void>, r2: Response<Void>) {
                                            goDemographics()
                                        }
                                        override fun onFailure(c2: Call<Void>, t2: Throwable) {
                                            goDemographics()
                                        }
                                    })
                                } else {
                                    goDemographics()
                                }
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                goDemographics()
                            }
                        })
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@RegisterActivity, "Fallo en login automático", Toast.LENGTH_LONG).show()
                    }
                })
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Error de conexión. Intenta más tarde.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveUserData(token: String, name: String, id: String, email: String) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("TOKEN", token)
            putString("USER_NAME", name)
            putString("USER_ID", id)
            putString("email", email)
            apply()
        }
    }

    private fun goDemographics() {
        startActivity(Intent(this, DemographicsActivity::class.java))
        finish()
    }
}
