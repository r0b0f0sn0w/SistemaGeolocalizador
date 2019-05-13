package com.r0_f0.SistemaGeolocalizador;

import android.Manifest;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class AgregarPortadorActivity extends AppCompatActivity {
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private String token = "";
    private String tokenanterior = "";
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_portador);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        cameraView =findViewById(R.id.camera_view);
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
                    token = barcodes.valueAt(0).displayValue.toString();
                    if (!token.equals(tokenanterior)) {
                        tokenanterior = token;
                        //Log.i("token", token);
                        Boolean es=validarCodigo(token);
                        if(es.equals(true)){
                            Vibrator v = (Vibrator) getSystemService(AgregarPortadorActivity.this.VIBRATOR_SERVICE);
                            v.vibrate(500);
                        }else{
                            handler.post(new Runnable(){
                                public void run(){
                                    Vibrator v = (Vibrator) getSystemService(AgregarPortadorActivity.this.VIBRATOR_SERVICE);
                                    v.vibrate(300);
                                    v.vibrate(300);
                                    Toast.makeText(AgregarPortadorActivity.this, "Codigo Qr no valido", Toast.LENGTH_LONG).show();
                                }
                            });
                        }//Cierra else
                            /*Intent i = new Intent(AgregarPortadorActivity.this,MostrarDatosLocalizadoActivity.class);
                            i.putExtra("QR",token);
                            startActivity(i);
                            finish();*/
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
    private Boolean validarCodigo(String s){
        Boolean validacion=false;
        String[] partes = s.split("=");
        if(partes[0].equals("https://raesaldro.000webhostapp.com/WebServicesGeolocalizador/LeerEncontrado.php?QR")){
            validacion=true;
            //Log.i("Si","Si valida");
        }else{
            validacion=false;
            //Log.i("No","No valida");
        }
        return validacion;
    }//Cierra el metyodo de validacion del QR
    //inicia el handler para mostrar los tost con el surface view activo
    private void initMe()
    {
        handler = new Handler();
    }
}//Cierra la clase agregarPortador
