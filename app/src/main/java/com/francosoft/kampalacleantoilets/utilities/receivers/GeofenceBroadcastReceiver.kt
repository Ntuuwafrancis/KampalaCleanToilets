package com.francosoft.kampalacleantoilets.utilities.receivers

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.ui.MainActivity
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.ACTION_GEOFENCE_EVENT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.CHANNEL_ID
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.NOTIFICATION_ID
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class GeofenceBroadcastReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val event = GeofencingEvent.fromIntent(intent)
            var toilet = Toilet()
            if (event != null) {
                if(event.hasError()){
                    return
                }

                if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                    Log.v(TAG, context.getString(R.string.geofence_entered))

                    val geofence = event.triggeringGeofences?.get(0)
                    geofence?.requestId?.let {
                        getToiletByID(context, it)
                        setFence( context,it)
                    }
                }
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(context: Context, toilet: Toilet) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java)
                as NotificationManager
//        val toilet = getToiletByID(context, toiletId)
        Log.d(TAG, "Geofence ${toilet.stitle} Added")
        val message =
            "Visit ${toilet.stitle} to ease your self"

//        val args = Bundle()
//        val bundle = bundleOf("nearToiletId" to toilet.id)
        val bundle = bundleOf("toilet" to toilet )
        val intent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.mapFragment)
            .setComponentName(MainActivity::class.java)
            .setArguments(bundle)
            .createPendingIntent()

//        val contentIntent = Intent(context, MapsFragment::class.java)
//        contentIntent.putExtra(EXTRA_NEAR_TOILET, toilet)
//        val contentPendingIntent = PendingIntent.getActivity(
//            context,
//            NOTIFICATION_ID,
//            contentIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )

        val mapImage = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.kct_icon
        )
        val bigPicStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(mapImage)
            .bigLargeIcon(null)

        val notification = NotificationCompat.Builder(context.applicationContext,  CHANNEL_ID)
            .setSmallIcon(R.drawable.kct_icon)
            .setContentTitle("Discover the toilets near you now!")
            .setContentText(message)
            .setContentIntent(intent)
            .setAutoCancel(true)
//            .setStyle(bigPicStyle)
//            .setLargeIcon(mapImage)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun setFence(context: Context, toiletId : String) {

        val firebaseDb =  FirebaseDatabase.getInstance()
        val auth =  Firebase.auth
        val userId = auth.currentUser?.uid
        val userRef = firebaseDb.getReference("user")
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java) as User
                    if (user.uid == userId) {
                        user.triggeringGeofenceId = toiletId
                        userRef.child(userId!!).setValue(user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        userRef.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun getToiletByID(context: Context, toiletId: String){

        var toilet = Toilet()
        val firebaseDb =  FirebaseDatabase.getInstance()
        val dbref = firebaseDb.getReference("toilet")
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet1 = postSnapshot.getValue(Toilet::class.java) as Toilet

                    if (toilet1.id == toiletId) {
                        toilet = toilet1
                        sendNotification(context, toilet)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        dbref.addListenerForSingleValueEvent(valueEventListener)



    }


}

private const val TAG = "GeofenceReceiver"