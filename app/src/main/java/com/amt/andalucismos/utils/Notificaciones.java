package com.amt.andalucismos.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public interface Notificaciones {

    /**
     * Muestra un mensaje Toast en la aplicación.
     *
     * Este método estático se puede utilizar en cualquier parte de la aplicación para mostrar un mensaje Toast.
     * Asegura que el Toast se muestre en el hilo de la interfaz de usuario. El método acepta el contexto de la aplicación,
     * el mensaje a mostrar y la duración del Toast.
     *
     * @param cContexto El contexto de la aplicación, normalmente 'this' o 'getApplicationContext()'.
     * @param sMensaje El mensaje de texto que se mostrará en el Toast.
     * @param iDuracion La duración del Toast; usa Toast.LENGTH_SHORT o Toast.LENGTH_LONG.
     */
    static void makeToast(Context cContexto, String sMensaje, int iDuracion){
        // Se asegura de que el Toast se muestre en el hilo de la interfaz de usuario
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(cContexto, sMensaje, iDuracion).show());
    }


    interface RespuestaDialog {
        void onPositivo();
        void onNegativo();
        void onNeutral();
    }

    /**
     * Crea y muestra un diálogo con botones de acción y gestiona las respuestas mediante callbacks.
     *
     * @param cContexto   El contexto de la aplicación, normalmente 'this' o 'getApplicationContext()'.
     * @param sTitulo     El título del diálogo. Si es nulo o vacío, se mostrará "Alert".
     * @param sMensaje    El mensaje principal del diálogo. Si es nulo o vacío, se mostrará "Elige una opción:".
     * @param sPositivo   El texto para el botón positivo. Si es nulo o vacío, el botón no se mostrará.
     * @param sNegativo   El texto para el botón negativo. Si es nulo o vacío, el botón no se mostrará.
     * @param sNeutral    El texto para el botón neutral. Si es nulo o vacío, el botón no se mostrará.
     * @param rdCallback    La interfaz de devolución de llamada para manejar los eventos de clic en los botones del diálogo.
     */
    static void makeDialog(Context cContexto, String sTitulo, String sMensaje, String sPositivo, String sNegativo, String sNeutral, RespuestaDialog rdCallback){
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(cContexto);

        if(sTitulo != null && !sTitulo.isEmpty()){
            adBuilder.setTitle(sTitulo);
        }

        adBuilder.setMessage(sMensaje != null && !sMensaje.isEmpty() ? sMensaje : "Elige una opción:");

        if(sPositivo != null && !sPositivo.isEmpty()){
            adBuilder.setPositiveButton(sPositivo, (dialogInterface, i) -> rdCallback.onPositivo());
        }

        if(sNegativo != null && !sNegativo.isEmpty()){
            adBuilder.setNegativeButton(sNegativo, (dialogInterface, i) -> rdCallback.onNegativo());
        }

        if(sNeutral != null && !sNeutral.isEmpty()){
            adBuilder.setNeutralButton(sNeutral, (dialogInterface, i) -> rdCallback.onNeutral());
        }

        adBuilder.create().show();
    }
}
