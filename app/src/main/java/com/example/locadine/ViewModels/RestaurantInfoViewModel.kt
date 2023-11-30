package com.example.locadine.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.locadine.pojos.RestaurantInfo

class RestaurantInfoViewModel: ViewModel() {
    companion object {
        var restaurantInfoList: List<RestaurantInfo> = listOf()
    }
}