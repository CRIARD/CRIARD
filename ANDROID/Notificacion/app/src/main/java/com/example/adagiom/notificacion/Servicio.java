package com.example.adagiom.notificacion;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

public class Servicio extends Service {

    private int mRandomNumber;
    private boolean mIsRandomGeneratorOn;

    private final int MIN=0;
    private final int MAX=100;
    final Messenger messenger=new Messenger(new ServiceHandler());
    public static final String FILENAME = "fileName";
    public static final String URLPATH = "urlPath";
    public static final String RESULTPATH = "urlPath";
    private Messenger outMessenger;
    public static final int GET_COUNT=0;
    public static int ESTADO = 1;


    private class ServiceHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.i("Servicio","Mensaje Recibido");
            switch (msg.what){
                case GET_COUNT:
                    Toast.makeText(getApplicationContext(), "Hola!!!", Toast.LENGTH_SHORT).show();

                    Message backMsg = Message.obtain();
                    backMsg.arg1 = ESTADO;
                    Bundle bundle = new Bundle();
                    bundle.putString(RESULTPATH, "Respuesta Mensaje");
                    backMsg.setData(bundle);
                    try {
                        outMessenger.send(backMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Servicio","In OnBind");
        Bundle extras = intent.getExtras();
        // Get messager from the Activity
        if (extras != null) {
            outMessenger = (Messenger) extras.get("MESSENGER");
        }
            return messenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i("Servicio","In OnReBind");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i("Servicio","Service Started");
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRandomNumberGenerator();
        Log.i("Servicio","Service Destruido");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Servicio","Servicio Iniciado...");
        mIsRandomGeneratorOn =true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                startRandomNumberGenerator();
            }
        }).start();
        return START_STICKY;
    }

    private void stopRandomNumberGenerator(){
        mIsRandomGeneratorOn =false;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("Servicio","In onUnbind");
        return super.onUnbind(intent);
    }

    private void startRandomNumberGenerator(){
        while (mIsRandomGeneratorOn){
            try{
                Thread.sleep(1000);
                if(mIsRandomGeneratorOn){
                    mRandomNumber =new Random().nextInt(MAX)+MIN;
                    Log.i("Servicio","Generando numeros Random: "+ mRandomNumber);
                }
            }catch (InterruptedException e){
                Log.i("Servicio","Thread Interrupted");
            }
        }
    }
}
