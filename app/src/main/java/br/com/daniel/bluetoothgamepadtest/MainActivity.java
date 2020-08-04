package br.com.daniel.bluetoothgamepadtest;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnUp, btnDown, btnLeft, btnRight, btnConexao;

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;

    ConnectedThread connectedThread;
    Handler mHandler;
    StringBuilder dadosBluetooth = new StringBuilder();

    BluetoothAdapter meuBluetoothAdapter = null;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;

    boolean conexao = false;

    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static String MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConexao = (Button) findViewById(R.id.btnConexao);
        btnUp = (Button) findViewById(R.id.btnUp);
        btnDown = (Button) findViewById(R.id.btnDown);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);

        meuBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (meuBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui bluetooth", Toast.LENGTH_LONG).show();
        } else if (!meuBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, SOLICITA_ATIVACAO);
        }

        btnConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    //desconectar
                    try {
                        meuSocket.close();
                        conexao = false;
                        btnConexao.setText("Conectar");
                        Toast.makeText(getApplicationContext(), "O Bluetooth foi desconectado.", Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                } else {
                    //conectar
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXAO);
                    conexao = true;
                    btnConexao.setText("Desconectar");
                }
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.enviar("up");
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth não está conectado.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.enviar("down");
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth não está conectado.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.enviar("left");
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth não está conectado.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.enviar("right");
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth não está conectado.", Toast.LENGTH_LONG).show();
                }
            }
        });

//        mHandler = new Handler() {
//            @Override
//            public void handleMessage(@NonNull Message msg) {
////                super.handleMessage(msg);
//                if(msg.what == MESSAGE_READ) {
//                    String recebidos = (String) msg.obj;
//                    dadosBluetooth.append(recebidos);
//                    int fimInformacao = dadosBluetooth.indexOf("}");
//                    if(fimInformacao > 0) {
//                        String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);
//                        int tamInformacao = dadosCompletos.length();
//
//                        if(dadosBluetooth.charAt(0) == '{') {
//                            String dadosFinais = dadosBluetooth.substring(1, tamInformacao);
//                            Log.d("Recebidos", dadosFinais);
//                        }
//                        dadosBluetooth.delete(0, dadosBluetooth.length());
//                    }
//                }
//            }
//        };
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case SOLICITA_ATIVACAO:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth ativado.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não foi ativado, o app será encerrado.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SOLICITA_CONEXAO:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);

                    meuDevice = meuBluetoothAdapter.getRemoteDevice(MAC);

                    try {
                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);

                        meuSocket.connect();

                        conexao = true;

                        connectedThread = new ConnectedThread(meuSocket);
                        connectedThread.start();

                        Toast.makeText(getApplicationContext(), "Você foi conectado com: " + MAC, Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + erro, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao obter o endereço MAC.", Toast.LENGTH_LONG).show();
                }
        }

    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

//            while (true) {
//                try {
//                    bytes = mmInStream.read(buffer);
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
//                } catch (IOException e) {
//                    break;
//                }
//            }
        }

        public void enviar(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
            }
        }
    }
}


////    public class MyBluetoothService {
////        private static final String TAG = "MY_APP_DEBUG_TAG";
////        private Handler handler; // handler that gets info from Bluetooth service
//
////        // Defines several constants used when transmitting messages between the
////        // service and the UI.
////        private interface MessageConstants {
////            public static final int MESSAGE_READ = 0;
////            public static final int MESSAGE_WRITE = 1;
////            public static final int MESSAGE_TOAST = 2;
//
////            // ... (Add other message types here as needed.)
////        }

//private class ConnectedThread extends Thread {
//    private final InputStream mmInStream;
//    private final OutputStream mmOutStream;
//    private byte[] mmBuffer; // mmBuffer store for the stream

//    public ConnectedThread(BluetoothSocket socket) {
//        InputStream tmpIn = null;
//        OutputStream tmpOut = null;

//        // Get the input and output streams; using temp objects because
//        // member streams are final.
//        try {
//            tmpIn = socket.getInputStream();
//            tmpOut = socket.getOutputStream();
//        } catch (IOException e) {
//        }

//        mmInStream = tmpIn;
//        mmOutStream = tmpOut;
//    }

//    public void run() {
//        mmBuffer = new byte[1024];
//        int numBytes; // bytes returned from read()

//        // Keep listening to the InputStream until an exception occurs.
//        while (true) {
//            try {
////                    // Read from the InputStream.
//                numBytes = mmInStream.read(mmBuffer);
//                String dadosBt = new String(mmBuffer, 0, numBytes);
//                mHandler.obtainMessage(MESSAGE_READ, numBytes, -1, dadosBt).sendToTarget();
//            } catch (IOException e) {
//                break;
//            }
//        }
//    }

//    public void enviar(String dadosEnviar) {
//        byte[] msgBuffer = dadosEnviar.getBytes();
//        try {
//            mmOutStream.write(msgBuffer);
//        } catch (IOException e) {
//        }
//    }
//}
//}