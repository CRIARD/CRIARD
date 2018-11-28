package app.criard.criardapp;

import org.json.JSONException;
import org.json.JSONObject;

public interface InterfazAsyntask {

    void mostrarToastMake(String msg);
    void construirRespuesta(String input) throws JSONException;
}
