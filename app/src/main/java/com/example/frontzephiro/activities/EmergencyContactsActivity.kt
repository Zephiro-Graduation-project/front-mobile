package com.example.frontzephiro.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.ContactsAdapter
import com.example.frontzephiro.api.AlertsApiService
import com.example.frontzephiro.models.ContactResponse
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmergencyContactsActivity : AppCompatActivity() {

    private lateinit var rvContacts: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        // 1) RecyclerView + Adapter con click y delete
        rvContacts = findViewById(R.id.rvContacts)
        contactsAdapter = ContactsAdapter(
            items = emptyList(),
            onItemClick = { contact ->
                openDialer(contact.cellphone.toString())
            },
            onEditClick = { /* si lo llegas a necesitar */ },
            onDeleteClick = { contact ->
                showDeleteConfirmation(contact)
            }
        )
        rvContacts.apply {
            layoutManager = LinearLayoutManager(this@EmergencyContactsActivity)
            adapter = contactsAdapter
        }

        // 2) FloatingActionButton para crear
        findViewById<FloatingActionButton>(R.id.floatingAddContact)
            .setOnClickListener {
                startActivity(Intent(this, CreateEmergencyContactActivity::class.java))
            }

        // 3) Animaciones Lottie
        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@EmergencyContactsActivity, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@EmergencyContactsActivity, EmergencyNumbersActivity::class.java))
            }
        }

        // 4) Bottom navigation
        setupBottomNavigation()

        // 5) Cargar lista
        loadContacts()
    }

    override fun onResume() {
        super.onResume()
        loadContacts()
    }

    private fun showDeleteConfirmation(contact: ContactResponse) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar contacto")
            .setMessage("¿Seguro que deseas eliminar a ${contact.fullName}?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()
                deleteContact(contact)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun loadContacts() {
        val userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            .getString("USER_ID", "")
            .orEmpty()

        if (userId.isBlank()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient
            .getAuthenticatedAlertsClient(this)
            .create(AlertsApiService::class.java)
            .getContacts(userId)
            .enqueue(object : Callback<List<ContactResponse>> {
                override fun onResponse(
                    call: Call<List<ContactResponse>>,
                    response: Response<List<ContactResponse>>
                ) {
                    if (response.isSuccessful) {
                        contactsAdapter.updateItems(response.body().orEmpty())
                    } else {
                        Toast.makeText(
                            this@EmergencyContactsActivity,
                            "Error ${response.code()} al cargar contactos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                override fun onFailure(call: Call<List<ContactResponse>>, t: Throwable) {
                    Toast.makeText(
                        this@EmergencyContactsActivity,
                        "Fallo de red: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun deleteContact(contact: ContactResponse) {
        val userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            .getString("USER_ID", "")
            .orEmpty()
        if (userId.isBlank()) return

        RetrofitClient
            .getAuthenticatedAlertsClient(this)
            .create(AlertsApiService::class.java)
            .deleteContact(userId, contact.id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EmergencyContactsActivity, "Contacto eliminado", Toast.LENGTH_SHORT).show()
                        contactsAdapter.removeItem(contact.id)
                    }
                    else {
                        Toast.makeText(this@EmergencyContactsActivity, "Error ${response.code()} al eliminar", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@EmergencyContactsActivity,
                        "Fallo de red: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun openDialer(phoneNumber: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(dialIntent)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.menuPerfil
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuInicio     -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.menuSeguimiento-> startActivity(Intent(this, TrackerMain::class.java))
                R.id.menuJardin     -> startActivity(Intent(this, GardenMain::class.java))
                R.id.menuContenido  -> startActivity(Intent(this, ContentActivity::class.java))
                else                -> return@setOnItemSelectedListener false
            }
            true
        }
    }
}
