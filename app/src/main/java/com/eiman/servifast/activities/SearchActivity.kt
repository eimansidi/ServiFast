package com.eiman.servifast.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.adapters.SmallPostAdapter
import com.eiman.servifast.adapters.TopUsersAdapter
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.items.ServicePostItem
import com.eiman.servifast.api.items.SmallPostItem
import com.eiman.servifast.api.items.TopUserItem
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
    private lateinit var rvResults: RecyclerView
    private lateinit var btnHome: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var btnSell: ImageButton

    private val userIdentifier: String by lazy {
        getSharedPreferences("user_session", MODE_PRIVATE)
            .getString("user_identifier", "") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search)

        logo      = findViewById(R.id.logo)
        searchBar = findViewById(R.id.searchBar)
        btnFilter = findViewById(R.id.btnFilter)
        rvResults = findViewById(R.id.rvSearchResults)
        btnHome   = findViewById(R.id.btnHome)
        btnMenu   = findViewById(R.id.btnMenu)
        btnSell   = findViewById(R.id.btnSell)

        // Layout
        rvResults.layoutManager = GridLayoutManager(this, 2)
        rvResults.isNestedScrollingEnabled = false

        // Scroll-to-top on logo/home
        logo.setOnClickListener { rvResults.scrollToPosition(0) }
        btnHome.setOnClickListener { rvResults.scrollToPosition(0) }

        // Search on enter
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val q = v.text.toString().trim()
                if (q.isNotEmpty()) performTextSearch(q)
                true
            } else false
        }

        // Filter dialog
        btnFilter.setOnClickListener {
            FilterDialog(this) { criteria ->
                performFilter(criteria)
            }.show()
        }

        // Sell & Menu navigation
        btnSell.setOnClickListener { startActivity(Intent(this, SellActivity::class.java)) }
        btnMenu.setOnClickListener { startActivity(Intent(this, MenuActivity::class.java)) }

        // Check intent extras
        intent.getStringExtra("query")?.let { performTextSearch(it) }
        run {
            // filter extras
            val min   = intent.getDoubleExtra("filterMin",  Double.NaN).takeIf { !it.isNaN() }
            val max   = intent.getDoubleExtra("filterMax",  Double.NaN).takeIf { !it.isNaN() }
            val loc   = intent.getStringExtra("filterLoc")
            val type  = intent.getStringExtra("filterType")
            val cat   = intent.getIntExtra("filterCat", -1).takeIf { it >= 0 }
            if (min != null || max != null || loc != null || type != null || cat != null) {
                performFilter(FilterRequest(min, max, loc, type, cat))
            }
        }
    }

    private fun performTextSearch(query: String) {
        RetrofitClient.instance.searchServices(SearchRequest(query))
            .enqueue(object : Callback<List<ServicePostItem>> {
                override fun onResponse(
                    call: Call<List<ServicePostItem>>,
                    response: Response<List<ServicePostItem>>
                ) {
                    val small = response.body().orEmpty().map {
                        SmallPostItem(
                            id          = it.id,
                            userType    = it.userType,
                            imageBase64 = it.imageBase64,
                            negotiable  = it.negotiable,
                            title       = it.title,
                            price       = it.price
                        )
                    }
                    rvResults.adapter = SmallPostAdapter(userIdentifier, small)
                }
                override fun onFailure(call: Call<List<ServicePostItem>>, t: Throwable) {
                    Toast.makeText(this@SearchActivity, "Error al buscar servicios", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun performFilter(criteria: FilterRequest) {
        RetrofitClient.instance.filterServices(criteria)
            .enqueue(object : Callback<List<ServicePostItem>> {
                override fun onResponse(
                    call: Call<List<ServicePostItem>>,
                    response: Response<List<ServicePostItem>>
                ) {
                    val small = response.body().orEmpty().map {
                        SmallPostItem(
                            id          = it.id,
                            userType    = it.userType,
                            imageBase64 = it.imageBase64,
                            negotiable  = it.negotiable,
                            title       = it.title,
                            price       = it.price
                        )
                    }
                    rvResults.adapter = SmallPostAdapter(userIdentifier, small)
                }
                override fun onFailure(call: Call<List<ServicePostItem>>, t: Throwable) {
                    Toast.makeText(this@SearchActivity, "Error al filtrar servicios", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
