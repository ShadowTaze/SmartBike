package com.example.benearle.sebapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {
    private static String ip = "192.168.145.130";
    private DatagramSocket socket;
    private InetAddress host;
    private int port;

    private void initConnection() throws Exception {
        host = InetAddress.getByName(ip);
        port = 13375;
        socket = new DatagramSocket();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initConnection();
        } catch (Exception e){
            System.out.println("ERROR");
        }
    }

    public void showState(View view) {
        Intent intent = new Intent(this, StateActivity.class);
        startActivity(intent);
    }


    public void settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startRide(View view) {
        Toast.makeText(this, "Starting a new ride...", Toast.LENGTH_SHORT).show();
        byte[] data = {'n', 'e', 'w', 'R', 'i', 'd', 'e', ':', '1'};
        try {
            socket.send(new DatagramPacket(data, data.length, host, port));
        } catch (Exception e){
            System.out.println("ERROR");
        }
    }
}
