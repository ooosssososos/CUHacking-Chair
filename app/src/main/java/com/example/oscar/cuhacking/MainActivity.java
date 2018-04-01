package com.example.oscar.cuhacking;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    String lastEv = "";
    Camera c;
    private boolean userHere = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET},1);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void sendRequest(String e){
        try {
            new SendRequestTask(e).execute(new URL("http://www.google.com"));
        }catch(Exception v){
            v.printStackTrace();
        }
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.values[0] <= 1) {
                if (userHere = false) {
                    userHere = true;
                    sendRequest("userreturn");
                }
                if(lastEv.equalsIgnoreCase("leanforward") || lastEv.equalsIgnoreCase("")){
                    lastEv = "leanbackward";
                    sendRequest("leanbackward");
                }
            }else{
                if(lastEv.equalsIgnoreCase("leanbackward") || lastEv.equalsIgnoreCase("")){
                    lastEv = "leanforward";
                    sendRequest("leanforward");
                }
                dispatchTakePictureIntent();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void proc(Bitmap b){
        if(!userHere){
            int p = b.getPixel(0,0);

            int R = (p >> 16) & 0xff;
            int G = (p >> 8) & 0xff;
            int B = p & 0xff;
            if((0.2126*R + 0.7152*G + 0.0722*B) > 150) userHere = false;
        }
    }

    private void dispatchTakePictureIntent() {
        c = Camera.open(0);
        c.startPreview();
        c.takePicture(null,null,new PhotoHandler(this));
        c.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap b = Bitmap.createScaledBitmap(imageBitmap, 1, 1, false);
            proc(b);
        }
    }
}

class PhotoHandler implements Camera.PictureCallback {

    private final Context context;

    public PhotoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap b = Bitmap.createScaledBitmap(picture, 1, 1, false);
        ((MainActivity)context).proc(b);
    }
}

class SendRequestTask extends AsyncTask<URL, Integer, Long> {
    String ev;
    public SendRequestTask(String e){
        ev = e;
    }

    protected Long doInBackground(URL... urls) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .authority("jaiiiiii.lib.id")
                    .path("testf@dev")
                    .appendQueryParameter("event", ev)
                    .build();
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL(uri.toString())).openConnection();
            HttpURLConnection.setFollowRedirects(false);
            urlConnection.setConnectTimeout(1 * 1000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
            urlConnection.connect();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            } finally {
                urlConnection.disconnect();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return (long)0;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Long result) {
    }
}
