package de.mhus.rest.core;

import java.nio.ByteBuffer;

import org.apache.shiro.subject.Subject;

public interface RestSocket {

    void close(int rc, String msg);

    Subject getSubject();

    long getId();

    CallContext getContext();

    boolean isClosed();

    String getNodeId();

    void sendString(String message);

    void sendBytes(ByteBuffer message);
    
}
