/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.rest.core.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.aaa.AccessApi;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.RestAuthenticator;
import de.mhus.rest.core.RestAuthenticatorByBasicAuth;
import de.mhus.rest.core.RestAuthenticatorByJwt;
import de.mhus.rest.core.RestAuthenticatorByTicket;
import de.mhus.rest.core.RestRequest;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;

/*
* Activate: blue-create de.mhus.rest.osgi.RestWebSocketServlet
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

    private static final String PUBLIC_PATH_START = "/public/";
    private static final String PUBLIC_PATH = "/public";

    private LinkedList<RestAuthenticator> authenticators = new LinkedList<>();
    private CfgString CFG_TRACE_ACTIVE = new CfgString(getClass(), "traceActivation", "");
    private CfgLong CFG_IDLE_TIMEOUT =
            new CfgLong(getClass(), "idleTimeout", MPeriod.HOUR_IN_MILLISECOUNDS);
    private Set<RestWebSocket> sessions = Collections.synchronizedSet(new HashSet<>());
    private Log log = Log.getLog(this);
    private int nextId = 0;

    public RestWebSocketServlet() {
        doInitialize();
    }

    protected void doInitialize() {
        getAuthenticators().add(new RestAuthenticatorByBasicAuth());
        getAuthenticators().add(new RestAuthenticatorByTicket());
        getAuthenticators().add(new RestAuthenticatorByJwt());
    }

    public RestApi getRestService() {
        return M.l(RestApi.class);
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(
                new WebSocketCreator() {
                    @Override
                    public Object createWebSocket(
                            ServletUpgradeRequest req, ServletUpgradeResponse resp) {
                        return new RestWebSocket(RestWebSocketServlet.this);
                    }
                });
    }

    public void onWebSocketConnect(RestWebSocket socket, Session session) {

        try {

            RestApi restService = getRestService();
            if (!restService.checkSecurityRequest(socket, session)) return;

            session.setIdleTimeout(CFG_IDLE_TIMEOUT.value());

            UpgradeRequest request = session.getUpgradeRequest();
            final String path = preparePath(request.getRequestURI().getPath());

            SpanContext parentSpanCtx =
                    ITracer.get()
                            .tracer()
                            .extract(Format.Builtin.HTTP_HEADERS, new SocketTraceMap(request));

            List<String> traceList = request.getParameterMap().get("_trace");
            String trace = traceList == null || traceList.size() < 1 ? null : traceList.get(0);
            if (MString.isEmpty(trace)) trace = CFG_TRACE_ACTIVE.value();

            if (parentSpanCtx == null) {
                socket.scope = ITracer.get().start("rest", trace);
            } else if (parentSpanCtx != null) {
                Span span =
                        ITracer.get().tracer().buildSpan("rest").asChildOf(parentSpanCtx).start();
                socket.scope = ITracer.get().activate(span);
            }

            if (MString.isSet(trace)) ITracer.get().activate(trace);

            if (path == null || path.length() < 1) {
                session.getUpgradeResponse().setStatusCode(HttpServletResponse.SC_NOT_FOUND);
                session.close(HttpServletResponse.SC_NOT_FOUND, "not found");
                session.setIdleTimeout(1000);
                return;
            }

            // authenticate
            AuthenticationToken token = null;
            RestRequest restRequest = new SocketRequestWrapper(request);
            for (RestAuthenticator authenticator : authenticators) {
                token = authenticator.authenticate(restRequest);
                if (token != null) break;
            }

            // create shiro Subject and execute
            Subject subject = M.l(AccessApi.class).createSubject();
            socket.subject = subject;

            if (token != null) {
                try {
                    Aaa.login(subject, token);
                } catch (AuthenticationException e) {
                    onLoginFailure(socket);
                    return;
                }
            }
            // check for public access
            if (token == null && !isPublicPath(path)) {
                onLoginFailure(socket);
                return;
            }

            subject.execute(() -> serviceInSession(socket, path));

        } finally {
            if (!sessions.contains(socket))
                socket.close(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
            if (socket.scope != null) socket.scope.close();
        }
    }

    private String preparePath(String path) {
        if (path == null) return null;
        int pos = path.indexOf('/', 1);
        if (pos < 0) return "";
        return path.substring(pos);
    }

    private void onLoginFailure(RestWebSocket socket) {
        socket.session.getUpgradeResponse().setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");
        socket.session.getUpgradeResponse().setStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
        onError(socket, null, HttpServletResponse.SC_UNAUTHORIZED, null, true);
    }

    void close(RestWebSocket socket, int rc, String msg) {
        if (socket == null) return;
        sessions.remove(socket);
        synchronized (socket) {
            if (socket.isClosed()) return;
            try {
                socket.session.getRemote().sendString("{_action:\"close\",_rc:" + rc + "}\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            socket.session.close(rc, msg);
            socket.session.setIdleTimeout(1000);
            socket.session = null;
            getRestService().unregister(socket);
        }
    }

    private void serviceInSession(RestWebSocket socket, String path) {

        Session session = socket.session;
        UpgradeRequest request = session.getUpgradeRequest();
        M.l(AccessApi.class).updateSessionLastAccessTime();

        // id
        long id = newId();
        socket.id = id;
        session.getUpgradeResponse().addHeader("RemoteIdent", String.valueOf(id));
        // subject
        Subject subject = SecurityUtils.getSubject();
        // parts of path
        List<String> parts = new LinkedList<String>(Arrays.asList(path.split("/")));
        if (parts.size() == 0) {
            onError(socket, null, HttpServletResponse.SC_NOT_FOUND, null, true);
            return;
        }
        parts.remove(0); // [empty]
        //      parts.remove(0); // rest

        // create call context object
        CallContext callContext =
                new CallContext(
                        request,
                        session,
                        CachedRestRequest.transformFromLists(
                                request.getParameterMap(), request.getHeaders(), null),
                        MHttp.METHOD.GET,
                        false);

        socket.context = callContext;
        RestApi restService = getRestService();

        if (!restService.checkSecurityPrepared(callContext)) return;

        try {

            Node item = restService.lookup(parts, null, callContext);

            if (item == null || !item.streamingAccept(socket)) {
                onError(socket, null, HttpServletResponse.SC_NOT_FOUND, "Resource Not Found", true);
                return;
            }

            socket.node = restService.getNodeId(item);
            sessions.add(socket);
            getRestService().register(socket);

            // log access
            String remote = getRestService().getRemoteAddress(socket);
            if (remote == null) remote = session.getRemoteAddress().getHostName();
            logAccess(
                    id,
                    remote,
                    subject,
                    session.getUpgradeRequest().getRequestURI(),
                    session.getUpgradeRequest().getParameterMap());

        } catch (MException t) {
            log.d(t);
            onError(socket, null, t.getReturnCode(), t.getMessage(), true);
            return;
        } catch (MRuntimeException t) {
            log.d(t);
            onError(socket, null, t.getReturnCode(), t.getMessage(), true);
            return;
        } catch (Throwable t) {
            log.d(t);
            onError(socket, t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, true);
            return;
        }

        return;
    }

    private void logAccess(
            long id,
            String remoteAddress,
            Subject subject,
            URI requestURI,
            Map<String, List<String>> parameterMap) {
        String paramLog = getParameterLog(parameterMap);
        log.d(
                "access",
                id,
                "\n Remote: "
                        + remoteAddress
                        + "\n Subject: "
                        + subject
                        + "\n Request: "
                        + requestURI
                        + "\n Parameters: "
                        + paramLog
                        + "\n");
    }

    private String getParameterLog(Map<?, ?> parameterMap) {
        StringBuilder out = new StringBuilder().append('{');
        for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
            out.append('\n').append(entry.getKey()).append("=[");
            Object val = entry.getValue();
            if (val == null) {
            } else if (val.getClass().isArray()) {
                boolean first = true;
                Object[] arr = (Object[]) val;
                for (Object o : arr) {
                    if (first) first = false;
                    else out.append(',');
                    out.append(o);
                }
            } else {
                out.append(val);
            }
            out.append("] ");
        }
        out.append('}');
        return out.toString();
    }

    private void onError(RestWebSocket socket, Throwable e, int rc, String msg, boolean close) {
        if (msg == null) {
            if (e != null) msg = e.toString();
            else msg = MHttp.HTTP_STATUS_CODES.getOrDefault(rc, "unknown");
        }
        if (close) close(socket, rc, msg);
    }

    public void onWebSocketError(RestWebSocket socket, Throwable cause) {
        log.d("error", socket, cause);
        synchronized (socket) {
            sessions.remove(socket);
            if (socket.isClosed()) return;
            try {
                socket.session.disconnect(); // or close() ???
            } catch (IOException e) {
            }
            socket.session = null;
            getRestService().unregister(socket);
        }
    }

    public void onWebSocketText(RestWebSocket socket, String message) {
        log.t("text", socket, message);
        synchronized (socket) {
            if (socket.isClosed()) return;
        }
        Node node = getRestService().getNode(socket.node);
        socket.subject.execute(() -> node.streamingText(socket, message));
    }

    public void onWebSocketBinary(RestWebSocket socket, byte[] payload, int offset, int len) {
        log.d("binary", socket, len);
        synchronized (socket) {
            if (socket.isClosed()) return;
        }
        Node node = getRestService().getNode(socket.node);
        socket.subject.execute(() -> node.streamingBinary(socket, payload, offset, len));
    }

    public void onWebSocketClose(RestWebSocket socket, int statusCode, String reason) {
        synchronized (socket) {
            sessions.remove(socket);
            socket.session = null;
            getRestService().unregister(socket);
        }
    }

    public LinkedList<RestAuthenticator> getAuthenticators() {
        return authenticators;
    }

    private synchronized long newId() {
        return nextId++;
    }

    public boolean isPublicPath(String path) {
        return path.startsWith(PUBLIC_PATH_START) || path.equals(PUBLIC_PATH);
    }
}
