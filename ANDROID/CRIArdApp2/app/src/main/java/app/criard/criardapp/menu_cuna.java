package app.criard.criardapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import android.os.Handler;
import android.widget.Switch;
import android.widget.Toast;

import app.criard.criardapp.InterfazAsyntask;
import app.criard.criardapp.ClienteHttp_GET;
public class menu_cuna extends Activity implements InterfazAsyntask{

    private Switch btn_led;
    private Switch btn_micro;
    private Switch btn_servo;
    private String ruta;
    private String uri;
    private ClienteHttp_GET threadClienteGet;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_cuna);

        Bundle bundle = getIntent().getExtras();
        ruta =(String) bundle.get("ruta");
        btn_led =  (Switch) findViewById(R.id.btn_led);
        btn_micro =  (Switch)findViewById(R.id.btn_micro);
        btn_servo =  (Switch)findViewById(R.id.btn_servo);
        btn_led.setOnCheckedChangeListener(checkedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()){
                case R.id.btn_led:
                    if(isChecked){
                        uri =  ruta + "led=1";
                        iniciarPeticion(uri);
                    }else{
                        uri =  ruta + "led=0";
                        iniciarPeticion(uri);
                    }
                    break;

                case R.id.btn_micro:
                    break;
                case R.id.btn_servo:

            }
        }
    };
    public void iniciarPeticion(final String uri){

        Toast.makeText(getApplicationContext(),"Iniciando peticion ...",Toast.LENGTH_SHORT).show();
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
