package com.example.jesuspereyra.test;

import android.os.AsyncTask;
import android.util.Log;

import com.example.jesuspereyra.test.ResultCallback;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by jesuspereyra on 9/7/17.
 */

public class SendToServer extends AsyncTask<String, String, String> {

//    private final static String SERVER_ADDRESS = "192.168.18.250";

    ResultCallback resultCallback;

    public SendToServer(ResultCallback resultCallback) {
        super();
        this.resultCallback = resultCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String modifiedSentence = null;
        try {
            Log.i("Info", "onClick: " + strings[0]);
            String SERVER_ADDRESS = (strings[1]);
            int port = new Integer(strings[2]);
            byte[] message;
            if(strings.length < 4) {
                message = (strings[0]).getBytes();
            } else {
                message = (strings[0] + "$" + strings[3]).getBytes();
            }
            byte[] receiveData = new byte[22];
            InetAddress IPAddress = InetAddress.getByName(SERVER_ADDRESS);
            DatagramPacket response = new DatagramPacket(message, message.length, IPAddress, port);
            DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
            DatagramSocket clientSocket = null;
            clientSocket = new DatagramSocket();
            System.out.println("Sending: '" + new String(message) + "'");
            System.out.println("Size: " + message.length);
            clientSocket.send(response);
            clientSocket.setSoTimeout(3000);
            while (true) {
                try {
                    clientSocket.receive(received);
                    modifiedSentence = new String(received.getData());
                    clientSocket.close();
                } catch (SocketTimeoutException ste) {
                    System.out.println("### Timed out after 5 seconds");
                    clientSocket.close();
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modifiedSentence;//returns what you want to pass to the onPostExecute()
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        resultCallback.success(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
