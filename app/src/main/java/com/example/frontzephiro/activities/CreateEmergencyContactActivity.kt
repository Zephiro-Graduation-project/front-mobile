package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.api.AlertsApiService
import com.example.frontzephiro.models.ContactRequest
import com.example.frontzephiro.models.ContactResponse
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateEmergencyContactActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText    // aquí recuperaremos R.id.emailInput
    private lateinit var etCellphone: TextInputEditText   // R.id.telefonoInput
    private lateinit var etEmail: TextInputEditText       // R.id.correoInput
    private lateinit var btnSave: Button                  // R.id.botonIniciarSesion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_emergency_contact)
        setupBottomNavigation()

        // Animaciones y botón “volver”
        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener { startActivity(Intent(this@CreateEmergencyContactActivity, EmergencyContactsActivity::class.java)) }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener { startActivity(Intent(this@CreateEmergencyContactActivity, EmergencyNumbersActivity::class.java)) }
        }
        findViewById<LinearLayout>(R.id.backContainer).setOnClickListener { onBackPressed() }

        // Enlazamos vistas según tu XML
        etFullName  = findViewById(R.id.emailInput)
        etCellphone = findViewById(R.id.telefonoInput)
        etEmail     = findViewById(R.id.correoInput)
        btnSave     = findViewById(R.id.botonIniciarSesion)

        btnSave.setOnClickListener {
            // 1) Obtener userId desde SharedPreferences
            val userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("USER_ID", "")
                .orEmpty()
            if (userId.isBlank()) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2) Leer y validar campos
            val fullName  = etFullName.text.toString().trim()
            val email     = etEmail.text.toString().trim()
            val phoneText = etCellphone.text.toString().trim()
            val cellphone = phoneText.toLongOrNull()

            if (fullName.isEmpty() || email.isEmpty() || cellphone == null) {
                Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3) Crear body del request
            val request = ContactRequest(
                userId    = userId,
                fullName  = fullName,
                email     = email,
                cellphone = cellphone
            )

            // 4) Llamada al API autenticado
            RetrofitClient
                .getAuthenticatedAlertsClient(this)
                .create(AlertsApiService::class.java)
                .createContact(request)
                .enqueue(object : Callback<ContactResponse> {
                    override fun onResponse(
                        call: Call<ContactResponse>,
                        response: Response<ContactResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CreateEmergencyContactActivity,
                                "Contacto creado con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@CreateEmergencyContactActivity,
                                "Error ${response.code()} al crear contacto",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<ContactResponse>, t: Throwable) {
                        Toast.makeText(this@CreateEmergencyContactActivity,
                            "Fallo de red: ${t.localizedMessage}",
                            Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.menuPerfil
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuInicio     -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.menuSeguimiento-> startActivity(Intent(this, TrackerMain::class.java))
                R.id.menuJardin     -> startActivity(Intent(this, GadActivity::class.java))
                R.id.menuContenido  -> startActivity(Intent(this, ContentActivity::class.java))
                R.id.menuPerfil     -> startActivity(Intent(this, ProfileActivity::class.java))
                else                -> return@setOnItemSelectedListener false
            }
            true
        }
    }
}
