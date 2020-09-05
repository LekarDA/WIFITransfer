package com.example.wifitransfer.ui.main.server;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.wifitransfer.R;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;

import static android.content.Context.WIFI_SERVICE;


public class ServerFragment extends Fragment {

    public static String SERVER_IP = "";
    public static final String SERVER_PORT = "8080";

    private ServerViewModel serverViewModel;
    private TextView tvIP, tvPort, tvConnection, tvMessage;
    private ImageView image;


    private ServerSocket serverSocket;
    private Thread Thread1 = null;

    private BufferedReader input;
    private Handler handler = new Handler();
    private Bitmap bitmap;

    public static ServerFragment newInstance() {
        ServerFragment fragment = new ServerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverViewModel = ViewModelProviders.of(this).get(ServerViewModel.class);
        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_server, container, false);
        tvIP = root.findViewById(R.id.tv_IP);
        tvPort = root.findViewById(R.id.tv_Port);
        tvConnection = root.findViewById(R.id.tv_ConnectionStatus);
        tvMessage = root.findViewById(R.id.tv_Messages);
        image = root.findViewById(R.id.iv_image);

        Thread1 = new Thread(new Thread1());
        Thread1.start();
        return root;
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                .getHostAddress();
    }

    class Thread1 implements Runnable {

        @Override
        public void run() {
            Socket socket;

            try {
                serverSocket = new ServerSocket(Integer.valueOf(SERVER_PORT));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvConnection.setText("Not connected");
                        tvIP.setText("IP: " + SERVER_IP);
                        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                    }
                });

                try {
                    socket = serverSocket.accept();
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvConnection.setText("Connected\n");
                        }
                    });

                    new Thread(new Thread2()).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    byte[]bytes = message.getBytes();

                    final File file = new File(getActivity().getExternalFilesDir(null), "TestFile.png");

                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                        bos.write(bytes);
                        bos.flush();
                        bos.close();

                        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                    } catch (Exception e) {
                        Log.e("Server", e.getMessage());
                    }

                    if (message != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvMessage.append("client:" + message + "\n"+ file.getName());
                                image.setImageBitmap(bitmap);
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
