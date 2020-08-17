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
package de.mhus.rest.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.shiro.AccessApi;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestException;
import de.mhus.rest.core.api.RestResult;
import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;

public abstract class AbstractRestServlet extends HttpServlet {

    private static final String RESULT_TYPE_JSON = "json";
    private static final String RESULT_TYPE_HTTP = "http";

    private static final String PUBLIC_PATH_START = "/public/";
    private static final String PUBLIC_PATH = "/public";

    private Log log = Log.getLog(this);

    private static final long serialVersionUID = 1L;

    private int nextId = 0;
    private LinkedList<RestAuthenticator> authenticators = new LinkedList<>();
    private RestApi restService;
    private CfgString CFG_TRACE_ACTIVE = new CfgString(getClass(), "traceActivation", "");

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // System.out.println(">>> " + req.getPathInfo());
        response.setHeader("Access-Control-Allow-Origin", "*");

        Scope scope = null;
        try {

            final String path = request.getPathInfo();

            SpanContext parentSpanCtx =
                    ITracer.get()
                            .tracer()
                            .extract(
                                    Format.Builtin.HTTP_HEADERS,
                                    new TextMap() {

                                        @Override
                                        public Iterator<Entry<String, String>> iterator() {
                                            final Enumeration<String> enu =
                                                    request.getHeaderNames();
                                            return new Iterator<Entry<String, String>>() {
                                                @Override
                                                public boolean hasNext() {
                                                    return enu.hasMoreElements();
                                                }

                                                @Override
                                                public Entry<String, String> next() {
                                                    final String key = enu.nextElement();
                                                    return new Entry<String, String>() {

                                                        @Override
                                                        public String getKey() {
                                                            return key;
                                                        }

                                                        @Override
                                                        public String getValue() {
                                                            return request.getHeader(key);
                                                        }

                                                        @Override
                                                        public String setValue(String value) {
                                                            return null;
                                                        }
                                                    };
                                                }
                                            };
                                        }

                                        @Override
                                        public void put(String key, String value) {}
                                    });

            String trace = request.getParameter("_trace");
            if (MString.isEmpty(trace)) trace = CFG_TRACE_ACTIVE.value();

            if (parentSpanCtx == null) {
                scope = ITracer.get().start("rest", trace);
            } else if (parentSpanCtx != null) {
                scope =
                        ITracer.get()
                                .tracer()
                                .buildSpan("rest")
                                .asChildOf(parentSpanCtx)
                                .startActive(true);
            }

            if (MString.isSet(trace)) ITracer.get().activate(trace);

            if (scope != null) {
                Tags.SPAN_KIND.set(scope.span(), Tags.SPAN_KIND_SERVER);
                Tags.HTTP_METHOD.set(scope.span(), request.getMethod());
                Tags.HTTP_URL.set(scope.span(), request.getRequestURL().toString());
            }

            if (path == null || path.length() < 1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // authenticate
            AuthenticationToken token = null;
            for (RestAuthenticator authenticator : authenticators) {
                token = authenticator.authenticate(request);
                if (token != null) break;
            }

            // create shiro Subject and execute
            final AuthenticationToken finalToken = token;
            Subject subject = M.l(AccessApi.class).createSubject();
            subject.execute(() -> serviceInSession(request, response, path, finalToken));

        } finally {
            if (scope != null) scope.close();
        }
    }

