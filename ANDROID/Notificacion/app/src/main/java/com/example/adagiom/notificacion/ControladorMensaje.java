package com.example.adagiom.notificacion;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

public class ControladorMensaje extends HandlerThread {

    private static final String TAG = ControladorMensaje.class.getSimpleName();

    public Handler mHandler;

    public ControladorMensaje(String name){
        super(name);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.i(TAG,"Respuesta del mensaje: "+msg.obj);
            }
        };
    }
}
