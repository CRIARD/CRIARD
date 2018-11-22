package app.criard.criardapp;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoArranque extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Receiver","Servicio Arrancado");
        String packageName = intent.getPackage();
        if(packageName == "app.criard.criardapp"){
            //Intent service = new Intent(context,  ServicioBT.class);
            //context.startService(service);
        }
    }
}