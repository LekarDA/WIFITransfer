package com.example.wifitransfer.ui.main.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.wifitransfer.CommonMethods;
import com.example.wifitransfer.R;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ClientFragment extends Fragment {

    private ClientViewModel clientViewModel;
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    private EditText etIP, etPort;
    private TextView tvMessages;
    private EditText etMessage;
    private Button btnSend, btnConnect, btn_ChooseFile;

    private String SERVER_IP;
    private int SERVER_PORT;
    private Thread Thread1 = null;
//    private PrintWriter output;
    private byte[] fileBytes;
    private OutputStream out;

    private Handler handler = new Handler();
    static long ActualFilelength = 0;

    public static ClientFragment newInstance() {
        ClientFragment fragment = new ClientFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientViewModel = ViewModelProviders.of(this).get(ClientViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_client, container, false);
        etIP = root.findViewById(R.id.et_IP);
        etPort = root.findViewById(R.id.et_Port);
        btnConnect = root.findViewById(R.id.btn_Connect);
        tvMessages = root.findViewById(R.id.tv_Messages);
        etMessage = root.findViewById(R.id.et_Message);
        btn_ChooseFile = root.findViewById(R.id.btn_ChooseFile);
        btnSend = root.findViewById(R.id.btn_Send);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileBytes != null) {
                    new Thread(new Thread3(fileBytes)).start();
                }
            }
        });

        btn_ChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/* video/* ");
                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
            }
        });

        return root;
    }

    class Thread1 implements Runnable {

        @Override
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = socket.getOutputStream();
//                output = new PrintWriter(socket.getOutputStream(), true);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected\n");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Thread3 implements Runnable {
        private byte[] picture;

        Thread3(byte[] picture) {
            this.picture = picture;
        }

        @Override
        public void run() {
            try {
                out.write(picture);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            output.write(message);
//            output.println();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("client: " + picture.toString() + "\n");
                    etMessage.setText("");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            Uri uri = data.getData();

            String selectedfilePath = null;
            try {
                selectedfilePath = CommonMethods.getPath(uri, getActivity());
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            String Extension = "";
            if (selectedfilePath != null) {
                File file = new File(selectedfilePath);
                Log.i("file name is   ::" ,  file.getName());
                Long FileLength = file.length();
                ActualFilelength = FileLength;
                try {
                    fileBytes = getByteArrayFromFile(file.getPath());
                            Extension = file.getName();
                    Log.i("Name of File-> ", "" + Extension);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            } else {
                CommonMethods.e("", "path is null");
                return;
            }

        }
    }

    public  byte[] getByteArrayFromFile(String filePath) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            for (int readNum; (readNum = fis.read(b)) != -1; ) {
                bos.write(b, 0, readNum);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            Log.d("mylog", e.toString());
        }
        return null;
    }

}
