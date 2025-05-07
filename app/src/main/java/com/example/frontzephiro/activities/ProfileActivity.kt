package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.api.ProfilingApiService
import com.example.frontzephiro.api.UserApiService
import com.example.frontzephiro.models.MailDTO
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val callAnimation = findViewById<LottieAnimationView>(R.id.call)
        val alertAnimation = findViewById<LottieAnimationView>(R.id.alert)
        callAnimation.repeatCount = 0
        callAnimation.playAnimation()

        alertAnimation.repeatCount = 0
        alertAnimation.playAnimation()

        callAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyContactsActivity::class.java)
            startActivity(intent)
        }

        alertAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyNumbersActivity::class.java)
            startActivity(intent)
        }

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val userName = sharedPreferences.getString("USER_NAME", "Usuario")
        val namePersona = findViewById<TextView>(R.id.name)
        namePersona.text = "$userName"

        val userEmail = sharedPreferences.getString("email", "Correo")
        val emailPersona = findViewById<TextView>(R.id.correoProfile)
        emailPersona.text = "$userEmail"

        setupCardInteractions()
        setupBottomNavigation()

        val exit = findViewById<ImageView>(R.id.logout)
        exit.setOnClickListener {
            logout()
        }

    }

    private fun setupCardInteractions() {
        val card1 = findViewById<androidx.cardview.widget.CardView>(R.id.cardContactos)
        val card2 = findViewById<androidx.cardview.widget.CardView>(R.id.cardMetricas)
        val card3 = findViewById<androidx.cardview.widget.CardView>(R.id.cardEliminar)

        card1.setOnClickListener {
            val intent = Intent(this, EmergencyContactsActivity::class.java)
            startActivity(intent)
        }

        card2.setOnClickListener {
            startActivity(Intent(this, GraficaActivity::class.java))
        }

        card3.setOnClickListener {
            showDeleteAccountDialog()
        }

        //.setOnClickListener {
            //lottieCard4.playAnimation()
            //val intent = Intent(this, EmergencyNumbersActivity::class.java)
            //startActivity(intent)
        //}

        //lottieExit.setOnClickListener {
            //startActivity(Intent(this, LoginActivity::class.java))
            //finish()
        //}
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menuPerfil

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuInicio -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }

                R.id.menuSeguimiento -> {
                    startActivity(Intent(this, TrackerMain::class.java))
                    true
                }

                R.id.menuJardin -> {
                    startActivity(Intent(this, GardenMain::class.java))
                    true
                }

                R.id.menuContenido -> {
                    startActivity(Intent(this, ContentActivity::class.java))
                    true
                }
                /*
                R.id.menuPerfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                } */
                else -> false
            }
        }
    }

    private fun showDeleteAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_email, null)
        val editTextEmail = dialogView.findViewById<TextInputEditText>(R.id.editTextEmail)

        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Cuenta")
            .setMessage("Para confirmar la eliminación, ingresa tu correo:")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { dialog, _ ->
                val enteredEmail = editTextEmail.text.toString().trim()
                val storedEmail = sharedPreferences.getString("email", "")?.trim() ?: ""

                Log.d("DELETE_ACCOUNT", "Correo ingresado: '$enteredEmail'")
                Log.d("DELETE_ACCOUNT", "Correo almacenado: '$storedEmail'")

                if (enteredEmail.equals(storedEmail, ignoreCase = true)) {
                    val userId = sharedPreferences.getString("USER_ID", "") ?: ""
                    deleteAccount(userId, enteredEmail)
                } else {
                    Toast.makeText(this, "El correo ingresado no coincide", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAccount(userId: String, email: String) {
        val userService = RetrofitClient
            .getClient()
            .create(UserApiService::class.java)

        val profilingService = RetrofitClient
            .getProfileClient()     // apunta a http://10.0.2.2:5032/
            .create(ProfilingApiService::class.java)

        userService.deleteAccount(userId, MailDTO(email))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                    if (resp.isSuccessful) {
                        profilingService.deleteProfile(userId, "Mongo")
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                                    if (resp.isSuccessful) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Cuenta y perfil eliminados",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Cuenta eliminada pero error borrando perfil: ${resp.code()}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    goToLogin()
                                }
                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error de red al borrar perfil",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    goToLogin()
                                }
                            })
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Error al eliminar la cuenta: ${resp.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "Error de conexión al eliminar cuenta",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun goToLogin() {
        sharedPreferences.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }


    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove("USER_ID")
        editor.remove("USER_NAME")
        editor.remove("TOKEN")
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
