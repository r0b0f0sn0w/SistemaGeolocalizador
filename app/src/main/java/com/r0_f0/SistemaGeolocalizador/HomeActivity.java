package com.r0_f0.SistemaGeolocalizador;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.r0_f0.SistemaGeolocalizador.Entidades.UsuarioPortador;

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
    protected ArrayList <UsuarioPortador> listausuarios;
    private String idusuario,elcorreo="";
    AdaptadorPortadores a=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        String idusuario= getIntent().getStringExtra("userid");
        String elcorreo= getIntent().getStringExtra("correo");
        recyclerPortadores=findViewById(R.id.recyclerPortadores);
        recyclerPortadores.setLayoutManager(new LinearLayoutManager(this));

        JSONObject json = new JSONObject();
        try{
            json.put("usuario",idusuario);
            new TraerPortadores().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/traerUsuariosPortadores.php", json.toString());
        } catch (JSONException e) {

        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }//Cierra onCreate

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
        final EditText txtConfiguracionNombre=vista.findViewById(R.id.txtConfiguracionNombre);
        txtConfiguracionNombre.setEnabled(false);
        final EditText txtConfiguracionAppPat=vista.findViewById(R.id.txtConfiguracionAppPat);
        txtConfiguracionAppPat.setEnabled(false);
        final EditText txtConfiguracionAppMat=vista.findViewById(R.id.txtConfiguracionAppMat);
        txtConfiguracionAppMat.setEnabled(false);
        final EditText txtConfiguracionTelefono=vista.findViewById(R.id.txtConfiguracionTelefono);
        txtConfiguracionTelefono.setEnabled(false);
        final EditText txtConfiguracionTelefono2=vista.findViewById(R.id.txtConfiguracionTelefono2);
        txtConfiguracionTelefono2.setEnabled(false);
        final EditText txtConfiguracionDireccion=vista.findViewById(R.id.txtConfiguracionDireccion);
        txtConfiguracionDireccion.setEnabled(false);
        btnConfEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtConfiguracionNombre.setEnabled(true);
                txtConfiguracionAppPat.setEnabled(true);
                txtConfiguracionAppMat.setEnabled(true);
                txtConfiguracionTelefono.setEnabled(true);
                txtConfiguracionTelefono2.setEnabled(true);
                txtConfiguracionDireccion.setEnabled(true);
                btnConfCambiosGuardar.setVisibility(View.VISIBLE);
                btnConfEditar.setVisibility(View.INVISIBLE);
            }
        });
    }
}//Cierra la calse HomeActivity
