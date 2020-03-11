package com.ironleft.corona.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ironleft.corona.MainActivity
import com.ironleft.corona.R
import com.lib.util.Log
import io.reactivex.subjects.PublishSubject
import java.util.*


class FirebaseMessaging : FirebaseMessagingService() {

    companion object {
        val pushTokenObservable = PublishSubject.create<String>()
        private var currentInstance: FirebaseMessaging? = null
        fun getInstance(): FirebaseMessaging? {
            return currentInstance
        }

        const val PUSH_DATA_KEY_TITLE = "title"
        const val PUSH_DATA_KEY_BODY = "body"
        const val PUSH_DATA_KEY_TYPE = "type"
        const val PUSH_DATA_KEY_PUSHTYPE = "pushType"
    }

    val param = HashMap<String, Any>()
    private var appTag = javaClass.simpleName
    var pushToken: String? = null
        private set(value) {
            if (field == value) return
            field = value
            pushToken?.let { pushTokenObservable.onNext(it) }
        }

    init {
        currentInstance = this

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            Log.i(appTag, "Message data background : ${remoteMessage.data}}")

            remoteMessage.data[PUSH_DATA_KEY_TITLE]?.let {
                param[PUSH_DATA_KEY_TITLE] = it
                if(it =="") param[PUSH_DATA_KEY_TITLE] = getString(R.string.app_name)
            }
            remoteMessage.data[PUSH_DATA_KEY_BODY]?.let { param[PUSH_DATA_KEY_BODY] = it }
            remoteMessage.data[PUSH_DATA_KEY_TYPE]?.let { param[PUSH_DATA_KEY_TYPE] = it }
            remoteMessage.data[PUSH_DATA_KEY_PUSHTYPE]?.let { param[PUSH_DATA_KEY_PUSHTYPE] = it }

            // PUSH_DATA_KEY_TYPE = 0 서비스 1 광고
            // PUSH_DATA_KEY_PUSHTYPE = 1~99 서비스 PUSH_DATA_KEY_TYPE = 0
            // PUSH_DATA_KEY_PUSHTYPE = 100~1000 광고 PUSH_DATA_KEY_TYPE = 1

//            Log.i(appTag, "서비스 : ${settingPreference.getServicePushKey()} / 마케팅 : ${settingPreference.getMarketingPushKey()}" +
//                    " /  푸시번호 : ${remoteMessage.data[PUSH_DATA_KEY_PUSHTYPE]} / 푸시타입 : ${remoteMessage.data[PUSH_DATA_KEY_TYPE]}")

            createNotification(param)
        }

        remoteMessage.notification?.let {
            Log.i(
                appTag, "Message Notification forground : ${remoteMessage.data} , remoteMessage.notification : " +
                        "${remoteMessage.notification}"
            )
            createNotification(param)
        }
    }

    override fun onNewToken(token: String) {
        pushToken = token
        Log.i(appTag, "onNewToken : $pushToken")
    }

    private fun handleNow(messageParam: HashMap<String, Any>) {
        Log.d(appTag, "Short lived task is done.")
    }

    private fun createNotification(messageParam: HashMap<String, Any>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(PUSH_DATA_KEY_TITLE, messageParam[PUSH_DATA_KEY_TITLE].toString())
        intent.putExtra(PUSH_DATA_KEY_BODY, messageParam[PUSH_DATA_KEY_BODY].toString())
        intent.putExtra(PUSH_DATA_KEY_TYPE, messageParam[PUSH_DATA_KEY_TYPE].toString())

        val reCode = rand(1,1000)
        val pendingIntent = PendingIntent.getActivity(
            this,  /* Request code */reCode, intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val channelId = getString(R.string.notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(messageParam[PUSH_DATA_KEY_TITLE].toString())
            .setContentText(messageParam[PUSH_DATA_KEY_BODY].toString())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messageParam[PUSH_DATA_KEY_BODY].toString())
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)


        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(reCode, notificationBuilder.build())
    }

    private fun rand(from: Int, to: Int) : Int {
        val random = Random()
        return random.nextInt(to - from) + from
    }

}