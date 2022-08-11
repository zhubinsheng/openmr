package com.bl.unitybridge;

import com.apkfuns.logutils.LogUtils;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class SimpleServer extends WebSocketServer {

	public void setMsgCallback(MsgCallback msgCallback) {
		this.msgCallback = msgCallback;
	}

	public interface MsgCallback{
		default void onMessage(WebSocket conn, String message){

		}
		public void onMessage(WebSocket conn, ByteBuffer message);
	}

	public SimpleServer(InetSocketAddress address) {
		super(address);
	}

	private MsgCallback msgCallback;

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		conn.send("Welcome to the server!"); //This method sends a message to the new client
		broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
		System.out.println("new connection to " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("received message from "	+ conn.getRemoteSocketAddress() + ": " + message);
		msgCallback.onMessage(conn, message);
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message ) {
		System.out.println("received ByteBuffer from "	+ conn.getRemoteSocketAddress());
		msgCallback.onMessage(conn, message);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		try {
			ex.printStackTrace();
			System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onStart() {
		System.out.println("server started successfully");
	}

}