package net.Protocol.Packets;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.protocol.Exceptions.ProtocolIOException;
import net.protocol.Exceptions.ProtocolSocketException;
import net.protocol.Exceptions.ProtocolUnknownPacketException;

public class SingleProtocolPacket {

    private int id;
    private int port;
    private String ip;
    private Logger logger;
    private String module;
    private InetAddress ia;

    public SingleProtocolPacket(String module, int id, String ip, int port, Logger logger) {
	this.id = id;
	this.port = port;
	this.ip = ip;
	this.logger = logger;
	this.module = module;

	InetAddress ia = null;
	try {
	    ia = InetAddress.getByName(ip);
	} catch (UnknownHostException ex) {
	    logger.log(Level.WARNING, "[" + module + "] UnknownHostException on packet " + id + "!");
	    return;
	}
	this.ia = ia;

    }

    public int getID() {
	return id;
    }

    public void send(String[] dataA) throws ProtocolSocketException, ProtocolIOException {
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
    }
}
