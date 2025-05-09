package com.example.frontzephiro.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.example.frontzephiro.api.GardenApiService
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
        val apiService = RetrofitClient.getClient()
            .create(UserApiService::class.java)

        val encryptedPassword = EncryptionUtils.encryptAES(password, "1234567890123456")
        val user = UserEntity(name, email, encryptedPassword, birthdate)

        apiService.register(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    showFailure()
                    return
                }

                val loginReq = LoginRequest(mail = email, password = encryptedPassword)
                RetrofitClient.getClient()
                    .create(UserApiService::class.java)
                    .login(loginReq)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, resp: Response<LoginResponse>) {
                            val lr = resp.body()
                            if (!resp.isSuccessful || lr == null) {
                                showFailure()
                                return
                            }

                            saveUserData(lr.token, lr.name, lr.id, email)

                            RetrofitClient
                                .getAuthenticatedGamificationClient(this@RegisterActivity)
                                .create(InventoryApiService::class.java)
                                .addInventory(lr.id)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, r: Response<Void>) {
                                        if (r.isSuccessful) {
                                            createGarden(lr.id)
                                        } else {
                                            showFailure()
                                            goHabits()
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        showFailure()
                                        goHabits()
                                    }
                                })
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            showFailure()
                        }
                    })
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showFailure()
            }
        })
    }

    private fun createGarden(userId: String) {
        RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GardenApiService::class.java)
            .addGarden(userId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, r2: Response<Void>) {
                    if (r2.isSuccessful) {
                        showSuccess()
                    } else {
                        showFailure()
                    }
                    goHabits()
                }

                override fun onFailure(call: Call<Void>, t2: Throwable) {
                    showFailure()
                    goHabits()
                }
            })
    }

    private fun saveUserData(token: String, name: String, id: String, email: String) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().apply {
            putString("TOKEN", token)
            putString("USER_NAME", name)
            putString("USER_ID", id)
            putString("email", email)
            // Guarda la fecha de registro:
            putString("REGISTRATION_DATE", today)
            apply()
        }
    }

    private fun goHabits() {
        startActivity(Intent(this, HabitsActivity::class.java))
        finish()
    }

    // Solo estos dos Toast:
    private fun showSuccess() {
        Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
    }
    private fun showFailure() {
        Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_LONG).show()
    }
}
