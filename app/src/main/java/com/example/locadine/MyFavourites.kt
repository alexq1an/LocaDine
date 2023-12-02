package com.example.locadine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locadine.adapters.FavouriteViewAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyFavourites : AppCompatActivity() {

    private lateinit var recycleView: RecyclerView
    private lateinit var recyclerViewAdapter: FavouriteViewAdapter
    private lateinit var favouritesArray: ArrayList<String>

    private lateinit var db: FirebaseFirestore
    private lateinit var fbAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_favourites)

        db = FirebaseFirestore.getInstance()
        fbAuth = FirebaseAuth.getInstance()
        val intent = Intent(this, RestaurantDetailsActivity::class.java)

        favouritesArray = ArrayList()

        recycleView = findViewById(R.id.recycle_view)
        recycleView.layoutManager = LinearLayoutManager(this)



        getFavourites(object: onFavouriteGetListener {
            override fun onDataGet(favouritesList: ArrayList<ArrayList<String>>) {

                // sets up the recycle view once the data has been received
                recyclerViewAdapter = FavouriteViewAdapter(this@MyFavourites, favouritesList)
                recycleView.adapter = recyclerViewAdapter

                recyclerViewAdapter.setOnItemClickListener(object: FavouriteViewAdapter.onItemClickListener {
                    override fun onItemClick(data: ArrayList<String>) {

                        // on item click it will show Restaurant details activity
                        intent.putExtra("PLACE_ID", data[2])
                        startActivity(intent)
                    }
                })
            }

        })

    }

    private fun getFavourites(listener: onFavouriteGetListener) {
        val favouritesList = ArrayList<ArrayList<String>>()

        val favourites = db.collection("users").document(fbAuth.uid!!).collection("favourites")

        favourites.get().addOnCompleteListener(this) {
            if (it.isSuccessful) {
                it.result.documents.forEach { doc ->

                    val favouriteVal = ArrayList<String>()
                    favouriteVal.add(doc.data!!["restaurantName"].toString())
                    favouriteVal.add(doc.data!!["restaurantPicture"].toString())
                    favouriteVal.add(doc.data!!["restaurantID"].toString())
                    favouritesList.add(favouriteVal)

                }

                listener.onDataGet(favouritesList)
            }
        }
    }

    interface onFavouriteGetListener {
        fun onDataGet(favouritesList: ArrayList<ArrayList<String>>)
    }

}