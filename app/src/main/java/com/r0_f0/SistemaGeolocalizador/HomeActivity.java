package com.r0_f0.SistemaGeolocalizador;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.r0_f0.SistemaGeolocalizador.Entidades.UsuarioPortador;
import com.r0_f0.SistemaGeolocalizador.Entidades.UsuarioResponsable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ProgressDialog cuadroDialogo;
    protected RecyclerView recyclerPortadores;
    private UsuarioResponsable usuarioResponsable =new UsuarioResponsable();
    protected ArrayList <UsuarioPortador> listausuarios;
    private Switch swDesactivarCuenta;
    private String idusuario;
    private String QR;
    private Handler handler = new Handler();
    private EditText txtConfiguracionNombre,txtConfiguracionAppMat,txtConfiguracionAppPat,txtConfiguracionTelefono,txtConfiguracionTelefono2,txtConfiguracionDireccion;
    AdaptadorPortadores a=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        idusuario= getIntent().getStringExtra("userid");
        String elcorreo= getIntent().getStringExtra("correo");
        recyclerPortadores=findViewById(R.id.recyclerPortadores);
        recyclerPortadores.setLayoutManager(new LinearLayoutManager(this));
        JSONObject json = new JSONObject();
        try{
            json.put("usuario",idusuario);
            new TraerPortadores().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/traerUsuariosPortadores.php", json.toString());
        } catch (JSONException e) {
            Toast.makeText(HomeActivity.this,"Ha sucedido un error :'(",Toast.LENGTH_LONG).show();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(HomeActivity.this).initiateScan();
            }
        });
    }//Cierra onCreate
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
            if (result.getContents() != null){
                Boolean es = validarCodigo(result.getContents());
                if (es.equals(true)) {
                    Vibrator v = (Vibrator) getSystemService(HomeActivity.this.VIBRATOR_SERVICE);
                    v.vibrate(500);
                    JSONObject jsvalidar = new JSONObject();
                    try{
                        jsvalidar.put("QR",QR);
                        new traerUsuarioPortador().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/validarUsuarioPortador.php", jsvalidar.toString());
                    } catch (JSONException e) {
                        Toast.makeText(HomeActivity.this,"Ha sucedido un error :'(",Toast.LENGTH_LONG).show();
                    }

                    /*usar un hilo de ui para poder mostrar el alertdialog
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(HomeActivity.this, "Codigo valido", Toast.LENGTH_LONG).show();
                            //mostrarFormularioRegistroPortadores();
                        }
                    });*/
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            Vibrator v = (Vibrator) getSystemService(HomeActivity.this.VIBRATOR_SERVICE);
                            v.vibrate(300);
                            v.vibrate(300);
                            Toast.makeText(HomeActivity.this, "Codigo no valido", Toast.LENGTH_LONG).show();
                        }
                    });
                }//Cierra else
            }else{
                //tvBarCode.setText("Error al escanear el código de barras");
            }
    }
    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.confCuenta) {
            mostrarConf();
        }
        return true;
    }
    private class TraerPortadores extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(HomeActivity.this);
            cuadroDialogo.setMessage("Recuperando su informacion, espere un momento");
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

            UsuarioPortador user=null;
            listausuarios=new ArrayList<>();
            try {
                JSONObject jsonObject=new JSONObject(result.replaceAll("[^\\x00-\\x7F]", ""));
                // Identificar por el nodo padre
                JSONArray registros=jsonObject.getJSONArray("usuarios");
                for(int i=0; i<=registros.length(); i++){
                    user=new UsuarioPortador();
                    JSONObject usuario=registros.getJSONObject(i);
                    user.setIdPortador(usuario.getString("up_telefono").toString());
                    user.setNombre(usuario.getString("up_nombre").toString());
                    user.setAppPat(usuario.getString("up_apellidopat").toString());
                    listausuarios.add(user);
                    a =new AdaptadorPortadores(listausuarios);
                    recyclerPortadores.setAdapter(a);
                    Log.e("cosasa","longutid del json Array"+registros.length()+" Longitud de la lista de usuarios"+listausuarios.size());
                }

                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(HomeActivity.this,"No "+e,Toast.LENGTH_LONG).show();
                Log.e("salio",""+e);
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            }
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
            }

        }//Cierra el postOnExecute
    }//Cierra la clase de consultar usuarios portadores
    private void mostrarConf(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflador = getLayoutInflater();
        final View vista =inflador.inflate(R.layout.configuraciondecuenta,null);
        builder.setView(vista);
        final AlertDialog dialogoRecuperarCuenta =builder.create();
        dialogoRecuperarCuenta.show();
        final Button btnConfEditar=vista.findViewById(R.id.btnConfEditar);
        final Button btnConfCambiosGuardar=vista.findViewById(R.id.btnConfCambiosGuardar);
        final Button btnConfCambiosCancelar=vista.findViewById(R.id.btnConfCambiosCancelar);
        txtConfiguracionNombre=vista.findViewById(R.id.txtConfiguracionNombre);
        swDesactivarCuenta=vista.findViewById(R.id.swDesactivarCuenta);
        txtConfiguracionNombre.setEnabled(false);
        txtConfiguracionAppPat=vista.findViewById(R.id.txtConfiguracionAppPat);
        txtConfiguracionAppPat.setEnabled(false);
        txtConfiguracionAppMat=vista.findViewById(R.id.txtConfiguracionAppMat);
        txtConfiguracionAppMat.setEnabled(false);
        txtConfiguracionTelefono=vista.findViewById(R.id.txtConfiguracionTelefono);
        txtConfiguracionTelefono.setEnabled(false);
        txtConfiguracionTelefono2=vista.findViewById(R.id.txtConfiguracionTelefono2);
        txtConfiguracionTelefono2.setEnabled(false);
        txtConfiguracionDireccion=vista.findViewById(R.id.txtConfiguracionDireccion);
        txtConfiguracionDireccion.setEnabled(false);
        JSONObject jsonUpdate = new JSONObject();
        try {
            jsonUpdate.put("id", idusuario);
            new traerUsuarioResp().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/traerUsrResp.php", jsonUpdate.toString());
        }catch(JSONException e){
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
                Toast.makeText(HomeActivity.this,"Ha sucesido un error al desactivar su cuenta intente mas tarde por favor",Toast.LENGTH_LONG).show();
            }
        }
        btnConfEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtConfiguracionNombre.setEnabled(true);
                txtConfiguracionAppPat.setEnabled(true);
                txtConfiguracionAppMat.setEnabled(true);
                txtConfiguracionTelefono.setEnabled(true);
                txtConfiguracionTelefono2.setEnabled(true);
                txtConfiguracionDireccion.setEnabled(true);
                usuarioResponsable.setNombre(txtConfiguracionNombre.getText().toString());
                usuarioResponsable.setApppat(txtConfiguracionAppPat.getText().toString());
                usuarioResponsable.setAppmat(txtConfiguracionAppMat.getText().toString());
                usuarioResponsable.setTelefono(txtConfiguracionTelefono.getText().toString());
                usuarioResponsable.setTelefono2(txtConfiguracionTelefono2.getText().toString());
                usuarioResponsable.setDireccion(txtConfiguracionDireccion.getText().toString());
                btnConfCambiosGuardar.setVisibility(View.VISIBLE);
                btnConfEditar.setVisibility(View.INVISIBLE);
                btnConfCambiosCancelar.setVisibility(View.VISIBLE);
            }
        });
        btnConfCambiosCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtConfiguracionNombre.setEnabled(false);
                txtConfiguracionAppPat.setEnabled(false);
                txtConfiguracionAppMat.setEnabled(false);
                txtConfiguracionTelefono.setEnabled(false);
                txtConfiguracionTelefono2.setEnabled(false);
                txtConfiguracionDireccion.setEnabled(false);
                txtConfiguracionNombre.setText(usuarioResponsable.getNombre());
                txtConfiguracionAppPat.setText(usuarioResponsable.getApppat());
                txtConfiguracionAppMat.setText(usuarioResponsable.getAppmat());
                txtConfiguracionTelefono.setText(usuarioResponsable.getTelefono());
                txtConfiguracionTelefono2.setText(usuarioResponsable.getTelefono2());
                txtConfiguracionDireccion.setText(usuarioResponsable.getDireccion());
                btnConfCambiosGuardar.setVisibility(View.INVISIBLE);
                btnConfCambiosCancelar.setVisibility(View.INVISIBLE);
                btnConfEditar.setVisibility(View.VISIBLE);
            }
        });
        btnConfCambiosGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtConfiguracionNombre.getText().toString().isEmpty()||txtConfiguracionAppPat.getText().toString().isEmpty()||txtConfiguracionAppMat.getText().toString().isEmpty()||txtConfiguracionTelefono.getText().toString().isEmpty()||txtConfiguracionTelefono2.getText().toString().isEmpty()||txtConfiguracionDireccion.getText().toString().isEmpty()){
                    Toast.makeText(HomeActivity.this,"Usted no ha llenado sus datos",Toast.LENGTH_LONG).show();
                    return;
                }else{
                    JSONObject jsonUpdate = new JSONObject();
                    try{
                        jsonUpdate.put("id",idusuario);
                        jsonUpdate.put("nombre",txtConfiguracionNombre.getText());
                        jsonUpdate.put("apepat",txtConfiguracionAppPat.getText());
                        jsonUpdate.put("apemat",txtConfiguracionAppMat.getText());
                        jsonUpdate.put("telefono",txtConfiguracionTelefono.getText());
                        jsonUpdate.put("telefono2",txtConfiguracionTelefono2.getText());
                        jsonUpdate.put("direccion",txtConfiguracionDireccion.getText());
                        new guardarResponsable().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/actualizarUsuarioResponsable.php", jsonUpdate.toString());
                    } catch (JSONException e) {
                        Toast.makeText(HomeActivity.this,"No se ha podido guardar su información, intente más tarde",Toast.LENGTH_LONG).show();
                        Log.e("Error :c","El error:\n"+e);
                    }//Cierra catch
                }//Cierra else
            }
        });
    }//Cierra metodo para mostrar la configuracion
    public void onclick(View view) {
        if(view.getId()==R.id.swDesactivarCuenta){
            if(swDesactivarCuenta.isChecked()){
                mostrarAdvertencia(idusuario);
                Toast.makeText(HomeActivity.this,"Habilitado",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(HomeActivity.this,"No habilitado",Toast.LENGTH_LONG).show();
            }
        }
    }//Cierra metodo onclick de un switch
    private void mostrarAdvertencia(final String currentid){
        final android.support.v7.app.AlertDialog.Builder cuadro=new android.support.v7.app.AlertDialog.Builder(this);
        cuadro.setMessage("¿Desea descativar si cuenta?\nAl hacer esto usted debera reactivarla via email y su sesion actial se cerrará");
        cuadro.setCancelable(false);
        cuadro.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                swDesactivarCuenta.setChecked(true);
                JSONObject jsonUpdate = new JSONObject();
                try {
                    jsonUpdate.put("id", currentid);
                    new desactivarCuenta().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/desactivarUsuarioResp.php", jsonUpdate.toString());
                }catch(JSONException e){
                    if(cuadroDialogo.isShowing()){
                        cuadroDialogo.dismiss();
                        Toast.makeText(HomeActivity.this,"Ha sucesido un error al desactivar su cuenta intente mas tarde porfavor",Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(HomeActivity.this,"Ha sucesido un error al desactivar su cuenta intente mas tarde porfavor",Toast.LENGTH_LONG).show();
                }
            }
        });
        cuadro.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                swDesactivarCuenta.setChecked(false);
            }
        });
        cuadro.show();
    }//Cierra metodo para mostrar advertencia
    private class desactivarCuenta extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(HomeActivity.this);
            cuadroDialogo.setMessage("Desactivando su cuenta, por favor espere");
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
                JSONObject jsonObject=new JSONObject(result.replaceAll("[^\\x00-\\x7F]", ""));
                JSONArray registro=jsonObject.getJSONArray("mensaje");
                for (int i=0;i<registro.length();i++){
                    JSONObject usuario=registro.getJSONObject(i);
                    if(usuario.getString("mensaje").equals("ok")){
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            Toast.makeText(HomeActivity.this,"Cuenta desactivada correctamente",Toast.LENGTH_LONG).show();
                            Intent intento = new Intent(HomeActivity.this, LoginActivity.class);
                            startActivity(intento);
                            finish();
                        }
                    }else if(
                            usuario.getString("mensaje").equals("no")){
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            Toast.makeText(HomeActivity.this,"No se actualizó :c",Toast.LENGTH_LONG).show();
                        }
                    }
                }//Cierra for
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(HomeActivity.this,"No "+e,Toast.LENGTH_LONG).show();
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            }
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
            }
        }//Cierra el postOnExecute
    }//Cierra la clase desactivarresponsable
    private class traerUsuarioResp extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(HomeActivity.this);
            cuadroDialogo.setMessage("Recuperando su información, por favor espere");
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
                JSONObject jsonObject=new JSONObject(result.replaceAll("[^\\x00-\\x7F]", ""));
                JSONArray registro=jsonObject.getJSONArray("datos");
                String nombre="",apppat="",apmat="",telefono="",telefono2="",direccion="";
                for (int i=0;i<registro.length();i++){
                    JSONObject usuario=registro.getJSONObject(i);
                    nombre=usuario.getString("nombre");
                    apppat=usuario.getString("apellidoPat");
                    apmat=usuario.getString("apellidoMat");
                    telefono=usuario.getString("telefono");
                    telefono2=usuario.getString("telefono2");
                    direccion=usuario.getString("direccion");
                }//Cierra for
                txtConfiguracionNombre.setText(nombre);
                txtConfiguracionAppPat.setText(apppat);
                txtConfiguracionAppMat.setText(apmat);
                txtConfiguracionTelefono.setText(telefono);
                txtConfiguracionTelefono2.setText(telefono2);
                txtConfiguracionDireccion.setText(direccion);
                usuarioResponsable.setNombre(nombre);
                usuarioResponsable.setApppat(apppat);
                usuarioResponsable.setAppmat(apmat);
                usuarioResponsable.setTelefono(telefono);
                usuarioResponsable.setTelefono2(telefono2);
                usuarioResponsable.setDireccion(direccion);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(HomeActivity.this,"Ha sucedido un problema al recuperar su informacion intente más tarde "+e,Toast.LENGTH_LONG).show();
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            }
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
            }
        }//Cierra el postOnExecute
    }//Cierra la clase de traerusuarioResponsable

    private class traerUsuarioPortador extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(HomeActivity.this);
            cuadroDialogo.setMessage("Validando codigo, por favor espere");
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
                JSONObject jsonObject=new JSONObject(result.replaceAll("[^\\x00-\\x7F]", ""));
                JSONArray registro=jsonObject.getJSONArray("usuario");
                String usr="";
                for (int i=0;i<registro.length();i++){
                    JSONObject usuario=registro.getJSONObject(i);
                    usr=usuario.getString("id_usuarioResp");
                }//Cierra for
                if(usr.equals("54")){
                    mostrarFormularioRegistroPortadores();
                }else{
                    Toast.makeText(HomeActivity.this,"Este codigo no esta disponible intente con otro",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(HomeActivity.this,"Ha sucedido un problema al recuperar su informacion intente más tarde "+e,Toast.LENGTH_LONG).show();
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            }
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
            }
        }//Cierra el postOnExecute
    }//Cierra la clase de traerusuarioResponsable
    private class guardarResponsable extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(HomeActivity.this);
            cuadroDialogo.setMessage("Actualizando sus datos, por favor espere");
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
                JSONObject jsonObject=new JSONObject(result.replaceAll("[^\\x00-\\x7F]", ""));
                JSONArray registro=jsonObject.getJSONArray("mensaje");
                for (int i=0;i<registro.length();i++){
                    JSONObject usuario=registro.getJSONObject(i);
                    if(usuario.getString("mensaje").equals("ok")){
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            Toast.makeText(HomeActivity.this,"Datos actualizados correctamente",Toast.LENGTH_LONG).show();
                            Intent intento = new Intent(HomeActivity.this, HomeActivity.class);
                            intento.putExtra("userid",idusuario);
                            startActivity(intento);
                        }
                    }else if(
                            usuario.getString("mensaje").equals("no")){
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            Toast.makeText(HomeActivity.this,"No se actualizaron sus datos",Toast.LENGTH_LONG).show();
                        }
                    }
                }//Cierra for
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(HomeActivity.this,"No se ha podido actialzar su información"+e,Toast.LENGTH_LONG).show();
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            }
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
            }
        }//Cierra el postOnExecute
    }//Cierra la clase guardar al portador

    private void mostrarFormularioRegistroPortadores(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflador = getLayoutInflater();
        final View vista =inflador.inflate(R.layout.frmagregarportador,null);
        builder.setView(vista);
        final AlertDialog dialogoRecuperarCuenta =builder.create();
        dialogoRecuperarCuenta.show();
    }
    private Boolean validarCodigo(String s) {
        Boolean validacion;
        String[] partes = s.split("=");
        if (partes[0].equals("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/LeerEncontrado.php?QR")) {
            validacion = true;
            QR = partes[1];
            Log.i("Si", "Si valida: " + QR);
        } else {
            validacion = false;
            Log.i("No", "No valida");
        }
        return validacion;
    }//Cierra el metyodo de validacion del QR
}//Cierra la calse HomeActivity