    private Object serviceInSession(
            HttpServletRequest req,
            HttpServletResponse resp,
            String path,
            AuthenticationToken authToken)
            throws IOException {

        M.l(AccessApi.class).updateSessionLastAccessTime();

        // id
        long id = newId();
        // subject
        Subject subject = SecurityUtils.getSubject();
        // method
        String method = req.getParameter("_method");
        if (method == null) method = req.getMethod();
        // parts of path
        List<String> parts = new LinkedList<String>(Arrays.asList(path.split("/")));
        if (parts.size() == 0) return null;
        parts.remove(0); // [empty]
        //      parts.remove(0); // rest
        // context
        MProperties context = new MProperties();
        // log access
        logAccess(
                id,
                req.getRemoteAddr(),
                req.getRemotePort(),
                subject,
                method,
                req.getPathInfo(),
                req.getParameterMap());

        // authenticate
        if (authToken != null) {
            try {
                subject.login(authToken);
            } catch (AuthenticationException e) {
                return onLoginFailure(authToken, e, req, resp, id);
            }
        }

        // check for public access
        if (authToken == null && !isPublicPath(path)) {
            return onLoginFailure(req, resp, id);
        }

        // create call context object
        CallContext callContext =
                new CallContext(
                        new HttpRequest(req.getParameterMap()), MHttp.toMethod(method), context);

        RestApi restService = getRestService();

        RestResult res = null;

        if (method.equals(MHttp.METHOD_HEAD)) {
            // nothing more to do
            return null;
        }

        try {
            Node item = restService.lookup(parts, null, callContext);

            if (item == null) {
                sendError(
                        id,
                        req,
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Resource Not Found",
                        null,
                        subject);
                return null;
            }

            if (method.equals(MHttp.METHOD_GET)) {
                res = item.doRead(callContext);
            } else if (method.equals(MHttp.METHOD_POST)) {

                if (callContext.hasAction()) res = item.doAction(callContext);
                else res = item.doCreate(callContext);
            } else if (method.equals(MHttp.METHOD_PUT)) {
                res = item.doUpdate(callContext);
            } else if (method.equals(MHttp.METHOD_DELETE)) {
                res = item.doDelete(callContext);
            } else if (method.equals(MHttp.METHOD_TRACE)) {

            }

            if (res == null) {
                sendError(
                        id, req, resp, HttpServletResponse.SC_NOT_IMPLEMENTED, null, null, subject);
                return null;
            }

            try {
                if (res != null) {
                    log.d("result", id, res);
                    resp.setContentType(res.getContentType());
                    res.write(resp.getWriter());
                }
            } catch (Throwable t) {
                log.d(t);
                sendError(
                        id,
                        req,
                        resp,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        t.getMessage(),
                        t,
                        subject);
                return null;
            }

        } catch (RestException t) {
            log.d(t);
            sendError(id, req, resp, t.getErrorId(), t.getMessage(), t, subject);
            return null;
        } catch (Throwable t) {
            log.d(t);
            sendError(
                    id,
                    req,
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    t.getMessage(),
                    t,
                    subject);
            return null;
        }
        return null;
    }

    public boolean isPublicPath(String path) {
        return path.startsWith(PUBLIC_PATH_START) || path.equals(PUBLIC_PATH);
    }

    public RestApi getRestService() {
        return restService;
    }

    public void setRestService(RestApi service) {
        this.restService = service;
    }

    private Object onLoginFailure(
            AuthenticationToken authToken,
            AuthenticationException e,
            HttpServletRequest req,
            HttpServletResponse resp,
            long id)
            throws IOException {

        resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");
        sendError(id, req, resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage(), e, null);
        return null;
    }

    private Object onLoginFailure(HttpServletRequest req, HttpServletResponse resp, long id)
            throws IOException {

        resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");
        sendError(id, req, resp, HttpServletResponse.SC_UNAUTHORIZED, "", null, null);
        return null;
    }

    private void logAccess(
            long id,
            String remoteAddr,
            int remotePort,
            Subject subject,
            String method,
            String pathInfo,
            @SuppressWarnings("rawtypes") Map parameterMap) {

        String paramLog = getParameterLog(parameterMap);
        log.d(
                "access",
                id,
                "\n Remote: "
                        + remoteAddr
                        + ":"
                        + remotePort
                        + "\n Subject: "
                        + subject
                        + "\n Method: "
                        + method
                        + "\n Request: "
                        + pathInfo
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

    private synchronized long newId() {
        return nextId++;
    }

    private void sendError(
            long id,
            HttpServletRequest req,
            HttpServletResponse resp,
            int errNr,
            String errMsg,
            Throwable t,
            Subject user)
            throws IOException {

        log.d("error", id, errNr, errMsg, t);

        // error result type
        String errorResultType = req.getParameter("_errorResult");
        if (errorResultType == null) errorResultType = RESULT_TYPE_JSON;

        if (errorResultType.equals(RESULT_TYPE_HTTP)) {
            resp.sendError(errNr);
            resp.getWriter().print(errMsg);
            return;
        }

        if (errorResultType.equals(RESULT_TYPE_JSON)) {

            if (errNr == HttpServletResponse.SC_UNAUTHORIZED) resp.setStatus(errNr);
            else resp.setStatus(HttpServletResponse.SC_OK);

            PrintWriter w = resp.getWriter();
            ObjectMapper m = new ObjectMapper();

            ObjectNode json = m.createObjectNode();
            json.put("_sequence", id);
            if (user != null) json.put("_user", String.valueOf(user.getPrincipal()));
            json.put("_error", errNr);
            json.put("_errorMessage", errMsg);
            resp.setContentType("application/json");
            m.writeValue(w, json);

            return;
        }
    }

    public LinkedList<RestAuthenticator> getAuthenticators() {
        return authenticators;
    }
}
