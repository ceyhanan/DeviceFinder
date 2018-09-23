/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devicesockettester;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ceyha
 */
public class DeviceSocketTester implements Runnable {

    private static InetAddress deviceIP;
    private static int devicePort;
    private static int serverPort;
    private static DatagramSocket serverSocket;
    private static long beginTime, avglatency, maxlatency;
    private static int sentCount, F9PacketCount, E7PacketCount, receivedCount, correctRcvCount, notRcvCount, wrongRcvCount;
    private static boolean isRunning = false;

    /**
     */
    public DeviceSocketTester() {
        sentCount = 0;
        F9PacketCount = 0;
        E7PacketCount = 0;
        receivedCount = 0;
        wrongRcvCount = 0;
        correctRcvCount = 0;
        notRcvCount = 0;
        beginTime = 0;
        avglatency = 0;
        maxlatency = 0;
        serverSocket = null;

        isRunning = true;
    }

    public static void logLatency(long current) {
        if (current > maxlatency) {
            maxlatency = current;
        }

        if (receivedCount == 0) {
            avglatency = current;
        } else {
            avglatency = (avglatency + current) / 2;
        }
    }

    public static boolean checkPacket(byte[] received) {
        byte[] testData = new byte[14];

        testData[0] = 0x02;
        //1,2,3,4 --> Cihaz ID
        testData[5] = 0x38;
        testData[6] = 0x38;
        testData[7] = 0x44;
        testData[8] = 0x42;
        testData[9] = 0x46;
        testData[10] = 0x46;
        //11,12 --> checksum
        testData[13] = 0x03;

        if ((received[0] != 0x02) || (received[13] != 0x03)) {
            return false;
        }

        if ((received[0] != testData[0])
                || (received[5] != testData[5])
                || (received[6] != testData[6])
                || (received[7] != testData[7])
                || (received[8] != testData[8])
                || (received[9] != testData[9])
                || (received[10] != testData[10])
                || (received[13] != testData[13])) {
            return false;
        }

        return true;
    }

    public static boolean packetTester() {
        byte[] txData = new byte[6];
        byte[] rxData = new byte[1024];
        DatagramPacket txPacket;
        DatagramPacket rxPacket;

        txData[0] = 0x02;
        txData[1] = 'D';
        txData[2] = 'B';
        txData[3] = '0';
        txData[4] = '6';
        txData[5] = 0x03;

        txPacket = new DatagramPacket(txData, txData.length, deviceIP, devicePort);
        rxPacket = new DatagramPacket(rxData, rxData.length);

        try {
            serverSocket.send(txPacket);
            beginTime = System.currentTimeMillis();
            sentCount++;
        } catch (IOException ex) {
            System.out.print("Paket gonderimi basarisiz! -> " + ex);
            return false;
        }

        try {
            serverSocket.setSoTimeout(5000);
            serverSocket.receive(rxPacket);
            logLatency(System.currentTimeMillis() - beginTime);
            receivedCount++;
            if (rxPacket.getData()[7] == 0x44 && rxPacket.getData()[8] == 0x42) {
                if (checkPacket(rxPacket.getData())) {
                    correctRcvCount++;
                } else {
                    wrongRcvCount++;
                }
            } else if (rxPacket.getData()[7] == 'F' && rxPacket.getData()[8] == '9') {
                F9PacketCount++;
            } else if (rxPacket.getData()[5] == 'E' && rxPacket.getData()[6] == '7') {
                E7PacketCount++;
            } else {
                wrongRcvCount++;
            }
        } catch (SocketTimeoutException ex) {
            System.out.print("\rPaket zaman asimi! -> " + ex);
            notRcvCount++;
            return true;
        } catch (IOException ex) {
            System.out.print("Paket alimi basarisiz! -> " + ex);
            return false;
        }
        return true;
    }

    public static boolean takeDevice() {
        String userInputStr;
        Scanner userInput = new Scanner(System.in);

        System.out.print("Cihaz IP:");
        userInputStr = userInput.nextLine();
        try {
            deviceIP = InetAddress.getByName(userInputStr);
        } catch (UnknownHostException ex) {
            System.out.println("Gecersiz IP adresi");
            return false;
        }

        System.out.print("Cihaz Port:");
        userInputStr = userInput.nextLine();
        try {
            devicePort = Integer.parseInt(userInputStr);
        } catch (NumberFormatException ex) {
            System.out.println("Gecersiz cihaz port");
            return false;
        }

        System.out.print("Server Port:");
        userInputStr = userInput.nextLine();
        try {
            serverPort = Integer.parseInt(userInputStr);
        } catch (NumberFormatException ex) {
            System.out.println("Gecersiz server port");
            return false;
        }
        return true;
    }

    public static boolean openSocket() {
        try {
            serverSocket = new DatagramSocket(serverPort);
            return true;
        } catch (SocketException ex) {
            System.out.println("Server soket olusturulamadi! -> " + ex);
            return false;
        }
    }

    public boolean setDevice(String ip, int remotePort, int localPort) {
        try {
            deviceIP = InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            System.out.println("DeviceSocketTester.setDevice error: " + ex);
            return false;
        }

        serverPort = localPort;
        devicePort = remotePort;

        return true;
    }

    public void startTest() {
        if (openSocket() == false) {
            System.exit(-1);
        }
        while (packetTester()) {
            StringBuilder strBuilder = new StringBuilder();

            strBuilder
                    .append("\r")
                    .append("Gelen: ")
                    .append(receivedCount)
                    .append(" Dogru: ")
                    .append(correctRcvCount)
                    .append(" Diger: ")
                    .append(wrongRcvCount)
                    .append(" Timeout: ")
                    .append(notRcvCount);

            strBuilder
                    .append(" Ortalama: ")
                    .append(avglatency)
                    .append(" Max: ")
                    .append(maxlatency)
                    .append("                     ");

            System.out.print(strBuilder);
        }
    }

    public String getStatusStr() {
        StringBuilder strBuilder = new StringBuilder();

        strBuilder
                .append("Giden paket: ")
                .append(sentCount)
                .append("\r\n")
                .append("\r\n")
                .append("Gelen paket: ")
                .append(receivedCount)
                .append("\r\n")
                .append("->DB: ")
                .append(correctRcvCount)
                .append("\r\n")
                .append("->F9: ")
                .append(F9PacketCount)
                .append("\r\n")
                .append("->E7: ")
                .append(E7PacketCount)
                .append("\r\n")
                .append("->Diger: ")
                .append(wrongRcvCount)
                .append("\r\n")
                .append("->Timeout: ")
                .append(notRcvCount)
                .append("\r\n")
                .append("\r\n")
                .append("Paket gecikmesi:")
                .append("\r\n")
                .append("->Ortalama: ")
                .append(avglatency)
                .append(" ms")
                .append("\r\n")
                .append("->Max: ")
                .append(maxlatency)
                .append(" ms");

        return strBuilder.toString();
    }

    public void stop() {
        isRunning = false;
        if ((serverSocket.isClosed() == false) &&(serverSocket != null)) {
            serverSocket.close();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            startTest();
        }
    }
}
