package com.r0_f0.SistemaGeolocalizador;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private Button btnRegistrarse, btnRecuperarCuenta, btnLogin, btnLoginEscanear;
    private EditText txtLoginCorreo, txtLoginContrasenia;
    public ProgressDialog cuadroDialogo;
    private String id;
    public AlertDialog dialogoRegistrarse,dialogoRecuperarCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        txtLoginCorreo = findViewById(R.id.txtLogincorreo);
        txtLoginContrasenia = findViewById(R.id.txtLoginpassword);
        btnLogin = findViewById(R.id.btnLogin);
        //Boton iniciar sesion
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean nwcon;
                nwcon = isNetDisponible();
                if (nwcon.equals(false)) {
                    Toast.makeText(LoginActivity.this, "Parece que usted no tiene su conexión a la red activa", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (isOnlineNet().equals(false)) {
                        Toast.makeText(LoginActivity.this, "Usted no tiene conexión a internet por favor intente cuando tenga", Toast.LENGTH_LONG).show();
                    } else {

                        if (txtLoginCorreo.getText().toString().isEmpty() && txtLoginContrasenia.getText().toString().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Ingrese su correo electronico y contraseña", Toast.LENGTH_LONG).show();
                            return;
                        } else if (txtLoginCorreo.getText().toString().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Ingrese su correo", Toast.LENGTH_LONG).show();
                            return;
                        } else if (txtLoginContrasenia.getText().toString().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Ingrese su contraseña", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("correo", txtLoginCorreo.getText().toString());
                                json.put("password", txtLoginContrasenia.getText().toString());
                                new iniciarsesion().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/login.php", json.toString());
                            } catch (JSONException e) {
                                Log.i("Error", "El error:\n" + e);
                            }
                        }//CIerra else
                    }//Cierra else
                }//cierra primer else
            }
        });//Cierra e listener del boton login
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarFrmRegistro();
            }
        });
        btnRecuperarCuenta = (Button) findViewById(R.id.btnRecuperarCuenta);
        btnRecuperarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarFrmRecuperarCuenta();
            }
        });
        btnLoginEscanear = (Button) findViewById(R.id.btnLoginEscanear);
        btnLoginEscanear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intento = new Intent(LoginActivity.this, AgregarPortadorActivity.class);
                startActivity(intento);
            }
        });
    }//Cierra el onCreate
    /*Metodos y clases para el inicio de sesion*/
    private class iniciarsesion extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            cuadroDialogo = new ProgressDialog(LoginActivity.this);
            cuadroDialogo.setMessage("Iniciando sesion, espere un momento");
            cuadroDialogo.setCancelable(false);
            cuadroDialogo.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(strings[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(strings[1]);
                wr.flush();
                wr.close();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject json = new JSONObject(result);
                JSONArray datos=json.getJSONArray("usuario");
                String estado="";
                for (int i=0;i<datos.length();i++){
                    JSONObject login=datos.getJSONObject(i);
                    id=login.getString("id_usuarioResp");
                    estado=login.getString("estado");
                }
                if(estado!=null){
                    switch (estado){
                        case "0":
                            Toast.makeText(LoginActivity.this,"Su cuenta no se ha verificado",Toast.LENGTH_LONG).show();
                            if(cuadroDialogo.isShowing()){
                                cuadroDialogo.dismiss();
                            }
                            break;
                        case "1":
                            if(cuadroDialogo.isShowing()){
                                cuadroDialogo.dismiss();
                                Intent i=new Intent(LoginActivity.this, HomeActivity.class);
                                i.putExtra("userid",id);
                                i.putExtra("correo",txtLoginCorreo.getText().toString());
                                startActivity(i);
                                finish();
                            }
                            break;
                        case "2":
                            if(cuadroDialogo.isShowing()){
                                cuadroDialogo.dismiss();
                            }
                            Toast.makeText(LoginActivity.this,"Su cuenta esta desactivada",Toast.LENGTH_LONG).show();
                            break;
                        case "":
                            if(cuadroDialogo.isShowing()){
                                cuadroDialogo.dismiss();
                            }
                            Toast.makeText(LoginActivity.this,"Correo o contraseña incorrecta (s)",Toast.LENGTH_LONG).show();
                            break;
                    }//Cierra switch
                }//Cierra if
            } catch(JSONException e){
                Log.e("El error:\n",e.toString());
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                    Toast.makeText(LoginActivity.this,"Correo eletronico o contraseña incorrectos",Toast.LENGTH_LONG).show();
                }
            }
        }//Cierra el postOnExecute
    }//Cierra clase iniciarsesion

    /*Metodos y clases para la funcionalidad para la seccion de registro*/
    private void mostrarFrmRegistro(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflador = getLayoutInflater();
        View vista =inflador.inflate(R.layout.registrarse,null);
        builder.setView(vista);
        dialogoRegistrarse =builder.create();
        dialogoRegistrarse.show();
        final TextView txtRegistroNombre=vista.findViewById(R.id.txtRegistroNombre);
        final TextView txtRegistroAppPat=vista.findViewById(R.id.txtRegistroAppPat);
        final TextView txtRegistroAppMat=vista.findViewById(R.id.txtRegistroAppMat);
        final TextView txtRegistroTelefono=vista.findViewById(R.id.txtRegistroTelefono);
        final TextView txtRegistroTelefono2=vista.findViewById(R.id.txtRegistroTelefono2);
        final TextView txtRegistroDireccion=vista.findViewById(R.id.txtRegistroDireccion);
        final TextView txtRegistroCorreo=vista.findViewById(R.id.txtRegistroCorreo);
        final TextView txtRegistroPassword=vista.findViewById(R.id.txtRegistroPassword);
        final TextView txtRegistroPassword2=vista.findViewById(R.id.txtRegistroPassword2);
        Button btnFrmRegistrarse=vista.findViewById(R.id.btnFrmRegistrarse);
        Button btnFrmCancelar=vista.findViewById(R.id.btnFrmCancelar);

        btnFrmRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean nwcon;
                nwcon=isNetDisponible();
                if(nwcon.equals(false)){
                    Toast.makeText(LoginActivity.this,"Parece que usted no tiene su conexión a la red activa",Toast.LENGTH_LONG).show();
                    return;
                }else{
                    if(isOnlineNet().equals(false)){
                        Toast.makeText(LoginActivity.this,"Usted no tiene conexión a internet porfavor intente cuando tenga",Toast.LENGTH_LONG).show();
                    }else{
                        String validacion;
                        validacion=validarFRM(txtRegistroNombre.getText().toString(),txtRegistroAppPat.getText().toString(),txtRegistroAppMat.getText().toString(),txtRegistroTelefono.getText().toString(),txtRegistroTelefono2.getText().toString(),txtRegistroDireccion.getText().toString(),txtRegistroCorreo.getText().toString(),txtRegistroPassword.getText().toString(),txtRegistroPassword2.getText().toString());
                        switch (validacion){
                            case "FrmVacio":
                                Toast.makeText(LoginActivity.this,"El formulario a sido lleno parcialente",Toast.LENGTH_LONG).show();
                                break;
                            case "RevisacC":
                                Toast.makeText(LoginActivity.this,"Las contraseñas no coinciden",Toast.LENGTH_LONG).show();
                                break;
                            case "Correcto":
                                JSONObject json= new JSONObject();
                                try {
                                    json.put("nombre",txtRegistroNombre.getText().toString());
                                    json.put("apepat",txtRegistroAppPat.getText().toString());
                                    json.put("apemat",txtRegistroAppMat.getText().toString());
                                    json.put("telefono",txtRegistroTelefono.getText().toString());
                                    json.put("telefono2",txtRegistroTelefono2.getText().toString());
                                    json.put("direccion",txtRegistroDireccion.getText().toString());
                                    json.put("correo_electronico",txtRegistroCorreo.getText().toString());
                                    json.put("password",txtRegistroPassword.getText().toString());
                                    new registrarse().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/crearUsuarioResponsable.php",json.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    dialogoRegistrarse.dismiss();
                                }
                                Toast.makeText(LoginActivity.this,"Iniciando registro",Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(LoginActivity.this,"Rellene el formulario completamente",Toast.LENGTH_LONG).show();
                                break;
                        }//Cierra switch
                    }//Cierra else
                }//cierra promer else
            }//Cierra onClick
        });
        btnFrmCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRegistrarse.dismiss();
            }
        });
        //Toast.makeText(LoginActivity.this,"Funciona",Toast.LENGTH_LONG).show();
    }//Cierra el metodo para mostrar el formulario de registro
    private class registrarse extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(LoginActivity.this);
            cuadroDialogo.setMessage("Verificando estado de la cuenta, por favor espere");
            cuadroDialogo.setCancelable(false);
            cuadroDialogo.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(strings[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(strings[1]);
                wr.flush();
                wr.close();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject json=new JSONObject(result);
                JSONArray registros=json.getJSONArray("mensaje");
                String msj="";
                for (int i=0;i<registros.length();i++){
                    JSONObject respuesta=registros.getJSONObject(i);
                    msj=respuesta.getString("mensaje");
                }
                switch (msj){
                    case "ok":
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            dialogoRegistrarse.dismiss();
                            Toast.makeText(LoginActivity.this,"Se ha registrado correctamente",Toast.LENGTH_LONG).show();
                        }
                        break;
                    case "no":
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            Toast.makeText(LoginActivity.this,"Error al registrase intente de nuevo más tarde",Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            } catch(JSONException e){
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                    Toast.makeText(LoginActivity.this,"No se ha podido enviar el correo de recuperacion, verifique su conexion a internet "+e,Toast.LENGTH_LONG).show();
                }
            }
        }//Cierra el postOnExecute
    }//Cierrra clase registrarse

    /*Metodos y clases para la funcionalidad para la seccion de recuperar cuenta*/
    private void mostrarFrmRecuperarCuenta(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflador = getLayoutInflater();
        View vista =inflador.inflate(R.layout.recuperarcuenta,null);
        builder.setView(vista);
        final AlertDialog dialogoRecuperarCuenta =builder.create();
        dialogoRecuperarCuenta.show();
        Button btnFrmRegistrarse=vista.findViewById(R.id.btnRecuperarCuenta);
        btnFrmRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,"Funciona",Toast.LENGTH_LONG).show();
                dialogoRecuperarCuenta.dismiss();
            }
        });
    }//Cierra el metodo para mostrar el formulario de registro
    private String validarFRM(String n, String Ap, String Am, String t, String t2, String dir, String email, String pass,String pass2) {
        String v = "";
        if (n.isEmpty()|| Ap.isEmpty() || Am.isEmpty()|| t.isEmpty() || t2.isEmpty()|| dir.isEmpty()|| email.isEmpty()|| pass.isEmpty()|| pass2.isEmpty()) {
            v="FrmVacio";
        } else {
            if(pass.equals(pass2)){
                v = "Correcto";
            }else{
                v="RevisacC";
            }
        }//cierra else
        return v;
    }//cierra metodo validarFRM

    /*Metodos para la validadcion del dispositivo*/
    /*Metodo para validar si el dispositivo tiene acceso a internet*/
    public static Boolean isOnlineNet() {
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }//Cierra metodo isOnlineNet
    /* Metodo para validar si el dispositivo tiene la red activa*/
    public boolean isNetDisponible() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();
        return (actNetInfo != null && actNetInfo.isConnected());
    }//Cierra isNetDisponible
}//Cierra la clase LoginActivity