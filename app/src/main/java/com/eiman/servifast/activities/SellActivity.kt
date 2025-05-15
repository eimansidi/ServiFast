package com.eiman.servifast.activities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.CategoryResponse
import com.eiman.servifast.api.models.CreateServiceRequest
import com.eiman.servifast.api.models.GenericResponse
import com.eiman.servifast.utils.LocaleHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class SellActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var btnClose: ImageButton
    private lateinit var imgPlaceholder: ImageView
    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputLocation: EditText
    private lateinit var btnCategory: Button
    private lateinit var inputPrice: EditText
    private lateinit var checkboxNeg: CheckBox
    private lateinit var btnContinue: Button

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var imageBase64: String? = null
    private var hasImage = false
    private val categoryList = mutableListOf<CategoryResponse>()
    private var selectedCategoryIndex = -1

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val lang = shared.getString("language", "es") ?: "es"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sell)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        btnClose        = findViewById(R.id.btnClose)
        imgPlaceholder  = findViewById(R.id.imgPlaceholder)
        inputTitle      = findViewById(R.id.inputTitle)
        inputDescription= findViewById(R.id.inputDescription)
        inputLocation   = findViewById(R.id.inputLocation)
        btnCategory     = findViewById(R.id.btnCategory)
        inputPrice      = findViewById(R.id.inputPrice)
        checkboxNeg     = findViewById(R.id.checkboxNegotiable)
        btnContinue     = findViewById(R.id.btnContinue)

        imgPlaceholder.alpha = 0.7f
        imgPlaceholder.setBackgroundResource(0)

        btnClose.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Salir")
                .setMessage("¿Deseas salir sin guardar?")
                .setPositiveButton("Sí") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult
            contentResolver.openInputStream(uri)?.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                imgPlaceholder.setImageBitmap(bmp)
                imgPlaceholder.alpha = 1.0f
                hasImage = true
                val baos = ByteArrayOutputStream().apply { bmp.compress(Bitmap.CompressFormat.JPEG, 80, this) }
                imageBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP)
            }
        }
        imgPlaceholder.setOnClickListener {
            if (hasImage) showImageOptions() else pickImageLauncher.launch("image/*")
        }

        btnCategory.setOnClickListener { showCategorySheet() }

        loadCategories()

        btnContinue.setOnClickListener {
            val title = inputTitle.text.toString().trim()
            val desc  = inputDescription.text.toString().trim()
            val loc   = inputLocation.text.toString().trim()
            val price = inputPrice.text.toString().toDoubleOrNull() ?: 0.0
            val neg   = checkboxNeg.isChecked
            if (title.isEmpty() || desc.isEmpty() || loc.isEmpty() || price <= 0.0 || selectedCategoryIndex < 0) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = prefs.getString("user_identifier", "") ?: ""
            val catId = categoryList[selectedCategoryIndex].id
            val req = CreateServiceRequest(
                user        = userId,
                titulo      = title,
                descripcion = desc,
                imagen      = imageBase64,
                precio      = price,
                negociable  = neg,
                ubicacion   = loc,
                idCategoria = catId
            )
            RetrofitClient.instance.createService(req)
                .enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            AlertDialog.Builder(this@SellActivity)
                                .setTitle("¡Enhorabuena!")
                                .setMessage("Tu servicio ha sido publicado exitosamente.")
                                .setPositiveButton("OK") { _, _ -> finish() }
                                .show()
                        } else {
                            Toast.makeText(this@SellActivity, "Error al añadir el servicio.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        Toast.makeText(this@SellActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    private fun showImageOptions() {
        AlertDialog.Builder(this)
            .setTitle("Imagen")
            .setItems(arrayOf("Cambiar imagen", "Eliminar imagen")) { _, which ->
                when (which) {
                    0 -> pickImageLauncher.launch("image/*")
                    1 -> {
                        imgPlaceholder.setImageResource(R.drawable.img_placeholder)
                        imgPlaceholder.alpha = 0.7f
                        hasImage = false
                        imageBase64 = null
                    }
                }
            }
            .show()
    }

    private fun showCategorySheet() {
        val sheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.widget_categories, null)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategories)
        rv.layoutManager = LinearLayoutManager(this)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rv.adapter = CategoryAdapter(categoryList) { index ->
            selectedCategoryIndex = index
            // get resource id by name
            val key = categoryList[index].nombre.lowercase()
            val resId = resources.getIdentifier(key, "string", packageName)
            btnCategory.text = if (resId != 0) getString(resId) else "Para ${categoryList[index].nombre}"
            sheet.dismiss()
        }
        sheet.setContentView(view)
        sheet.show()
    }

    private fun loadCategories() {
        RetrofitClient.instance.getCategories()
            .enqueue(object : Callback<List<CategoryResponse>> {
                override fun onResponse(call: Call<List<CategoryResponse>>, response: Response<List<CategoryResponse>>) {
                    val list = response.body().orEmpty()
                    categoryList.clear()
                    categoryList.addAll(list)
                }
                override fun onFailure(call: Call<List<CategoryResponse>>, t: Throwable) {
                    Toast.makeText(this@SellActivity, "Error al cargar categorías.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    inner class CategoryAdapter(
        private val items: List<CategoryResponse>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.VH>() {
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val txt: TextView = view.findViewById(R.id.tvCategory)
            init {
                view.setOnClickListener { onClick(adapterPosition) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val key = items[position].nombre.lowercase()
            val resId = resources.getIdentifier(key, "string", packageName)
            holder.txt.text = if (resId != 0) getString(resId) else "Para ${items[position].nombre}"
        }

        override fun getItemCount() = items.size
    }
}
