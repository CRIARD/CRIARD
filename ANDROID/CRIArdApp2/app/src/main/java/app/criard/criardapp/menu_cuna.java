package app.criard.criardapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import android.os.Handler;
import android.widget.Toast;

import app.criard.criardapp.InterfazAsyntask;
import app.criard.criardapp.ClienteHttp_GET;
public class menu_cuna extends Activity implements InterfazAsyntask{

    private ImageButton btn_led;
    private ImageButton btn_micro;
    private ImageButton btn_servo;
    private String ruta;
    private ClienteHttp_GET threadClienteGet;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_cuna);

        Bundle bundle = getIntent().getExtras();
        ruta =(String) bundle.get("ruta");
        btn_led =  (ImageButton)findViewById(R.id.btn_led);
        btn_micro =  (ImageButton)findViewById(R.id.btn_micro);
        btn_servo =  (ImageButton)findViewById(R.id.btn_servo);
        btn_led.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener =  new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_led:
                     String uri =  ruta + "led=1";
                     iniciarPeticion(uri);
                     break;

                case R.id.btn_micro:
                case R.id.btn_servo:

            }
        }
    };

    public void iniciarPeticion(final String uri){

        Toast.makeText(getApplicationContext(),"Iniciando peticion " + uri + " ...",Toast.LENGTH_SHORT).show();
        //Se crea y ejecuta un Thread que envia una peticion POST al servidor para que encienda el led
        threadClienteGet = new ClienteHttp_GET(menu_cuna.this);
        threadClienteGet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri);

        //Toast.makeText(getApplicationContext(),"Encendido",Toast.LENGTH_SHORT).show();
    }

    //Metodo que es llamada cuando se cierra la APP
    protected void onDestroy()
    {
        super.onDestroy();

        //Cuando se cierra la APP se detiene la recepcion de los msjs del Servidor
        handler.removeCallbacksAndMessages(null);


        Log.i("LOG","Recepcion de msjs finalizada");
        Toast.makeText(getApplicationContext(),"Cerrando Aplicacion...",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

}
