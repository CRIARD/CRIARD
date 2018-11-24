package app.criard.criardapp;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Informe_temperatura extends Activity {

    TextView temp;
    Messenger mService = null;
    boolean mBound;
    private int contador = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_temperatura);
        temp = (TextView) findViewById(R.id.temp);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Cambia el estado del Bluethoot (Acrtivado /Desactivado)
        //se define (registra) el handler que captura los broadcast anterirmente mencionados.
        registerReceiver(mReceiver, filter);

        new Thread(new Runnable() {

            @Override
            public void run() {
                // El servicio se finaliza a sí mismo cuando finaliza su
                // trabajo.

                    try {
                        while(!mBound) {
                            // Simulamos trabajo de 10 segundos.
                            Thread.sleep(10000);

                            Message msg = Message.obtain(null, ServicioBT.GET_INFO_TEMP, 0, 0);
                            try {
                                mService.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        }).start();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_cuna:
                    Intent intent1 = new Intent(Informe_temperatura.this,CRIArdMainActivity.class);
                    startActivity(intent1);
                    finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(mConnection);
        unregisterReceiver(mReceiver);
    }
    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Bundle data = message.getData();
            String text;
            int temperatura;
            Log.i("MainActivity","Respuesta recibida");
            Log.i("MainActivity", String.valueOf(message.arg1));
            switch (message.arg1){
                case ServicioBT.GET_RESPUESTA:
                    text = data.getString(ServicioBT.RESULTPATH);
                    temperatura = text.indexOf("T");
                    if(temperatura >= 0){
                        showToast("Temperatura: " + text.substring(temperatura));
                    }
                    break;
            }
        }
    };

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                if(state == BluetoothAdapter.STATE_DISCONNECTED || state == BluetoothAdapter.STATE_OFF){
                    Toast.makeText(Informe_temperatura.this,"Se ha perdido la conexion..",Toast.LENGTH_SHORT).show();
                    unbindService(mConnection);
                    AlertDialog alerta = createSimpleDialog();
                    alerta.show();
                }
            }
        }
    };

    public AlertDialog createSimpleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Error")
                .setMessage("Se ha perdido la conexion.")
                .setNegativeButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Informe_temperatura.this,ServicioBT.class);
                                stopService(intent);
                                finish();
                            }
                        });

        return builder.create();
    }
    @Override
    protected void onStop()
    {

        super.onStop();
    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getApplicationContext(), ServicioBT.class);
        Messenger messenger = new Messenger(handler);
        intent.putExtra("MESSENGER_1", messenger);
        intent.putExtra("CLIENTE",ServicioBT.ACTIVITY_TEMP);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onBackPressed() {

        if(contador == 0){
            Toast.makeText(getApplicationContext(),"Presione nuevamente para salir",Toast.LENGTH_SHORT).show();
            contador++;
        }else{
            Intent intent = new Intent(Informe_temperatura.this,ServicioBT.class);
            stopService(intent);
            finish();
        }

        new CountDownTimer(3000,1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                contador=0;
            }
        }.start();
    }

}
