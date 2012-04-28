package net.protocol.Listeners;

import java.util.HashMap;
import java.util.logging.Logger;
import net.Protocol.Packets.RegisteredProtocolPacket;
import net.Protocol.Util.RegisteredPacketThread;
import net.protocol.Exceptions.ProtocolPacketAlreadyRegisteredException;
import net.protocol.Exceptions.ProtocolPacketNotRegisteredException;
import net.protocol.Exceptions.ProtocolUnknownPacketException;

public class RegisteredPacketListener {

    private int port;
    private String module;
    private Logger logger;
    private boolean running=false;;
    private HashMap<Integer, RegisteredProtocolPacket> packets = new HashMap<Integer, RegisteredProtocolPacket>();

    public RegisteredPacketListener(String module, Logger logger, int port) {
	this.port = port;
	this.module = module;
	this.logger = logger;
    }

    public int getPort() {
	return port;
    }

    public String getModule() {
	return module;
    }

    public Logger getLogger() {
	return logger;
    }

    public void registerPacket(RegisteredProtocolPacket packet) throws ProtocolPacketAlreadyRegisteredException {
	if (packets.containsKey(packet.getID())) {
	    throw new ProtocolPacketAlreadyRegisteredException();
	}
	packets.put(packet.getID(), packet);
    }

    public void unregisterPacket(RegisteredProtocolPacket packet) throws ProtocolPacketNotRegisteredException {
	if (!packets.containsKey(packet.getID())) {
	    throw new ProtocolPacketNotRegisteredException();
	}
	packets.remove(packet.getID());
    }

    public void processPacket(int id, String data, String ip, int port) throws ProtocolUnknownPacketException {
	if (!packets.containsKey(id)) {
	    return;
	}
	RegisteredProtocolPacket packet = packets.get(id);
	if (!data.contains("¤")) {
	    throw new ProtocolUnknownPacketException();
	}
	String[] dataArray;
	if (packet.getExpectedReturnLength() > 1) {
	    if (!data.contains("ª")) {
		throw new ProtocolUnknownPacketException();
	    }
	    dataArray = data.split("¤")[0].split("ª");
	    if (data.length() != packet.getExpectedReturnLength()) {
		throw new ProtocolUnknownPacketException();
	    }
	} else {
	    dataArray = new String[1];
	    dataArray[0] = data.split("¤")[0];
	}
	packets.get(id).registeredExecute(dataArray, ip, port);
    }

    public void start() {
	running=true;
	RegisteredPacketThread.main(this);
    }
    
    public void stop() {
	running=false;
    }
    
    public boolean isRunning(){
	return running;
    }
}
