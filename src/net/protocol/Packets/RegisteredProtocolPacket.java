package net.Protocol.Packets;

import net.Protocol.Util.RegisteredPacketThread;

public class RegisteredProtocolPacket {

    private int id;
    private int expectedReturnLength;

    public RegisteredProtocolPacket(int id, int expectedReturnLength) {
	this.id = id;
	this.expectedReturnLength = expectedReturnLength;
    }

    public int getID() {
	return id;
    }

    public void registeredExecute(String[] data, String ip, int port) {
	//insert code here
    }

    public int getExpectedReturnLength() {
	return expectedReturnLength;
    }
}
