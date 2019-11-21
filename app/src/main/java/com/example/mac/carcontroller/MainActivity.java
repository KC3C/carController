package com.example.mac.carcontroller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int RECEIVER_PORT = 8686; // 接收端端口号
    public static final int SENDER_PORT = 8080; // 发送端端口号
    private EditText videoSenderIpEditTest;
    private ImageView receivedImageView;
    private ServerSocket imageSocket;
    private Bitmap bitmap;
    private String videoSenderIp;
    private Matrix matrix = new Matrix();

    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FrameLayout frameLayout = findViewById(R.id.fragment_container);
        videoSenderIpEditTest = findViewById(R.id.video_sender_ip_input);
        receivedImageView = findViewById(R.id.video_receiver);
        receivedImageView.setImageResource(R.drawable.ic_menu_camera);

        // 填写好IP后点右下角按钮开始连接发送端
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoSenderIp = videoSenderIpEditTest.getText().toString();
                if (videoSenderIp.length() != 0) {
                    //连接
//                    ConnectClientTask connectClientTask = new ConnectClientTask(videoSenderIp, SENDER_PORT);
//                    connectClientTask.execute();
                    Thread connectThread = new ConnectSender();
                    connectThread.start();
                } else {
                    Toast.makeText(MainActivity.this, "请先输入发送端IP地址", Toast.LENGTH_SHORT).show();
                }
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        matrix.postRotate(90);

        Thread videoThread = new ReceiveVideo();
        videoThread.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //连接发送端
    public class ConnectClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;

        ConnectClientTask(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
        }


        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(videoSenderIp, SENDER_PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(MainActivity.this, "请求已发送!", Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
        }
    }

    // 连接发送端的线程
    class ConnectSender extends Thread {
        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(videoSenderIp, SENDER_PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 监听是否收到图像的线程
    class ReceiveVideo extends Thread{
        private int length = 0;
        private int num = 0;
        private byte[] buffer = new byte[2048];
        private byte[] data = new byte[204800];

        @Override
        public void run(){
            try{
                imageSocket = new ServerSocket(RECEIVER_PORT);
                while(true){
                    // 监听是否收到图像
                    Socket socket = imageSocket.accept();
                    try{
                        InputStream input = socket.getInputStream();
                        Log.d("Image","GetImage");
                        num = 0;
                        do{
                            length = input.read(buffer);
                            if(length >= 0){
                                System.arraycopy(buffer,0,data,num,length);
                                num += length;
                            }
                        }while(length >= 0);

                        //调用线程后台处理图像
                        new setImageThread(data,num).start();
                        input.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }finally{
                        socket.close();
                    }
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    class setImageThread extends Thread {
        private byte[]data;
        private int num;
        public setImageThread(byte[] data, int num){
            this.data = data;
            this.num = num;
        }
        @Override
        public void run(){
            bitmap = BitmapFactory.decodeByteArray(data, 0, num);
            if(bitmap!=null) {
                if (bitmap.getWidth() > bitmap.getHeight())
                    bitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                receivedImageView.setVisibility(View.VISIBLE);

                receivedImageView.setImageBitmap(bitmap);
            }
        }
    }
}
