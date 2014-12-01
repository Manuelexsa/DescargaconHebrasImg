package com.example.kronos.descargarimagen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;


public class Principal extends Activity {

    private EditText etURL,et1;
    private ImageView imagen;
    private EditText etNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        imagen = (ImageView)findViewById(R.id.ivImagen);
        etURL = (EditText)findViewById(R.id.etUrl);
        etNombre = (EditText)findViewById(R.id.etNombre);
    }

    public void guardar(View view) {
        Hebra h = new Hebra();
        h.execute();
    }

    private class Hebra extends AsyncTask<Object, Objects, File> {

        private URL url;
        private String dir;
        private ProgressDialog dialogo;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!etURL.getText().toString().equals("")) {
                imagen.setImageBitmap(null);
                dir = etURL.getText().toString();
                if(!dir.startsWith(getString(R.string.protocolo))){
                    dir = getString(R.string.protocolo) + dir;
                }
                url = null;
                try {
                    url = new URL(dir);
                } catch(MalformedURLException e) {
                    Toast.makeText(Principal.this, getString(R.string.errorUrl), Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(Principal.this,getString(R.string.urlVacia), Toast.LENGTH_SHORT).show();
            }
            DialogoProgreso();
        }

        @Override
        protected File doInBackground(Object... objects) {
            if(dir != null && url != null) {
                File f = buscaFichero();
                if(f!=null) {
                    boolean correcto = descargarImagen(f, url);
                    if (correcto) {
                        return f;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(File f) {
            super.onPostExecute(f);
            dialogo.dismiss();
            if((f != null) && (f.exists())) {
                Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                imagen.setImageBitmap(myBitmap);
            } else{
                Toast.makeText(Principal.this, getString(R.string.errorGuardar), Toast.LENGTH_SHORT).show();
            }
        }
        private boolean descargarImagen(File f, URL url) {
            try {
                URLConnection urlCon = url.openConnection();
                InputStream is = urlCon.getInputStream();
                FileOutputStream fichero = new FileOutputStream(f);
                byte[] bufferTemp = new byte[1000];
                int temp = is.read(bufferTemp);
                while(temp > 0) {
                    fichero.write(bufferTemp, 0, temp);
                    temp = is.read(bufferTemp);
                }
                is.close();
                fichero.close();
            } catch(IOException e) {
                return false;
            }

            return true;
        }

        private File buscaFichero() {
            String nombre = dir.split("/")[(dir.split("/").length - 1)];
            String extension = "";
            int i = nombre.lastIndexOf('.');
            if (i > 0) {
                extension = nombre.substring(i + 1);
            }
            if(extension.equalsIgnoreCase(getString(R.string.jpeg)) ||
                    extension.equalsIgnoreCase(getString(R.string.png)) ||
                    extension.equalsIgnoreCase(getString(R.string.jpg)) ||
                    extension.equalsIgnoreCase(getString(R.string.gif))){
                if (!etNombre.getText().toString().equals("")) {
                    nombre = etNombre.getText().toString() + "." + extension;
                }
                RadioButton externaPrivada = (RadioButton) findViewById(R.id.ePrivada);
                if (externaPrivada.isChecked()) {
                    return new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), nombre);
                }
                return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), nombre);
            }
            return null;
        }

        private void DialogoProgreso(){
            dialogo = new ProgressDialog(Principal.this);
            dialogo.setMessage(getString(R.string.descargando));
            dialogo.setCancelable(false);
            dialogo.show();
        }


    }
    public void salir(View v){
        finish();
    }
}
