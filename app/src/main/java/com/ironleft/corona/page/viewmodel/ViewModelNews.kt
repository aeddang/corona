package com.ironleft.corona.page.viewmodel


import androidx.lifecycle.ViewModel
import com.ironleft.corona.store.Repository


class ViewModelNews (val repo: Repository) : ViewModel(){
    private val appTag = javaClass.simpleName



    override fun onCleared() {
        super.onCleared()

    }



}