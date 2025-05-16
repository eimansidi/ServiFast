package com.eiman.servifast.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.adapters.PostAdapter
import com.eiman.servifast.adapters.SmallPostAdapter
import com.eiman.servifast.adapters.TopUsersAdapter
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.items.ServicePostItem
import com.eiman.servifast.api.items.SmallPostItem
import com.eiman.servifast.api.items.TopUserItem
import com.eiman.servifast.api.models.FilterRequest
import com.eiman.servifast.ui.FilterDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var logo: ImageView
    private lateinit var searchBar: EditText
    private lateinit var btnFilter: ImageButton
    private lateinit var scrollView: NestedScrollView
    private lateinit var rvNovedades: RecyclerView
    private lateinit var rvMejorValorados: RecyclerView
    private lateinit var rvAllPosts: RecyclerView
    private lateinit var btnHome: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var btnSell: ImageButton

    private val userIdentifier: String by lazy {
        getSharedPreferences("user_session", MODE_PRIVATE)
            .getString("user_identifier", "") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        logo             = findViewById(R.id.logo)
        searchBar        = findViewById(R.id.searchBar)
        btnFilter        = findViewById(R.id.btnFilter)
        scrollView       = findViewById(R.id.viewHome)
        rvNovedades      = findViewById(R.id.rvNovedades)
        rvMejorValorados = findViewById(R.id.rvMejorValorados)
        rvAllPosts       = findViewById(R.id.rvAllPosts)
        btnHome          = findViewById(R.id.btnHome)
        btnMenu          = findViewById(R.id.btnMenu)
        btnSell          = findViewById(R.id.btnSell)

        // Scroll to top
        logo.setOnClickListener { scrollView.smoothScrollTo(0, 0) }
        btnHome.setOnClickListener { scrollView.smoothScrollTo(0, 0) }

        // Search on enter
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    startActivity(Intent(this, SearchActivity::class.java).apply {
                        putExtra("query", query)
                    })
                }
                true
            } else false
        }

        btnFilter.setOnClickListener {
            FilterDialog(this) { criteria: FilterRequest ->
                startActivity(Intent(this, SearchActivity::class.java).apply {
                    putExtra("filterMin",  criteria.minPrice)
                    putExtra("filterMax",  criteria.maxPrice)
                    putExtra("filterLoc",  criteria.location)
                    putExtra("filterType", criteria.userType)
                    putExtra("filterCat",  criteria.categoryId)
                })
            }.show()
        }

        // Sell & Menu navigation
        btnSell.setOnClickListener { startActivity(Intent(this, SellActivity::class.java)) }
        btnMenu.setOnClickListener { startActivity(Intent(this, MenuActivity::class.java)) }

        // RecyclerViews layout managers
        rvNovedades.layoutManager      = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvMejorValorados.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvAllPosts.layoutManager       = GridLayoutManager(this, 2)

        // Disable nested scrolling
        rvNovedades.isNestedScrollingEnabled      = false
        rvMejorValorados.isNestedScrollingEnabled = false
        rvAllPosts.isNestedScrollingEnabled       = false

        loadNovedades()
        loadTopUsers()
        loadAllPosts()
    }

    private fun loadNovedades() {
        RetrofitClient.instance.getLatestServices()
            .enqueue(object : Callback<List<ServicePostItem>> {
                override fun onResponse(
                    call: Call<List<ServicePostItem>>,
                    response: Response<List<ServicePostItem>>
                ) {
                    val items = response.body().orEmpty()
                    rvNovedades.adapter = PostAdapter(userIdentifier, items)
                }
                override fun onFailure(call: Call<List<ServicePostItem>>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Error al cargar novedades", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadTopUsers() {
        RetrofitClient.instance.getTopUsers()
            .enqueue(object : Callback<List<TopUserItem>> {
                override fun onResponse(
                    call: Call<List<TopUserItem>>,
                    response: Response<List<TopUserItem>>
                ) {
                    val items = response.body().orEmpty()
                    rvMejorValorados.adapter = TopUsersAdapter(items) { user ->
                        startActivity(Intent(this@HomeActivity, ProfileActivity::class.java).apply {
                            putExtra("user_identifier", user.id)
                        })
                    }
                }
                override fun onFailure(call: Call<List<TopUserItem>>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Error al cargar mejor valorados", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadAllPosts() {
        RetrofitClient.instance.getSmallServices()
            .enqueue(object : Callback<List<SmallPostItem>> {
                override fun onResponse(
                    call: Call<List<SmallPostItem>>,
                    response: Response<List<SmallPostItem>>
                ) {
                    val items = response.body().orEmpty()
                    rvAllPosts.adapter = SmallPostAdapter(userIdentifier, items)
                }
                override fun onFailure(call: Call<List<SmallPostItem>>, t: Throwable) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Error al cargar servicios ligeros",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
