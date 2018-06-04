package com.example.jesuspereyra.test;

import android.os.AsyncTask;
import android.util.Log;

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

public class SendToServerRGB extends AsyncTask<String, String, String> {

//    private final static String SERVER_ADDRESS = "192.168.18.250";

    ResultCallback resultCallback;

    public SendToServerRGB(ResultCallback resultCallback) {
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
            String SERVER_ADDRESS = (strings[3]);
            int port = new Integer(strings[4]);
            byte[] message1;
            byte[] message2;
            byte[] message3;
            message1 = (strings[0] + "$" + "R").getBytes();
            message2 = (strings[1] + "$" + "G").getBytes();
            message3 = (strings[2] + "$" + "B").getBytes();
            byte[] receiveData = new byte[22];
            InetAddress IPAddress = InetAddress.getByName(SERVER_ADDRESS);
            DatagramPacket response1 = new DatagramPacket(message1, message1.length, IPAddress, port);
            DatagramPacket response2 = new DatagramPacket(message2, message2.length, IPAddress, port);
            DatagramPacket response3 = new DatagramPacket(message3, message3.length, IPAddress, port);
            DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
            DatagramSocket clientSocket = null;
            clientSocket = new DatagramSocket();
            System.out.println("Sending: '" + new String(message1) + "' Red");
            System.out.println("Sending: '" + new String(message2) + "' Green");
            System.out.println("Sending: '" + new String(message3) + "' Blue");
            System.out.println("Size: " + message1.length);
            System.out.println("Size: " + message2.length);
            System.out.println("Size: " + message3.length);
            clientSocket.send(response1);
            clientSocket.send(response2);
            clientSocket.send(response3);
            clientSocket.setSoTimeout(5000);
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
