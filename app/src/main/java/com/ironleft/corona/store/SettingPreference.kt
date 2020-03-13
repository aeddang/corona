package com.ironleft.corona.store

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId

import com.ironleft.corona.PreferenceName
import com.lib.module.CachedPreference
import com.lib.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.Exception

class SettingPreference(context: Context) : CachedPreference(context, PreferenceName.SETTING) {
    companion object {
        private const val PUSH_ABLE = "pushAble"
        private const val SELECTED_COUNTRY = "selectedCounry"
    }
    private var appTag = javaClass.simpleName


    private var pushDispose:Disposable? = null
    fun putPushAble(isOn: Boolean) {
        put(PUSH_ABLE, isOn)
        pushDispose?.dispose()
        pushDispose = null
        if(isOn){
            try {
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) return@OnCompleteListener
                        task.result?.token?.let {
                            Log.d(appTag, "set current push key $it")
                            //settingPreference.setPushKey(it)
                        }
                    })
            } catch (e:Exception){
                Log.d(appTag, "get push key error $e")
            }

        }else{
            pushDispose = Observable.just(isOn).subscribeOn(Schedulers.io()).subscribe (
                {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId()
                        Log.d(appTag, "delete push key")
                    } catch (e:Exception){
                        Log.d(appTag, "delete push key error $e")
                    }

                },{
                    Log.d(appTag, "delete push key error $it")
                }
            )
        }
    }
    fun getPushAble(): Boolean = get(PUSH_ABLE, true) as Boolean

    fun putSelectedCountry(country: String)  = put(SELECTED_COUNTRY, country)
    fun getSelectedCountry(): String = get(SELECTED_COUNTRY, "") as String

}