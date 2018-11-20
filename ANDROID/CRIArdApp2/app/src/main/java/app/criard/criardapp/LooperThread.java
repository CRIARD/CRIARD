package app.criard.criardapp;

import android.os.HandlerThread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class LooperThread extends HandlerThread {
    public Handler mHandler;
    public Message message;
    public StringBuilder stringBuilder = new StringBuilder();
    public LooperThread(String name) {
        super(name);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String mensaje = (String) msg.obj;
                stringBuilder.append(mensaje);
                Log.i("mensaje thread",stringBuilder.toString());
                // process incoming messages here
                // this will run in non-ui/background thread
            }
        };
    }
    public Message enviarMensaje(){
        return message;
    }
}
