package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.api.AlertsApiService
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.models.ContactRequest
import com.example.frontzephiro.models.ContactResponse
import com.example.frontzephiro.models.ContactUpdateRequest
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateEmergencyContactActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etCellphone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var gamificationService: GamificationApiService

    private var isEdit = false
    private var contactId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_emergency_contact)

        // 1) Vincula vistas
        etFullName  = findViewById(R.id.emailInput)      // ojo: tu XML usa este id para nombre
        etCellphone = findViewById(R.id.telefonoInput)
        etEmail     = findViewById(R.id.correoInput)
        btnSave     = findViewById(R.id.botonIniciarSesion)

        // 2) Animaciones y navegación
        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@CreateEmergencyContactActivity, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@CreateEmergencyContactActivity, EmergencyNumbersActivity::class.java))
            }
        }
        findViewById<LinearLayout>(R.id.backContainer).setOnClickListener { onBackPressed() }
        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            selectedItemId = R.id.menuPerfil
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuInicio     -> startActivity(Intent(this@CreateEmergencyContactActivity, HomeActivity::class.java))
                    R.id.menuSeguimiento-> startActivity(Intent(this@CreateEmergencyContactActivity, TrackerMain::class.java))
                    R.id.menuJardin     -> startActivity(Intent(this@CreateEmergencyContactActivity, GadActivity::class.java))
                    R.id.menuContenido  -> startActivity(Intent(this@CreateEmergencyContactActivity, ContentActivity::class.java))
                }
                true
            }
        }

        // 3) Detecta modo edición
        contactId = intent.getStringExtra("CONTACT_ID")
        if (!contactId.isNullOrBlank()) {
            isEdit = true
            etFullName.setText(intent.getStringExtra("FULL_NAME"))
            etEmail.setText(intent.getStringExtra("EMAIL"))
            etCellphone.setText(intent.getLongExtra("CELLPHONE", 0L).toString())
            btnSave.text = "Actualizar contacto"
        }

        // 4) Listener del botón
        btnSave.setOnClickListener { onSaveClicked() }
    }

    private fun onSaveClicked() {
        val userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            .getString("USER_ID", "").orEmpty()
        if (userId.isBlank()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName  = etFullName.text.toString().trim()
        val email     = etEmail.text.toString().trim()
        val phoneStr  = etCellphone.text.toString().trim()
        val cellphone = phoneStr.toLongOrNull()
        if (fullName.isEmpty() || email.isEmpty() || cellphone == null) {
            Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEdit && contactId != null) {
            val updateReq = ContactUpdateRequest(
                contactId = contactId!!,
                userId    = userId,
                fullName  = fullName,
                email     = email,
                cellphone = cellphone
            )

            RetrofitClient
                .getAuthenticatedAlertsClient(this)
                .create(AlertsApiService::class.java)
                .updateContact(updateReq)
                .enqueue(object : Callback<ContactResponse> {
                    override fun onResponse(
                        call: Call<ContactResponse>,
                        response: Response<ContactResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@CreateEmergencyContactActivity,
                                "Contacto actualizado",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@CreateEmergencyContactActivity,
                                "Error ${response.code()} al actualizar",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    override fun onFailure(call: Call<ContactResponse>, t: Throwable) {
                        Toast.makeText(
                            this@CreateEmergencyContactActivity,
                            "Fallo de red: ${t.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        } else {
            val req = ContactRequest(userId, fullName, email, cellphone)
            RetrofitClient
                .getAuthenticatedAlertsClient(this)
                .create(AlertsApiService::class.java)
                .createContact(req)
                .enqueue(object : Callback<ContactResponse> {
                    override fun onResponse(call: Call<ContactResponse>, resp: Response<ContactResponse>) {
                        if (resp.isSuccessful) {
                            rewardUserForNewContact()
                            Toast.makeText(this@CreateEmergencyContactActivity, "Contacto creado", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@CreateEmergencyContactActivity, "Error ${resp.code()} al crear", Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<ContactResponse>, t: Throwable) {
                        Toast.makeText(this@CreateEmergencyContactActivity, "Fallo de red: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
    private fun rewardUserForNewContact() {
        gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        val prefs  = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""

        gamificationService.rewardEmergencyContact(userId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                    if (rewardResp.isSuccessful) {
                        Log.e("CreateEmergencyContactActivity", "Recompensa por nuevo contacto aplicada")
                        Toast.makeText(this@CreateEmergencyContactActivity,"Recompensa por nuevo contacto aplicada",Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("CreateEmergencyContactActivity", "Error recompensa nuevo contacto: ${rewardResp.code()}")
                        Toast.makeText(this@CreateEmergencyContactActivity,"Error recompensa nuevo contacto: ${rewardResp.code()}",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("CreateEmergencyContactActivity", "Fallo recompensa nuevo contacto: ${t.message}")
                    //Toast.makeText(this@CreateEmergencyContactActivity,"Fallo recompensa nuevo contacto: ${t.message}",Toast.LENGTH_LONG).show()
                }
            })
    }
}
