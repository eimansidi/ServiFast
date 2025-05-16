package com.eiman.servifast.ui

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.CategoryResponse
import com.eiman.servifast.api.models.FilterRequest
import com.eiman.servifast.adapters.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilterDialog(
    context: Context,
    private val onApply: (FilterRequest) -> Unit
) : Dialog(context) {

    private val inputMin: EditText
    private val inputMax: EditText
    private val inputLoc: EditText
    private val btnParticular: Button
    private val btnProfesional: Button
    private val btnCategory: Button
    private val btnClose: ImageButton
    private val btnApply: Button

    private val categoryList = mutableListOf<CategoryResponse>()
    private var selectedCategoryIndex: Int? = null

    init {
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_filter, null))
        setCanceledOnTouchOutside(false)

        inputMin       = findViewById(R.id.inputMinPrice)
        inputMax       = findViewById(R.id.InputMaxPrice)
        inputLoc       = findViewById(R.id.filterInputLocation)
        btnParticular  = findViewById(R.id.filterParticular)
        btnProfesional = findViewById(R.id.filterProfesional)
        btnCategory    = findViewById(R.id.filterButtonCategory)
        btnClose       = findViewById(R.id.filterClose)
        btnApply       = findViewById(R.id.btnFilterApply)

        // Load categories
        RetrofitClient.instance.getCategories()
            .enqueue(object: Callback<List<CategoryResponse>> {
                override fun onResponse(call: Call<List<CategoryResponse>>, resp: Response<List<CategoryResponse>>) {
                    categoryList.clear()
                    categoryList.addAll(resp.body().orEmpty())
                }
                override fun onFailure(call: Call<List<CategoryResponse>>, t: Throwable) {
                    Toast.makeText(context, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
                }
            })

        btnClose.setOnClickListener { dismiss() }

        btnParticular.setOnClickListener {
            btnParticular.isSelected = true
            btnProfesional.isSelected = false
        }
        btnProfesional.setOnClickListener {
            btnProfesional.isSelected = true
            btnParticular.isSelected = false
        }

        btnCategory.setOnClickListener { showCategorySheet() }

        btnApply.setOnClickListener {
            val min = inputMin.text.toString().toDoubleOrNull()
            val max = inputMax.text.toString().toDoubleOrNull()
            val loc = inputLoc.text.toString().takeIf { it.isNotBlank() }
            val userType = when {
                btnParticular.isSelected  -> "particular"
                btnProfesional.isSelected -> "profesional"
                else                      -> null
            }
            val catId = selectedCategoryIndex?.let { categoryList[it].id }

            onApply(FilterRequest(min, max, loc, userType, catId))
            dismiss()
        }
    }

    private fun showCategorySheet() {
        val sheet = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.widget_categories, null)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategories)
        rv.layoutManager = LinearLayoutManager(context)
        rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        rv.adapter = CategoryAdapter(categoryList) { index ->
            selectedCategoryIndex = index
            btnCategory.text = "Para ${categoryList[index].nombre}"
            sheet.dismiss()
        }
        sheet.setContentView(view)
        sheet.show()
    }
}