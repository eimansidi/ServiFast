package com.eiman.servifast.activities

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.adapters.RatingsAdapter
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.items.RatingItem
import com.eiman.servifast.api.items.UserItem
import com.eiman.servifast.api.models.GenericResponse
import com.eiman.servifast.api.models.UpdateUserRequest
import com.eiman.servifast.api.models.UserProfileRequest
import com.eiman.servifast.api.models.UserProfileResponse
import com.eiman.servifast.api.models.UserRatingListRequest
import com.eiman.servifast.api.models.UserRatingsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var avatarView: ImageView
    private lateinit var nameView: TextView
    private lateinit var ratingContainer: LinearLayout
    private lateinit var totalRatingsView: TextView
    private lateinit var rvRatings: RecyclerView
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var userIdentifier: String = ""
    private var isOwnProfile: Boolean = false
    private var hasAvatar: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        userIdentifier = intent.getStringExtra("user_identifier")
            ?: prefs.getString("user_identifier", "")!!
        isOwnProfile = userIdentifier == prefs.getString("user_identifier", "")

        avatarView       = findViewById(R.id.userAvatar)
        nameView         = findViewById(R.id.tvUsername)
        ratingContainer  = findViewById(R.id.userRatingContainer)
        totalRatingsView = findViewById(R.id.tvRatings)
        rvRatings        = findViewById(R.id.rvRatings)

        rvRatings.layoutManager = LinearLayoutManager(this)
        rvRatings.setHasFixedSize(true)

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { handleImageUri(it) }
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        loadProfile()
        loadRatingsList()
    }

    private fun showAvatarOptions() {
        val options = mutableListOf("Cambiar avatar")
        if (hasAvatar) options.add("Eliminar avatar")
        AlertDialog.Builder(this)
            .setTitle("Avatar")
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Cambiar avatar" -> pickImageLauncher.launch("image/*")
                    "Eliminar avatar" -> updateAvatarOnServer(null)
                }
            }
            .show()
    }

    private fun handleImageUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val byteStream = java.io.ByteArrayOutputStream().apply {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, this)
                }
                val bytes = byteStream.toByteArray()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                updateAvatarOnServer(base64)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error procesando imagen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateAvatarOnServer(base64: String?) {
        val req = UpdateUserRequest(
            user = userIdentifier,
            avatar = base64
        )
        RetrofitClient.instance.updateUser(req)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true) {
                            hasAvatar = !base64.isNullOrEmpty()
                            if (base64 != null) prefs.edit().putString("user_avatar", base64).apply()
                            else prefs.edit().remove("user_avatar").apply()
                            Toast.makeText(this@ProfileActivity, "Avatar actualizado", Toast.LENGTH_SHORT).show()
                            loadProfile()
                        } else {
                            Toast.makeText(this@ProfileActivity, "Error servidor: ${body?.error ?: "Desconocido"}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@ProfileActivity, "Error HTTP: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun loadProfile() {
        if (isOwnProfile) {
            loadOwnProfile()
        } else {
            loadExternalProfile()
        }
    }

    private fun loadOwnProfile() {
        val req = UserProfileRequest(user = userIdentifier)
        RetrofitClient.instance.getUserProfile(req)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Error HTTP: ${response.code()}", Toast.LENGTH_LONG).show()
                        return
                    }
                    val body = response.body()
                    if (body?.success == true) {
                        hasAvatar = !body.avatar.isNullOrEmpty()
                        if (hasAvatar) {
                            val bytes = Base64.decode(body.avatar, Base64.NO_WRAP)
                            avatarView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                        } else {
                            avatarView.setImageResource(R.drawable.generic_avatar)
                        }
                        avatarView.setOnClickListener { showAvatarOptions() }
                        nameView.text = "${body.nombre.capitalize()} ${body.apellidos.capitalize()}"
                        renderStars(body.ratingPromedio)
                        totalRatingsView.text = body.totalValoraciones.toString()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Error servidor: ${body ?: "Desconocido"}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun loadExternalProfile() {
        RetrofitClient.instance.getUsers()
            .enqueue(object : Callback<List<UserItem>> {
                override fun onResponse(call: Call<List<UserItem>>, response: Response<List<UserItem>>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Error HTTP: ${response.code()}", Toast.LENGTH_LONG).show()
                        return
                    }
                    val list = response.body().orEmpty()
                    val targetId = userIdentifier.toIntOrNull()
                    val user = list.find { it.id == targetId }
                    if (user != null) {
                        hasAvatar = !user.avatarBase64.isNullOrEmpty()
                        if (hasAvatar) {
                            val bytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                            avatarView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                        } else {
                            avatarView.setImageResource(R.drawable.generic_avatar)
                        }
                        nameView.text = user.nombre.capitalize()
                        renderStars(user.rating)
                        totalRatingsView.text = user.totalValoraciones.toString()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Usuario no encontrado", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<List<UserItem>>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun loadRatingsList() {
        val req = UserRatingListRequest(user = userIdentifier)
        val call = if (isOwnProfile) {
            RetrofitClient.instance.getUserRatings(req)
        } else {
            RetrofitClient.instance.getUsersRatings(req)
        }

        call.enqueue(object : Callback<UserRatingsResponse> {
            override fun onResponse(call: Call<UserRatingsResponse>, response: Response<UserRatingsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val list: List<RatingItem> = response.body()!!.ratings
                    rvRatings.adapter = RatingsAdapter(list)
                    rvRatings.adapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ProfileActivity, "Ratings no disponibles", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<UserRatingsResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Error red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun renderStars(rating: Float) {
        val avg = rating.roundToInt()
        val stars = listOf(
            findViewById<ImageView>(R.id.star1),
            findViewById<ImageView>(R.id.star2),
            findViewById<ImageView>(R.id.star3),
            findViewById<ImageView>(R.id.star4),
            findViewById<ImageView>(R.id.star5)
        )
        for ((i, star) in stars.withIndex()) {
            val colorRes = if (i < avg) R.color.star_active else R.color.star_inactive
            star.setColorFilter(ContextCompat.getColor(this, colorRes), android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }
}
