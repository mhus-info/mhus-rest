package de.mhus.rest.osgi;

import javax.servlet.Servlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import de.mhus.lib.annotations.service.ServiceComponent;

/*
 * Activate: sb-create de.mhus.rest.osgi.RestWebSocketServlet
 * Test: 
 * curl --include \
     --no-buffer \
     --header "Connection: Upgrade" \
     --header "Upgrade: websocket" \
     --header "Host: localhost:8181" \
     --header "Origin: http://localhost:8181/example-websocket" \
     --header "Sec-WebSocket-Key: SGVsbG8sIHdvcmxkIQ==" \
     --header "Sec-WebSocket-Version: 13" \
     http://localhost:8181/restsocket
 * 
 * websocat ws://localhost:8181/restsocket
 */
@ServiceComponent(
        name = "RestWebSocketServlet",
        service = Servlet.class,
        property = "alias=/restsocket/*")
public class RestWebSocketServlet extends WebSocketServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(RestWebSocket.class);
    }
}
