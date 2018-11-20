package com.example.adagiom.notificacion;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class ControladorMensaje extends HandlerThread {

    public Handler mHandler;


    public ControladorMensaje(String name) {
        super(name);
    }

    public ControladorMensaje(String name, int priority) {
        super(name, priority);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

        mHandler = new Handler(getLooper()){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                System.out.println(msg);
                Log.i("Servicio",msg.toString());
            }
        };

        mHandler.obtainMessage().sendToTarget();
    }

}
