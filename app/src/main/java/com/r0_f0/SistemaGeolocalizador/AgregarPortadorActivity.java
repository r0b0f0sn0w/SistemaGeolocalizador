package com.r0_f0.SistemaGeolocalizador;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AgregarPortadorActivity extends AppCompatActivity {
    private CameraSource cameraSource;
    private String idportador = "";
    private ProgressDialog cuadroDialogo;
    private SurfaceView cameraView;
    private String QR;
    private String id = "";
    public AlertDialog dialogoAgregarportador;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private String token = "";
    private String tokenanterior = "";
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_portador);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        String idu = getIntent().getStringExtra("userid");
        String elQR = getIntent().getStringExtra("QR");
        id = idu;
        QR = elQR;
        cameraView = findViewById(R.id.camera_view);
        initQR();
        initMe();
    }

    public void initQR() {
        // creo el detector qr
        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();
        // creo la camara
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();
        // listener de ciclo de vida de la camara
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // verifico si el usuario dio los permisos para la camara
                if (ActivityCompat.checkSelfPermission(AgregarPortadorActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // verificamos la version de ANdroid que sea al menos la M para mostrar
                        // el dialog de la solicitud de la camara
                        if (shouldShowRequestPermissionRationale(
                                Manifest.permission.CAMERA)) ;
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                    return;
                } else {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        // preparo el detector de QR
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    token = barcodes.valueAt(0).displayValue;
                    if (!token.equals(tokenanterior)) {
                        tokenanterior = token;
                        //Log.i("token", token);
                        Boolean es = validarCodigo(token);
                        if (es.equals(true)) {
                            Vibrator v = (Vibrator) getSystemService(AgregarPortadorActivity.this.VIBRATOR_SERVICE);
                            v.vibrate(500);
                            //usar un hilo de ui para poder mostrar el alertdialog
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    frmAgregarPortador();
                                    cameraSource.stop();
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                public void run() {
                                    Vibrator v = (Vibrator) getSystemService(AgregarPortadorActivity.this.VIBRATOR_SERVICE);
                                    v.vibrate(300);
                                    v.vibrate(300);
                                    Toast.makeText(AgregarPortadorActivity.this, "Codigo no valido", Toast.LENGTH_LONG).show();
                                }
                            });
                        }//Cierra else
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    synchronized (this) {
                                        wait(2000);
                                        // limpiamos el token
                                        tokenanterior = "";
                                    }
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    Log.e("Error", "Waiting didnt work!!");
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }//cierra if
                }//Cierra if
            }
        });
    }//Cierra metodo de initQR

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

    //inicia el handler para mostrar los tost con el surface view activo
    private void initMe() {
        handler = new Handler();
    }

    private void frmAgregarPortador() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AgregarPortadorActivity.this);
        LayoutInflater inflador = getLayoutInflater();
        View vista = inflador.inflate(R.layout.frmagregarportador, null);
        builder.setView(vista);
        dialogoAgregarportador = builder.create();
        dialogoAgregarportador.show();
        final EditText txtAgregarPortadorNombre = vista.findViewById(R.id.txtAgregarPortadorNombre);
        final EditText txtAgregarPortadorAppPat = vista.findViewById(R.id.txtAgregarPortadorAppPat);
        final EditText txtAgregarPortadorAppMat = vista.findViewById(R.id.txtAgregarPortadorAppMat);
        final EditText txtAgregarPortadorTelefono = vista.findViewById(R.id.txtAgregarPortadorTelefono);
        final EditText txtAgregarPortadorTelefono2 = vista.findViewById(R.id.txtAgregarPortadorTelefono2);
        final EditText txtAgregarPortadorDireccion = vista.findViewById(R.id.txtAgregarPortadorDireccion);
        final EditText txtAgregarPortadorDescripcion = vista.findViewById(R.id.txtAgregarPortadorDescripcion);
        final EditText txtAgregarPortadorPadecimiento = vista.findViewById(R.id.txtAgregarPortadorPadecimiento);
        final Button btnAgregarPortador = vista.findViewById(R.id.btnAgregarPortador);
        final Button btnCancelarAgregarPortador = vista.findViewById(R.id.btnCancelarAgregarPortador);
        //traerInfoResponsable();

        btnAgregarPortador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtAgregarPortadorNombre.getText().toString().isEmpty() || txtAgregarPortadorAppPat.getText().toString().isEmpty() || txtAgregarPortadorAppMat.getText().toString().isEmpty() || txtAgregarPortadorTelefono.getText().toString().isEmpty() || txtAgregarPortadorTelefono2.getText().toString().isEmpty() || txtAgregarPortadorDireccion.getText().toString().isEmpty() || txtAgregarPortadorPadecimiento.getText().toString().isEmpty() || txtAgregarPortadorDescripcion.getText().toString().isEmpty()) {
                    Toast.makeText(AgregarPortadorActivity.this, "El formulario esta vacío", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    JSONObject jsonUpdate = new JSONObject();
                    try {
                        jsonUpdate.put("QR", QR);
                        jsonUpdate.put("idresp", id);
                        jsonUpdate.put("idportador", idportador);
                        jsonUpdate.put("nombre", txtAgregarPortadorNombre.getText().toString().trim());
                        jsonUpdate.put("appat", txtAgregarPortadorAppPat.getText().toString().trim());
                        jsonUpdate.put("apmat", txtAgregarPortadorAppMat.getText().toString().trim());
                        jsonUpdate.put("telefono", txtAgregarPortadorTelefono.getText());
                        jsonUpdate.put("telefono2", txtAgregarPortadorTelefono2.getText());
                        jsonUpdate.put("direccion", txtAgregarPortadorDireccion.getText());
                        jsonUpdate.put("padecimiento", txtAgregarPortadorPadecimiento.getText());
                        jsonUpdate.put("descripcion", txtAgregarPortadorDescripcion.getText());
                        new AgregarPortador().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/nuevoUsuarioPortador.php", jsonUpdate.toString());
                    } catch (JSONException e) {
                        Log.e("Error :c", "El error:\n" + e);
                    }//Cierra catch
                }//Cierra else
            }
        });
        btnCancelarAgregarPortador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraSource.stop();
            }
        });
    }
    private class AgregarPortador extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(AgregarPortadorActivity.this);
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
            try {
                JSONObject jsonObject=new JSONObject(result);
                JSONArray registro=jsonObject.getJSONArray("mensaje");
                for (int i=0;i<registro.length();i++){
                    JSONObject usuario=registro.getJSONObject(i);
                    if(usuario.getString("mensaje").equals("ok")){
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            Toast.makeText(AgregarPortadorActivity.this,"Usuario agregado correctamente",Toast.LENGTH_LONG).show();
                        }
                    }else if(
                            usuario.getString("mensaje").equals("no")){
                        if(cuadroDialogo.isShowing()){
                            cuadroDialogo.dismiss();
                            Toast.makeText(AgregarPortadorActivity.this,"No se actualizó :c",Toast.LENGTH_LONG).show();
                        }
                    }
                }//Cierra for
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AgregarPortadorActivity.this,"No "+e,Toast.LENGTH_LONG).show();
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            }
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
            }
        }//Cierra el postOnExecute
    }//Cierra la clase de Agregarportador
    private void traerInfoResponsable(){
        JSONObject jsonTraer = new JSONObject();
        try {
            jsonTraer.put("id_Resp", id);
            new traerInfoResponsable().execute("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/traerUsuarioResponsable.php", jsonTraer.toString());
        }catch(JSONException e){
        }//
    }//Cierra metodo para traer la info del responsable
    private class traerInfoResponsable extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            super.onPreExecute();
            cuadroDialogo=new ProgressDialog(AgregarPortadorActivity.this);
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
            try {
                JSONObject jsonObject=new JSONObject(result);
                JSONArray registro=jsonObject.getJSONArray("datos");
                String direccion="",tel="",tel2="";
                for (int i=0;i<registro.length();i++){
                    JSONObject usuario=registro.getJSONObject(i);
                    direccion=usuario.getString("direccion");
                    tel=usuario.getString("telefono");
                    tel2=usuario.getString("telefono2");
                    //idportador=usuario.getString("id_portador");
                }//Cierra for

                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
                if(direccion!=null){
                    //txtAgregarPortadorTelefono.setText(tel);
                    //txtAgregarPortadorTelefono2.setText(tel2);
                    //txtAgregarPortadorDireccion.setText(direccion);
                }//Cierra if
                else{
                    Toast.makeText(AgregarPortadorActivity.this,"Ha sucedido un error al recuperar su información",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AgregarPortadorActivity.this,"No se ha podido recuperar la informacion suya"+e,Toast.LENGTH_LONG).show();
                if(cuadroDialogo.isShowing()){
                    cuadroDialogo.dismiss();
                }
            }//Cierra catch
            if(cuadroDialogo.isShowing()){
                cuadroDialogo.dismiss();
            }
        }//Cierra el postOnExecute
    }//Cierra la clase de traerInfoResponsable
}//Cierra la clase agregarPortador
