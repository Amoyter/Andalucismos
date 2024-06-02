package com.amt.andalucismos.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.R;
import com.amt.andalucismos.models.Palabra;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificacionReceiver", "onReceive: Intent recibido.");
        MainViewModel mainViewModel = new ViewModelProvider(ViewModelStoreOwnerSingleton.getInstance()).get(MainViewModel.class);

        mainViewModel.getPalabraDelDia().observeForever(palabra -> {
            if (palabra != null) {
                String palabraDelDia = palabra.getPalabra();
                Log.d("NotificacionReceiver", "Palabra del día: " + palabraDelDia);
                enviarNotification(context, palabraDelDia, palabra.getExpresionId());
            }
        });

        mainViewModel.loadPalabraDelDia();
    }

    private void enviarNotification(Context context, String palabraDelDia, String expresionId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("palabraId", expresionId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "palabraDelDiaChannel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Palabra del día")
                .setContentText("La palabra del día es: " + palabraDelDia)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}

