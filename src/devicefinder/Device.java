/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devicefinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author Ceyhan ANKITÇI
 */
public class Device {

    /*
    String hostName;
    InetAddress device_ip;
    InetAddress remote_ip;
    int device_port;
    int remote_port;
    byte[] mac;
     */
    private DatagramSocket socket = null;
    private DatagramPacket packet;
    private byte[] sendData;
    private byte[] SmartPackTxFrame;
    public LocalParameters localparams;
    public SocketParameters socketparams;

    public final int SmartCommand_WHOIS = 0x0000;
    public final int SmartCommand_GET_SERIALTOETHERNETCONFIG = 0x0005;
    public final int SmartCommand_GET_SECONDSCOUNTER = 0x0101;
    public final int SmartCommand_UART_ERROR_STATUS = 0x0103;
    public final int SmartCommand_F_SETLOCAL = 0xFF01;
    public final int SmartCommand_F_SETSOCKETS = 0xFF03;
    public final int SmartCommand_F_SETSERIALTOETHERNETCONFIG = 0xFF05;
    public final int SmartCommand_F_SETHARDWARECONFIG = 0xFF07;

    public class LocalParameters {

        public byte[] MAC = new byte[6];
        public byte[] DeviceHostName = new byte[16];
        public byte[] IP = new byte[4];
        public byte[] GatewayIP = new byte[4];
        public byte[] SubnetMask = new byte[4];
        public byte[] PrimaryDNS = new byte[4];
        public byte[] SecondaryDNS = new byte[4];
        public byte DHCP;
        public byte Rsv1;
        public byte Rsv2;
        public byte Rsv3;

        private byte[] _SmartPackCommandParameters = new byte[40];
    }

    public class SocketParameters {

        public byte ServerConfig;
        public byte ClientConfig;
        public byte Rsv1;
        public byte Rsv2;
        public int HostPort;
        public int RemotePort;
        public byte[] RemoteIP = new byte[4];

        public byte[] _SmartPackCommandParameters = new byte[12];
    }

    public Device(byte[] hostName, InetAddress device_ip, InetAddress remote_ip, int device_port, int remote_port, byte[] mac) {
        localparams = new LocalParameters();
        socketparams = new SocketParameters();

        localparams.DeviceHostName = hostName;
        localparams.IP = device_ip.getAddress();
        localparams.MAC = mac;
        socketparams.RemoteIP = remote_ip.getAddress();
        socketparams.HostPort = device_port;
        socketparams.RemotePort = remote_port;
    }

    public void changeIP(InetAddress ip) {
        byte[] SmartPackHeader = new byte[12];
        SmartPackHeader[0] = (byte) '*';
        SmartPackHeader[1] = (byte) 's';
        SmartPackHeader[2] = (byte) 'c';
        SmartPackHeader[3] = (byte) 'p';
        SmartPackHeader[4] = (byte) '@';
        SmartPackHeader[5] = localparams.MAC[3];
        SmartPackHeader[6] = localparams.MAC[4];
        SmartPackHeader[7] = localparams.MAC[5];
        SmartPackHeader[8] = (byte) SmartCommand_F_SETLOCAL;
        SmartPackHeader[9] = (byte) (SmartCommand_F_SETLOCAL >> 8);
        SmartPackHeader[10] = (byte) (localparams._SmartPackCommandParameters.length);
        SmartPackHeader[11] = (byte) (localparams._SmartPackCommandParameters.length >> 8);
        
        System.arraycopy(localparams.DeviceHostName, 0, localparams._SmartPackCommandParameters, 0, 16);
        System.arraycopy(localparams.IP, 0, localparams._SmartPackCommandParameters, 16, 4);
        System.arraycopy(localparams.GatewayIP, 0, localparams._SmartPackCommandParameters, 20, 4);
        System.arraycopy(localparams.SubnetMask, 0, localparams._SmartPackCommandParameters, 24, 4);
        System.arraycopy(localparams.PrimaryDNS, 0, localparams._SmartPackCommandParameters, 28, 4);
        System.arraycopy(localparams.SecondaryDNS, 0, localparams._SmartPackCommandParameters, 32, 4);
        localparams._SmartPackCommandParameters[36] = localparams.DHCP;
        localparams._SmartPackCommandParameters[37] = 0;
        localparams._SmartPackCommandParameters[38] = 0;
        localparams._SmartPackCommandParameters[39] = 0;

        SmartPackTxFrame = new byte[SmartPackHeader.length + localparams._SmartPackCommandParameters.length + 1]; //+1 Checksum
        System.arraycopy(SmartPackHeader, 0, SmartPackTxFrame, 0, SmartPackHeader.length);
        System.arraycopy(localparams._SmartPackCommandParameters, 0, SmartPackTxFrame, SmartPackHeader.length, localparams._SmartPackCommandParameters.length);

        byte[] TxChecksum = new byte[1];
        TxChecksum[0] = 0;
        for (int i = 0; i < SmartPackTxFrame.length; i++) {
            TxChecksum[0] += SmartPackTxFrame[i];
        }
        System.arraycopy(TxChecksum, 0, SmartPackTxFrame, SmartPackTxFrame.length - 1, TxChecksum.length);
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println("Error create datagram socket: " + ex);
        }
        try {
            packet = new DatagramPacket(SmartPackTxFrame, SmartPackTxFrame.length, InetAddress.getByName("255.255.255.255"), 31500);
            socket.send(packet);
        } catch (IOException ex) {
            System.out.println("Error broadcasting: " + ex);
        }
    }

    public void changeRemoteIP(InetAddress ip) {

    }

    public void changePort(int port) {

    }

    public void changeRemotePort(int port) {

    }
}
