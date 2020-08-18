package de.mhus.rest.osgi;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.shiro.subject.Subject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.RestSocket;
import io.opentracing.Scope;

public class RestWebSocket implements WebSocketListener, RestSocket {

    Session session;
    Subject subject;
    Scope scope;
    RestWebSocketServlet servlet;
    long id;
    String node;
    CallContext context;
    
    public RestWebSocket(RestWebSocketServlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        servlet.onWebSocketClose(this, statusCode, reason);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        servlet.onWebSocketBinary(this, payload, offset, len);
    }

    @Override
    public void onWebSocketText(String message) {
        servlet.onWebSocketText(this, message);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        servlet.onWebSocketConnect(this, session);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        servlet.onWebSocketError(this, cause);
    }

    @Override
    public void close(int rc, String msg) {
        servlet.close(this, rc, msg);
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public CallContext getContext() {
        return context;
    }

    @Override
    public boolean isClosed() {
        return session == null || !session.isOpen();
    }

    @Override
    public String getNodeId() {
        return node;
    }
    
    @Override
    public String toString() {
        return node + ":" + id;
    }

    @Override
    public void sendString(String message) {
        if (isClosed()) return;
        try {
            session.getRemote().sendString(message);
        } catch (IOException e) {
            // TODO log? close?
        }
    }
    
    @Override
    public void sendBytes(ByteBuffer message) {
        if (isClosed()) return;
        try {
            session.getRemote().sendBytes(message);
        } catch (IOException e) {
            // TODO log? close?
        }
    }

}
