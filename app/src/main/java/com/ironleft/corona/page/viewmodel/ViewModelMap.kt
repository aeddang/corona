package com.ironleft.corona.page.viewmodel


import androidx.lifecycle.ViewModel
import com.ironleft.corona.store.Repository



class ViewModelMap (val repo: Repository) : ViewModel(){
    private val appTag = javaClass.simpleName

    init {


    }


    override fun onCleared() {
        super.onCleared()

    }



}