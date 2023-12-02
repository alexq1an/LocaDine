package com.example.locadine

import com.example.locadine.pojos.RestaurantInfo

object FilterSetting { // for passing Restaurant filter info
    var distance: Int = 200
    var rating: Double = 0.0
    var price: Int = 1
    var updateTrigger = false

    var restaurants: List<RestaurantInfo>? = null // for pass filtered out list of restaurant
}