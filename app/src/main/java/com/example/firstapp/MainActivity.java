package com.example.firstapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;


@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btn_download;
    ServerSocket serverSocket;
    TextView tvMessages = null;
    private static final String NAME_OF_VIDEO = "bigbunny.mp4";
    private static String  SERVER_IP = "";
    private static final  int SERVER_PORT = 8080;
    //public static String URL = "https://www.radiantmediaplayer.com/media/big-buck-bunny-360p.mp4";
    private static final  String myURL ="https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_640_3MG.mp4";

      @Override
    protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                  || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
              ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

          else {
              // Permission has already been granted
              Log.d("MAIN_ACTIVITY", "WRITE PERMISSION ALREADY GRANTED");
          }

          if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                  || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
              ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 1);

          else {
              // Permission has already been granted
              Log.d("MAIN_ACTIVITY", "WRITE PERMISSION ALREADY GRANTED");
          }

          if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED)
              ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION}, 1);

          else {
              // Permission has already been granted
              Log.d("MAIN_ACTIVITY", "WRITE PERMISSION ALREADY GRANTED");
          }

          try {
              SERVER_IP = getLocalIpAddress();
          } catch (UnknownHostException e) {
              e.printStackTrace();
          }


          btn_download = (Button) findViewById(R.id.download_btn);
          videoView = (VideoView) findViewById(R.id.videoView);
          tvMessages = (TextView) findViewById(R.id.tvMessages);

          btn_download = findViewById(R.id.download_btn);
          tvMessages.setText("Waiting for download to complete....");
          btn_download.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                  Toast.makeText(MainActivity.this, "Download clicked", Toast.LENGTH_SHORT).show();
                  new DownloadingTask().execute();
                  Uri uri = Uri.parse(myURL);
                  videoView.setVideoURI(uri);
                  videoView.start();




              }
          });

      }
    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }



    public class DownloadingTask extends AsyncTask<Void, Void, Void>  {

        String outputFile;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Download Started", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                String rootDir = Environment.getExternalStorageDirectory()
                        + "/" + "Video";
                File rootFile = new File(rootDir);
                rootFile.mkdir();
                URL url = new URL(myURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                int status = httpURLConnection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    Log.e("HTTP", "Server returned HTTP " + status
                            + " " + httpURLConnection.getResponseMessage());
                }

                File localFile = new File(rootFile, NAME_OF_VIDEO);
                outputFile = "PATH OF LOCAL FILE : " + localFile.getPath();
                if (rootFile.exists()) Log.d("OUT", outputFile);

                FileOutputStream fos = new FileOutputStream(localFile);
                InputStream in = httpURLConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int nbOfPaquetsReceived = 0;
                int len1 = 0;
                try {
                    while ((len1 = in.read(buffer)) > 0) {
                        nbOfPaquetsReceived++;
                        fos.write(buffer, 0, len1);
                    }
                } catch (IOException se) {
                    Log.d("BUG", "Hmmmmm");
                }
                fos.close();
                in.close();


            } catch (IOException e) {
                Log.d("Error....", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Download Failed", Toast.LENGTH_SHORT).show();
                    Log.e("FAIL", "Download Failed");

                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("FAIL EXE", "Download Failed with Exception - " + e.getLocalizedMessage());

            }
            super.onPostExecute(result);
            Thread Thread1 = new Thread(new Thread1());
            Thread1.start();
        }
    }


    class Thread1 implements Runnable {
        @Override
        public void run() {
            Socket socket = null;
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            OutputStream os = null;
            try {

                serverSocket = new ServerSocket(SERVER_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Waiting for clients on : " + SERVER_IP+":"+SERVER_PORT );

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                System.out.println("Waiting...");
                try {
                    socket = serverSocket.accept();
                    System.out.println("Accepted connection : " + socket);
                    // send file
                    File myFile = new File ("/storage/emulated/0/Video/bigbunny.mp4");
                    byte [] mybytearray  = new byte [(int)myFile.length()];
                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);
                    try {
                        bis.read(mybytearray,0,mybytearray.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    os = socket.getOutputStream();
                    System.out.println("Sending " + "/storage/emulated/0/Video/bigbunny.mp4" + "(" + mybytearray.length + " bytes)");
                    os.write(mybytearray,0,mybytearray.length);
                    os.flush();
                    System.out.println("Done.");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {


                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (socket!=null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                tvMessages.setText("Done.");
            }
        }

    }
}