package net.Protocol.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.protocol.Exceptions.ProtocolUnknownPacketException;
import net.protocol.Listeners.RegisteredPacketListener;

public class RegisteredPacketThread implements Runnable {

    private RegisteredPacketListener listener;

    public static void main(RegisteredPacketListener listener) {
	RegisteredPacketThread pl = new RegisteredPacketThread();
	pl.listener = listener;
	new Thread(pl).start();
    }

    @Override
    public void run() {
	int port = listener.getPort();
	DatagramSocket serverSocket = null;
	try {
	    serverSocket = new DatagramSocket(port);
	} catch (SocketException ex) {
	    listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Socket Exception on port " + port + ", closing thread.");
	    return;
	}

	byte[] receiveData = new byte[1024];
	listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Started packet listening thread.");
	while (true) {
	    if (!listener.isRunning()) {
		break;
	    }
	    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    try {
		serverSocket.receive(receivePacket);
	    } catch (IOException ex) {
		listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] IO Exception on port " + port + ", closing thread.");
		return;
	    }
	    char[] recieve = new String(receivePacket.getData()).toCharArray();
	    if (recieve == null) {
		listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Bad packet received.");
		continue;
	    }

	    if (recieve.length < 3) {
		listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Bad packet received.");
		continue;
	    }
	    String info = new String(recieve);
	    if (info.length() < 3) {
		listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Bad packet received.");
		continue;
	    }
	    if (!info.contains("¤")) {
		listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Bad packet received.");
		continue;
	    }
	    int id = -1;
	    try {
		id = Integer.parseInt("" + info.charAt(0) + info.charAt(1) + info.charAt(2));
	    } catch (NumberFormatException ex) {
		listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Bad packet received.");
		continue;
	    }
	    info = info.replace("" + info.charAt(0) + info.charAt(1) + info.charAt(2) + "¤", "");
	    try {
		listener.processPacket(id, info, receivePacket.getAddress().getHostAddress(), receivePacket.getPort());
	    } catch (ProtocolUnknownPacketException ex) {
		listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Bad packet received.");
	    }
	}
	listener.getLogger().log(Level.SEVERE, "[" + listener.getModule() + "] Closed packet listening thread.");
    }
}
