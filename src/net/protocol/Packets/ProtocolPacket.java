package net.Protocol.Packets;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.protocol.Exceptions.ProtocolIOException;
import net.protocol.Exceptions.ProtocolSocketException;
import net.protocol.Exceptions.ProtocolUnknownHostException;
import net.protocol.Exceptions.ProtocolUnknownPacketException;

public class ProtocolPacket {

    private int id;
    private int expectedReturnLength;
    private int port;
    private String ip;
    private Logger logger;
    private String module;
    private InetAddress ia;

    public ProtocolPacket(String module, int id, int expectedReturnLength, String ip, int port, Logger logger) throws ProtocolUnknownHostException {
	this.id = id;
	this.expectedReturnLength = expectedReturnLength;
	this.port = port;
	this.ip = ip;
	this.logger = logger;
	this.module = module;

	InetAddress ia = null;
	try {
	    ia = InetAddress.getByName(ip);
	} catch (UnknownHostException ex) {
	    logger.log(Level.WARNING, "[" + module + "] UnknownHostException on packet " + id + "!");
	    throw new ProtocolUnknownHostException();
	}
	this.ia = ia;

    }

    public int getID() {
	return id;
    }

    public String[] communicateWithRetries(String[] dataA, int retries) throws ProtocolSocketException, ProtocolIOException, ProtocolUnknownPacketException {
	for (int ctr = 0; ctr < retries; ctr++) {
	    String[] data = communicate(dataA);
	    if (data != null) {
		return data;
	    }
	}
	return null;
    }

    public String[] communicate(String[] dataA) throws ProtocolSocketException, ProtocolIOException, ProtocolUnknownPacketException {
	String ids="";
	if (id<10){
	    ids="00";
	}
	else if(id<100){
	    ids="0";
	}
	ids=ids+id;
	String dat = "" + ids + "¤";
	boolean first = true;
	for (String s : dataA) {
	    if (first) {
		dat = dat + s;
		first = false;
	    } else {
		dat = dat + ":" + s;
	    }
	}
	dat = dat + "¤";
	byte[] bytes = new byte[1024];
	bytes = dat.getBytes();
	DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ia, port);
	DatagramSocket sender = null;
	try {
	    sender = new DatagramSocket();
	} catch (SocketException ex) {
	    logger.log(Level.WARNING, "[" + module + "] SocketException on packet " + id + "!");
	    throw new ProtocolSocketException();
	}
	try {
	    sender.send(packet);
	} catch (IOException ex) {
	    logger.log(Level.WARNING, "[" + module + "] IOException on packet " + id + "!");
	    throw new ProtocolIOException();
	}
	DatagramSocket serverSocket = null;
	try {
	    serverSocket = new DatagramSocket(port);
	} catch (SocketException ex) {
	    logger.log(Level.SEVERE, "[" + module + "] Socket Exception on port " + port + ", closing thread.");
	    throw new ProtocolSocketException();
	}

	byte[] receiveData = new byte[1024];
	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	try {
	    serverSocket.receive(receivePacket);
	} catch (IOException ex) {
	    logger.log(Level.SEVERE, "[" + module + "] IO Exception on port " + port + ".");
	    throw new ProtocolIOException();
	}
	char[] recieve = new String(receivePacket.getData()).toCharArray();
	if (recieve == null) {
	    return null;
	}

	if (recieve.length < 3) {
	    return null;
	}
	String info = new String(recieve);
	if (info.length() < 3) {
	    throw new ProtocolUnknownPacketException();
	}
	if (!info.contains("¤")) {
	    throw new ProtocolUnknownPacketException();
	}
	int pid = -1;
	try {
	    pid = Integer.parseInt("" + info.charAt(0) + info.charAt(1) + info.charAt(2));
	} catch (NumberFormatException ex) {
	    throw new ProtocolUnknownPacketException();
	}
	if (pid != id) {
	    throw new ProtocolUnknownPacketException();
	}
	String data = info.replace("" + info.charAt(0) + info.charAt(1) + info.charAt(2) + "¤", "");
	if (!data.contains("¤")) {
	    throw new ProtocolUnknownPacketException();
	}
	String[] dataArray;
	if (getExpectedReturnLength() > 1) {
	    if (!data.contains("ª")) {
		throw new ProtocolUnknownPacketException();
	    }
	    dataArray = data.split("¤")[0].split("ª");
	    if (data.length() != getExpectedReturnLength()) {
		throw new ProtocolUnknownPacketException();
	    }
	} else {
	    dataArray = new String[1];
	    dataArray[0] = data.split("¤")[0];
	}
	return dataArray;

    }

    public int getExpectedReturnLength() {
	return expectedReturnLength;
    }
}
