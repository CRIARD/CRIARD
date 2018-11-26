package app.criard.criardapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

class HandlerActivity extends Handler {

    private static HandlerActivity handler;
    public String dato_temp;
    public String dato_cuna;

    public static HandlerActivity getInstance(){

        if (handler == null){ //if there is no instance available... create new one
            handler = new HandlerActivity();
        }
        return handler;
    }

    @Override
    public void handleMessage(Message msg) {

        super.handleMessage(msg);
        Bundle data = msg.getData();
        Message message = Message.obtain();
        String text;
        int cuna;
        int humedad_apagado;
        int temperatura;
        Log.i("MainActivity","Respuesta recibida");
        Log.i("MainActivity", String.valueOf(msg.arg1));
        switch (msg.arg1){
            case ServicioBT.GET_RESPUESTA:

                text = data.getString(ServicioBT.RESULTPATH);

                cuna = text.indexOf("C");
                temperatura = text.indexOf("T");

                if(temperatura >= 0){
                    Log.i("Handler","temperatura");
                    dato_temp = text.substring(temperatura+1,text.length());
                }else {
                    Log.i("Handler","servo encendido");
                    message.obj = "encendi el servo";
                    dato_cuna = text;
                }

                break;
        }
    }
    public String getDato_temp() {
        return dato_temp;
    }

    public String getDato_cuna() {
        return dato_cuna;
    }
}
