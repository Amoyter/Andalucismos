package com.amt.andalucismos.ui.ajustes;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.amt.andalucismos.R;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.Notificaciones;

public class AjustesFragment extends PreferenceFragmentCompat {
    private static final String CHANNEL_ID = "canalPalabraDelDia";
    private MainViewModel mainViewModel;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_ajustes, rootKey);

        // Obtener la preferencia de notificaciones
        manejarPreferenciaNotificaciones();

        // Obtener la preferencia de limpiar historial
        manejearPreferenciaHistorial();
    }

    private void manejarPreferenciaNotificaciones() {
        SwitchPreferenceCompat preferenciaNotificaciones = findPreference("ajustes_notificaciones");
        if (preferenciaNotificaciones != null) {
            preferenciaNotificaciones.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean habilitado = (Boolean) newValue;
                if (habilitado) {
                    habilitarNotificaciones();
                } else {
                    deshabilitarNotificaciones();
                }
                return true;
            });
        }
    }

    private void manejearPreferenciaHistorial() {
        Preference limpiarHistorialPreference = findPreference("ajustes_limpiar_historial");
        if (limpiarHistorialPreference != null) {
            limpiarHistorialPreference.setOnPreferenceClickListener(preference -> {
                Notificaciones.makeDialog(getContext(), "Limpiar historial"
                        , "¿Estás seguro de que deseas limpiar el historial?"
                        , "Sí", "No", "", new Notificaciones.RespuestaDialog() {
                            @Override
                            public void onPositivo() {
                                mainViewModel.eliminarHistorial();
                                Notificaciones.makeToast(getContext(), "Historial de búsqueda limpiado", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onNegativo() {}

                            @Override
                            public void onNeutral() {}
                        });
                return true;
            });
        }
    }

    private void habilitarNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Palabra del Día",
                    NotificationManager.IMPORTANCE_DEFAULT);
            canal.setDescription("Canal para notificaciones de la palabra del día");
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    private void deshabilitarNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.deleteNotificationChannel(CHANNEL_ID);
        }
    }
}