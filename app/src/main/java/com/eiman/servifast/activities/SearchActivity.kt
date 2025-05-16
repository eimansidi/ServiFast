package com.eiman.servifast.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.adapters.SmallPostAdapter
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.items.ServicePostItem
import com.eiman.servifast.api.items.SmallPostItem
import com.eiman.servifast.api.models.CategoryResponse
import com.eiman.servifast.api.models.FilterRequest
import com.eiman.servifast.api.models.SearchRequest
import com.eiman.servifast.ui.FilterDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var logo: ImageView
    private lateinit var searchBar: EditText
    private lateinit var btnFilter: ImageButton
    private lateinit var filtersScroll: HorizontalScrollView
    private lateinit var filtersContainer: LinearLayout
    private lateinit var tvSearchHeader: TextView
    private lateinit var rvResults: RecyclerView
    private lateinit var btnHome: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var btnSell: ImageButton

    private val userIdentifier: String by lazy {
        getSharedPreferences("user_session", MODE_PRIVATE)
            .getString("user_identifier", "") ?: ""
    }

    private val categoryMap = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search)

        logo             = findViewById(R.id.logo)
        searchBar        = findViewById(R.id.searchBar)
        btnFilter        = findViewById(R.id.btnFilter)
        filtersScroll    = findViewById(R.id.filtersScroll)
        filtersContainer = findViewById(R.id.filtersContainer)
        tvSearchHeader   = findViewById(R.id.tvSearchHeader)
        rvResults        = findViewById(R.id.rvSearchResults)
        btnHome          = findViewById(R.id.btnHome)
        btnMenu          = findViewById(R.id.btnMenu)
        btnSell          = findViewById(R.id.btnSell)

        rvResults.layoutManager = GridLayoutManager(this, 2)
        rvResults.isNestedScrollingEnabled = false

        logo.setOnClickListener { rvResults.scrollToPosition(0) }
        btnHome.setOnClickListener { rvResults.scrollToPosition(0) }

        RetrofitClient.instance.getCategories()
            .enqueue(object: Callback<List<CategoryResponse>> {
                override fun onResponse(call: Call<List<CategoryResponse>>, resp: Response<List<CategoryResponse>>) {
                    resp.body()?.forEach { categoryMap[it.id] = it.nombre }
                }
                override fun onFailure(call: Call<List<CategoryResponse>>, t: Throwable) {  }
            })

        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val q = v.text.toString().trim()
                if (q.isNotEmpty()) performTextSearch(q)
                true
            } else false
        }

        btnFilter.setOnClickListener {
            FilterDialog(this) { criteria: FilterRequest ->
                performFilter(criteria)
            }.show()
        }

        btnSell.setOnClickListener { startActivity(Intent(this, SellActivity::class.java)) }
        btnMenu.setOnClickListener { startActivity(Intent(this, MenuActivity::class.java)) }

        intent.getStringExtra("query")?.let { performTextSearch(it) }
        intent.takeIf {
            it.hasExtra("filterMin") ||
                    it.hasExtra("filterMax") ||
                    it.hasExtra("filterLoc") ||
                    it.hasExtra("filterType") ||
                    it.hasExtra("filterCat")
        }?.let {
            val min   = it.getDoubleExtra("filterMin", Double.NaN).takeIf { v -> !v.isNaN() }
            val max   = it.getDoubleExtra("filterMax", Double.NaN).takeIf { v -> !v.isNaN() }
            val loc   = it.getStringExtra("filterLoc")
            val type  = it.getStringExtra("filterType")
            val cat   = it.getIntExtra("filterCat", -1).takeIf { id -> id >= 0 }
            performFilter(FilterRequest(min, max, loc, type, cat))
        }
    }

    private fun performTextSearch(query: String) {
        tvSearchHeader.text = "${getString(R.string.busqueda)}: $query"
        tvSearchHeader.visibility = View.VISIBLE
        filtersContainer.removeAllViews()
        filtersScroll.visibility = View.GONE

        RetrofitClient.instance.searchServices(SearchRequest(query))
            .enqueue(object : Callback<List<ServicePostItem>> {
                override fun onResponse(call: Call<List<ServicePostItem>>, response: Response<List<ServicePostItem>>) {
                    val list = response.body().orEmpty().map {
                        SmallPostItem(
                            id          = it.id,
                            userType    = it.userType,
                            imageBase64 = it.imageBase64,
                            negotiable  = it.negotiable,
                            title       = it.title,
                            price       = it.price
                        )
                    }
                    rvResults.adapter = SmallPostAdapter(userIdentifier, list)
                }
                override fun onFailure(call: Call<List<ServicePostItem>>, t: Throwable) {
                    Toast.makeText(this@SearchActivity, "Error al buscar servicios", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun performFilter(fr: FilterRequest) {
        tvSearchHeader.visibility = View.GONE
        filtersContainer.removeAllViews()
        filtersScroll.visibility = View.VISIBLE
        addFilterChips(fr)

        RetrofitClient.instance.filterServices(fr)
            .enqueue(object : Callback<List<ServicePostItem>> {
                override fun onResponse(call: Call<List<ServicePostItem>>, response: Response<List<ServicePostItem>>) {
                    val list = response.body().orEmpty().map {
                        SmallPostItem(
                            id          = it.id,
                            userType    = it.userType,
                            imageBase64 = it.imageBase64,
                            negotiable  = it.negotiable,
                            title       = it.title,
                            price       = it.price
                        )
                    }
                    rvResults.adapter = SmallPostAdapter(userIdentifier, list)
                }
                override fun onFailure(call: Call<List<ServicePostItem>>, t: Throwable) {
                    Toast.makeText(this@SearchActivity, "Error al filtrar servicios", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addFilterChips(fr: FilterRequest) {
        fun formatPrice(p: Double) =
            if (p == p.toInt().toDouble()) "${p.toInt()}" else p.toString()

        fun addChip(text: String, onRemove: () -> Unit) {
            val chip = layoutInflater.inflate(R.layout.chip_filter, filtersContainer, false)
            chip.findViewById<TextView>(R.id.tvFilter).text = text
            chip.findViewById<ImageButton>(R.id.filterClose).setOnClickListener { onRemove() }
            filtersContainer.addView(chip)
        }

        fr.minPrice?.let {
            addChip("${getString(R.string.precio_min)}: ${formatPrice(it)}") { performFilter(fr.copy(minPrice = null)) }
        }
        fr.maxPrice?.let {
            addChip("${getString(R.string.precio_max)}: ${formatPrice(it)}") { performFilter(fr.copy(maxPrice = null)) }
        }
        fr.location?.let {
            addChip("${getString(R.string.ubicacion)}: $it") { performFilter(fr.copy(location = null)) }
        }
        fr.userType?.let {
            val label = if (it == "particular") getString(R.string.particular) else getString(R.string.profesional)
            addChip("$label") { performFilter(fr.copy(userType = null)) }
        }
        fr.categoryId?.let { id ->
            val name = categoryMap[id] ?: id.toString()
            addChip("${getString(R.string.categorias)}: $name") { performFilter(fr.copy(categoryId = null)) }
        }
    }
}
