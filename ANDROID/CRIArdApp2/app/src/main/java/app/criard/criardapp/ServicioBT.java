package app.criard.criardapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ServicioBT extends Service {

    private boolean flagLUZ = false;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    // String for MAC address del Hc05
    private static String address = "98:D3:71:F5:D9:06";
    private ConnectedThread mConnectedThread;
    final Messenger messenger=new Messenger(new ServiceHandler());
    private StringBuilder recDataString = new StringBuilder();
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final int GET_SERVO_OFF=1;
    public static final int GET_SERVO_ON=2;
    public static final int GET_LED_OFF=3;
    public static final int GET_LED_ON=4;
    public static final int GET_MUSICA_ON=5;
    public static final int GET_MUSICA_OFF=6;
    public static final int GET_RESPUESTA=7;
    public static final int GET_INFO=0;

    public static final String RESULTPATH = "RespuestaServicio";
    Handler bluetoothIn;
    private Messenger outMessenger;
    final int handlerState = 0; //used to identify handler message

    private class ServiceHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = new Bundle();
            Message message = Message.obtain();
            switch (msg.what){
                case GET_INFO:
                    Log.i("Servicio","Mensaje Recibido");
                    mConnectedThread.write("#");
                    break;
                case GET_SERVO_ON:
                    mConnectedThread.write("1");
                    break;
                case GET_SERVO_OFF:
                    mConnectedThread.write("2");
                    break;
                case GET_LED_ON:
                    mConnectedThread.write("3");
                    break;
                case GET_LED_OFF:
                    mConnectedThread.write("4");
                    break;
                case GET_MUSICA_ON:
                    mConnectedThread.write("5");
                    break;
                case GET_MUSICA_OFF:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    //Metodo que se llama cuando se crea el servicio con startservice
    public void onCreate() {
        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Toast.makeText(this, "Servicio creado", Toast.LENGTH_SHORT).show();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        bluetoothIn = Handler_Msg_Hilo_Principal();

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            showToast("La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ServicioBT.ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    //Despues de onCreate automaticamente se ejecuta a OnstartCommand,
    //idArranque: Es el id del servicio a ejecutar
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
        Toast.makeText(this, "Servicio arrancado " + idArranque, Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
    }

    @Override
    //On destroy se invoca cuando se ejcuta stopService
    public void onDestroy() {
        Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    //Se invoca a este metodo cuando ejecuta bindService
    public IBinder onBind(Intent intent) {
        Log.i("Servicio","In OnBind");
        Bundle extras = intent.getExtras();
        // Get messager from the Activity
        if (extras != null) {
            outMessenger = (Messenger) extras.get("MESSENGER");
        }
        return messenger.getBinder();
    }


    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)// cambiar aqui
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true) {
                try {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.i("Mensaje BLuethoot", readMessage);
                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                showToast("La conexion fallo");
                //finish();
            }
        }
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                Bundle extra_servo = new Bundle();
                Message msg_servo = Message.obtain();
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);

                    int endOfLineIndex = recDataString.indexOf("\n");
                    Log.i("Arduino","Mensaje recibido de arduino");
                    //cuando recibo toda una linea la muestro en el layout

                    if (endOfLineIndex > 0) {

                        int estadoCuna = recDataString.indexOf("H1");
                        int estandoLLando = recDataString.indexOf("ME");
                        if(estadoCuna >= 0){
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    // El servicio se finaliza a sí mismo cuando finaliza su
                                    // trabajo.
                                    try {
                                        // Simulamos trabajo de 10 segundos.
                                        Thread.sleep(10000);

                                        Log.i("Notificacion","Comienzo Notificacion");
                                        // Instanciamos e inicializamos nuestro manager.
                                        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                                getBaseContext())
                                                .setSmallIcon(R.mipmap.cuna)
                                                .setContentTitle("CRIARD")
                                                .setContentText("Cuna Mojada")
                                                .setWhen(System.currentTimeMillis());

                                        nManager.notify(12345, builder.build());
                                        Log.i("Notificacion","Fin Notificacion");
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                        }
                        if(estandoLLando >= 0){
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    // El servicio se finaliza a sí mismo cuando finaliza su
                                    // trabajo.
                                    try {
                                        // Simulamos trabajo de 10 segundos.
                                        Thread.sleep(10000);

                                        Log.i("Notificacion","Comienzo Notificacion");
                                        // Instanciamos e inicializamos nuestro manager.
                                        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                                getBaseContext())
                                                .setSmallIcon(R.mipmap.cuna)
                                                .setContentTitle("CRIARD")
                                                .setContentText("Bebe LLorando")
                                                .setWhen(System.currentTimeMillis());

                                        nManager.notify(12345, builder.build());
                                        Log.i("Notificacion","Fin Notificacion");
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                        }
                        msg_servo.arg1 = GET_RESPUESTA;
                        extra_servo.putString(RESULTPATH, recDataString.toString());
                        msg_servo.setData(extra_servo);
                        try {

                            outMessenger.send(msg_servo);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    recDataString.delete(0, recDataString.length());

                }

            }
        };
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}