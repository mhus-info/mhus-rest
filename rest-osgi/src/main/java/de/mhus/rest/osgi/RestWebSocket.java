package de.mhus.rest.osgi;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class RestWebSocket implements WebSocketListener {

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.println("RestWebSocket::onWebSocketClose " + statusCode);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        System.out.println("RestWebSocket::onWebSocketBinary");
    }

    @Override
    public void onWebSocketText(String message) {
        System.out.println("RestWebSocket::onWebSocketText");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        System.out.println("RestWebSocket::onWebSocketConnect");
        try {
            session.getRemote().sendString("Hello World");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        System.out.println("RestWebSocket::onWebSocketError");
    }
}
