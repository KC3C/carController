package com.example.mac.carcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int RECEIVER_PORT = 8686; // 接收端端口号
    public static final int SENDER_PORT = 8080; // 发送端端口号
    private ServerSocket imageSocket;
    private Bitmap bitmap;
    private String videoSenderIp;
    private Matrix matrix = new Matrix();

    private EditText videoSenderIpEditTest;
    private ImageView receivedImageView;
    private EditText blueToothEditText;
    private String blueToothId;
    private FloatingActionButton bfab;
    private FloatingActionButton fab;
    private BluetoothAdapter mBluetoothAdapter;
    public static BluetoothSocket mBluetoothSocket;
    private static UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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
        blueToothEditText = findViewById(R.id.bluetooth_name);
        blueToothEditText.setVisibility(View.GONE); // 隐藏蓝牙输入框
        bfab = (FloatingActionButton) findViewById(R.id.link_bluetooth);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // 填写好IP后点右下角按钮开始连接发送端
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
                    fab.hide();
                    hideIpText();
                } else {
                    Toast.makeText(MainActivity.this, "请先输入发送端IP地址", Toast.LENGTH_SHORT).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 处理图像放置到imageView的handler
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(bitmap!=null && msg.what == 1) {
                    if (bitmap.getWidth() > bitmap.getHeight())
                        bitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    receivedImageView.setVisibility(View.VISIBLE);
                    receivedImageView.setImageBitmap(bitmap);
                } else if (msg.what == 2) {
                    Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
                }
                super.handleMessage(msg);
            }
        };


        matrix.postRotate(90);

        Thread videoThread = new ReceiveVideo();
        videoThread.start();

        //填好蓝牙名字并按下按钮连接蓝牙
        bfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                blueToothId = blueToothEditText.getText().toString();
                // 写死蓝牙名称
                blueToothId = "高颜值人士";
                if(blueToothId.length()==0){
                    Toast.makeText(MainActivity.this, "请先输入蓝牙名字", Toast.LENGTH_SHORT).show();
                    finish();
                }
                //连接小车蓝牙
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(mBluetoothAdapter == null){
                    Toast.makeText(MainActivity.this,"BlueTooth can't be linked",Toast.LENGTH_LONG).show();
                    finish();
                }
                if(!mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.enable();
                }
                mBluetoothAdapter.startDiscovery();
                Object[] btobjs = mBluetoothAdapter.getBondedDevices().toArray();
                for(int i=0;i<btobjs.length;++i){
                    BluetoothDevice bdevice = (BluetoothDevice)btobjs[i];
                    if(bdevice.getName().equals(blueToothId)){
                        try {
                            mBluetoothSocket = bdevice.createInsecureRfcommSocketToServiceRecord(mUUID);
                        }
                        catch (IOException e){
                        }
                        break;
                    }
                }
                if(mBluetoothSocket != null){
                    try {
                        mBluetoothSocket.connect();
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    catch (IOException e){
                    }
                    Toast.makeText(MainActivity.this,"bluetooth linked successfully",Toast.LENGTH_LONG).show();
                    bfab.hide();
                    hideBlueTeethText();
                }
                else{
                    Toast.makeText(MainActivity.this,"bluetooth not found",Toast.LENGTH_LONG).show();
                }
            }
        }
        );
    }

    //小车前进
    public void Forward(){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            mBluetoothSocket.getOutputStream().write("A".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public void Forward(int time){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            char[] msg_arr = Integer.toString(time).toCharArray();
            mBluetoothSocket.getOutputStream().write("Q".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                mBluetoothSocket.getOutputStream().write(msg_arr[i]);
            mBluetoothSocket.getOutputStream().write("O".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public void Back(){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            mBluetoothSocket.getOutputStream().write("B".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public void Back(int time){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            char[] msg_arr = Integer.toString(time).toCharArray();
            mBluetoothSocket.getOutputStream().write("H".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                mBluetoothSocket.getOutputStream().write(msg_arr[i]);
            mBluetoothSocket.getOutputStream().write("O".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public void Left(){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            mBluetoothSocket.getOutputStream().write("R".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public void Left(int time){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            char[] msg_arr = Integer.toString(time).toCharArray();
            mBluetoothSocket.getOutputStream().write("Y".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                mBluetoothSocket.getOutputStream().write(msg_arr[i]);
            mBluetoothSocket.getOutputStream().write("O".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public void Right(){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            mBluetoothSocket.getOutputStream().write("L".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public void Right(int time){
        if(mBluetoothSocket == null){
            Toast.makeText(MainActivity.this,"bluetooth not connected",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            char[] msg_arr = Integer.toString(time).toCharArray();
            mBluetoothSocket.getOutputStream().write("Z".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                mBluetoothSocket.getOutputStream().write(msg_arr[i]);
            mBluetoothSocket.getOutputStream().write("O".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public static void Stop(){
        if(mBluetoothSocket == null){
            Message msg = Message.obtain();
            msg.what = 2;
            handler.sendMessage(msg);
            return;
        }
        try {
            mBluetoothSocket.getOutputStream().write("P".getBytes());
        }
        catch (IOException e){
            Log.e("Bluetooth",e.getMessage());
        }
    }

    public boolean isBlueToothConnected(){
        return mBluetoothSocket != null;
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

        if (id == R.id.nav_button) {
            Toast.makeText(MainActivity.this, "Button Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new ButtonFragment());
        } else if (id == R.id.nav_gesture){
            Toast.makeText(MainActivity.this, "Gesture Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new GestureFragment());
        }else if (id == R.id.nav_joystick){
            Toast.makeText(MainActivity.this, "Joystick Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new JoyStickFragment());
        }else if (id == R.id.nav_voice){
            Toast.makeText(MainActivity.this, "Voice Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new VoiceFragment());
        }
        else if (id == R.id.nav_gravity){
            Toast.makeText(MainActivity.this, "Gravity Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new GravityFragment());
        }
        else if (id == R.id.nav_trace){
            Toast.makeText(MainActivity.this, "Trace Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new TraceFragment());
        }
        else if (id == R.id.nav_face) {
            Toast.makeText(MainActivity.this, "Face Mode", Toast.LENGTH_SHORT).show();
//            replaceFragment(new FaceFragment());
            Intent intent=new Intent(MainActivity.this, FaceActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void hideIpText() {
        videoSenderIpEditTest.setVisibility(View.GONE);
    }

    private void hideBlueTeethText() {
        blueToothEditText.setVisibility(View.GONE);
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
            Message msg=new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }
}
