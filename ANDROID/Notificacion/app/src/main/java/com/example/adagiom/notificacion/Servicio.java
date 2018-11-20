package com.example.adagiom.notificacion;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class Servicio extends Service {

    Handler handlerService;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){

        Toast.makeText(this,"Servicio generado", Toast.LENGTH_SHORT).show();
        handlerService = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Servicio iniciado",Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "Servicio iniciado...");
        Message msg = new Message();
        msg.obj = "Mensaje enviado desde Servicio";
        handlerService.sendMessage(msg);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this,"Servicio destruido",Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "Servicio destruido...");
    }
}
