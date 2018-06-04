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
import java.util.UUID;

/**
 * Created by jesuspereyra on 5/28/18.
 */

public class SendToServerPieces extends AsyncTask<String, String, String>{

//    private final static String SERVER_ADDRESS = "192.168.18.250";

    ResultCallback resultCallback;

    public SendToServerPieces(ResultCallback resultCallback) {
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
        String uniqueId = UUID.randomUUID().toString();
        try {
            System.out.println(uniqueId);
            Log.i("Info", "onClick: " + strings[0]);
            String SERVER_ADDRESS = (strings[5]);
            int port = new Integer(strings[6]);
            byte[] message1, message2, message3, message4, message5;
            message1 = (strings[0] + "$" + uniqueId + "1").getBytes();
            message2 = (strings[1] + "$" + uniqueId + "2").getBytes();
            message3 = (strings[2] + "$" + uniqueId + "3").getBytes();
            message4 = (strings[3] + "$" + uniqueId + "4").getBytes();
            message5 = (strings[4] + "$" + uniqueId + "5").getBytes();
            byte[] receiveData = new byte[22];
            InetAddress IPAddress = InetAddress.getByName(SERVER_ADDRESS);
            DatagramPacket response1 = new DatagramPacket(message1, message1.length, IPAddress, port);
            DatagramPacket response2 = new DatagramPacket(message2, message2.length, IPAddress, port);
            DatagramPacket response3 = new DatagramPacket(message3, message3.length, IPAddress, port);
            DatagramPacket response4 = new DatagramPacket(message4, message4.length, IPAddress, port);
            DatagramPacket response5 = new DatagramPacket(message5, message5.length, IPAddress, port);
            DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
            DatagramSocket clientSocket = null;
            clientSocket = new DatagramSocket();
            System.out.println("Sending: 1'" + new String(message1) + "' 1");
            System.out.println("Sending: 2'" + new String(message2) + "' 2");
            System.out.println("Sending: 3'" + new String(message3) + "' 3");
            System.out.println("Sending: 4'" + new String(message4) + "' 4");
            System.out.println("Sending: 5'" + new String(message5) + "' 5");
            System.out.println("Size: 1" + message1.length);
            System.out.println("Size: 2" + message2.length);
            System.out.println("Size: 3" + message3.length);
            System.out.println("Size: 4" + message4.length);
            System.out.println("Size: 5" + message5.length);
            clientSocket.send(response1);
            clientSocket.send(response2);
            clientSocket.send(response3);
            clientSocket.send(response4);
            clientSocket.send(response5);
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

