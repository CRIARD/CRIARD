package app.criard.criardapp;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityInicial extends Activity {

    EditText ip;
    EditText puerto;
    Intent intent;
    Button boton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);
        ip = (EditText) findViewById(R.id.txt_ip);
        puerto = (EditText) findViewById(R.id.txt_puerto);
        boton = (Button) findViewById(R.id.btn_iniciar);
        boton.setOnClickListener(this.Conectar);
    }

    private View.OnClickListener Conectar = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            intent = new Intent(ActivityInicial.this,CRIArdMainActivity.class);

            intent.putExtra("ip",ip.getText().toString());
            intent.putExtra("puerto",puerto.getText().toString());

            startActivity(intent);

            finish();
        }
    };

}
