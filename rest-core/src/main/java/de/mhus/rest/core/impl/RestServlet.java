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
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.basics.RC;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.aaa.AccessApi;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.TraceJsonMap;
import de.mhus.lib.core.util.Provider;
import de.mhus.lib.errors.AccessDeniedException;
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
import de.mhus.rest.core.api.RestException;
import de.mhus.rest.core.api.RestResult;
import de.mhus.rest.core.api.RestTranslationService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

/*
 * Activate: blue-create de.mhus.rest.osgi.RestServlet
 * Test: http://localhost:8182/rest/public/?_action=ping&_method=POST
 */
@ServiceComponent(name = "RestServlet", service = Servlet.class, property = "alias=/rest/*")
public class RestServlet extends HttpServlet {

    private static final String RESULT_TYPE_JSON = "json";
    private static final String RESULT_TYPE_HTTP = "http";

    private static final String PUBLIC_PATH_START = "/public/";
    private static final String PUBLIC_PATH = "/public";

    private Log log = Log.getLog(this);

    private static final long serialVersionUID = 1L;

    private int nextId = 0;
    private LinkedList<RestAuthenticator> authenticators = new LinkedList<>();
    private CfgString CFG_TRACE_ACTIVE = new CfgString(getClass(), "traceActivation", "");
    private CfgBoolean CFG_TRACE_FOLLOW = new CfgBoolean(getClass(), "traceFollow", true);
    private CfgBoolean CFG_TRACE_PATH = new CfgBoolean(getClass(), "tracePath", true);
    private CfgBoolean CFG_TRACE_PARAM = new CfgBoolean(getClass(), "traceParam", true);
    private CfgBoolean CFG_HEADER_TAGS = new CfgBoolean(getClass(), "traceHeader", true);
    private CfgBoolean CFG_TRACE_RETURN = new CfgBoolean(getClass(), "traceReturn", true);

    private CfgString CFG_CORS_ORIGIN = new CfgString(getClass(), "corsOrigin", "*");
    private CfgString CFG_CORS_HEADERS = new CfgString(getClass(), "corsHeaders", "*");

    public RestServlet() {
        doInitialize();
    }

    protected void doInitialize() {
        getAuthenticators().add(new RestAuthenticatorByJwt());
        getAuthenticators().add(new RestAuthenticatorByTicket());
        getAuthenticators().add(new RestAuthenticatorByBasicAuth());
    }

    public RestApi getRestService() {
        return M.l(RestApi.class);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // System.out.println(">>> " + req.getPathInfo());
        response.setHeader("Access-Control-Allow-Origin", CFG_CORS_ORIGIN.value());
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, HEAD, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", CFG_CORS_HEADERS.value());
        response.setHeader("Access-Control-Max-Age", "0");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Vary", "*");

        response.setCharacterEncoding(MString.CHARSET_UTF_8); // default

        final RestApi restService = getRestService();
        if (!restService.checkSecurityRequest(request, response)) {
            log.d("request blocked by security");
            return;
        }

        Scope scope = null;
        try {

            final String path = request.getPathInfo();

            if (path == null || path.length() < 1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // tracing
            SpanContext parentSpanCtx = null;
            if (CFG_TRACE_FOLLOW.value()) {
                parentSpanCtx =
                        ITracer.get()
                                .tracer()
                                .extract(
                                        Format.Builtin.HTTP_HEADERS, new TraceExtractRest(request));
            }
            String trace = request.getParameter("_trace");
            if (MString.isEmpty(trace)) trace = CFG_TRACE_ACTIVE.value();

            if (parentSpanCtx == null) {
                scope = ITracer.get().start("rest", trace);
            } else if (parentSpanCtx != null) {
                Span span =
                        ITracer.get().tracer().buildSpan("rest").asChildOf(parentSpanCtx).start();
                scope = ITracer.get().activate(span);
            }

            if (MString.isSet(trace)) ITracer.get().activate(trace);

            if (scope != null) {
                // method
                String method = request.getParameter("_method");
                if (method == null) method = request.getMethod();
                method = method.toUpperCase();
                Span span = ITracer.get().current();
                Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_SERVER);
                Tags.HTTP_METHOD.set(span, method);
                Tags.HTTP_URL.set(span, request.getRequestURL().toString());
                span.setTag("http.remote", getRestService().getRemoteAddress(request));
                String pi = request.getPathInfo();
                if (CFG_TRACE_PATH.value()) {
                    if (pi != null) {
                        int i = 0;
                        for (String part : pi.split("/")) {
                            span.setTag("urlpart" + i, part);
                            i++;
                        }
                    }
                }
                if (CFG_TRACE_PARAM.value()) {
                    Map<String, String[]> map = request.getParameterMap();
                    if (map != null) {
                        for (Map.Entry<String, String[]> me : map.entrySet())
                            span.setTag("param_" + me.getKey(), arrayToString(me.getValue()));
                    }
                }
                if (CFG_HEADER_TAGS.value()) {
                    Enumeration<String> enu = request.getHeaderNames();
                    while (enu.hasMoreElements()) {
                        String name = enu.nextElement();
                        StringBuilder sb = null;
                        if ("Authorization".equals(name)) {
                            sb = new StringBuilder();
                            String v = MString.beforeIndex(request.getHeader(name), ' ');
                            sb.append(v);
                            sb.append(" ***");
                        } else {
                            Enumeration<String> enu2 = request.getHeaders(name);
                            while (enu2.hasMoreElements()) {
                                String value = enu2.nextElement();
                                if (sb == null) sb = new StringBuilder();
                                else sb.append(",");
                                sb.append(value);
                            }
                        }
                        if (sb != null) span.setTag("header_" + name, sb.toString());
                    }
                }
            }

            // method
            String method = request.getParameter("_method");
            if (method == null) method = request.getMethod();
            method = method.toUpperCase();

            if (method.equals(MHttp.METHOD_OPTIONS)) {
                // nothing more to do
                return;
            }
            final String finalMethod = method;

            // authenticate - find login token
            AuthenticationToken token = null;
            RestRequest restRequest = new RestRequestWrapper(request);
            for (RestAuthenticator authenticator : authenticators) {
                token = authenticator.authenticate(restRequest);
                if (token != null) break;
            }

            // create shiro Subject and execute
            final AuthenticationToken finalToken = token;
            Subject subject = M.l(AccessApi.class).createSubject();
            subject.execute(
                    () ->
                            serviceInSession(
                                    request, response, path, finalMethod, finalToken, restService));

        } finally {
            if (scope != null) scope.close();
        }
    }

    private String arrayToString(String[] value) {
        if (value == null) return "null";
        if (value.length == 0) return "";
        if (value.length == 1) return value[0];
        return Arrays.toString(value);
    }

    private Object serviceInSession(
            HttpServletRequest req,
            HttpServletResponse resp,
            String path,
            String method,
            AuthenticationToken authToken,
            RestApi restService)
            throws IOException {

        M.l(AccessApi.class).updateSessionLastAccessTime();

        // id
        long id = newId();
        // subject
        Subject subject = SecurityUtils.getSubject();
        // parts of path
        List<String> parts = new LinkedList<String>(Arrays.asList(path.split("/")));
        if (parts.size() == 0) return null;
        parts.remove(0); // [empty]
        //      parts.remove(0); // rest

        // authenticate - login
        if (authToken != null) {
            try {
                Aaa.login(subject, authToken);
            } catch (AuthenticationException e) {
                return onLoginFailure(authToken, e, req, resp, id);
            }
        }

        // check for public access
        if (authToken == null && !isPublicPath(path)) {
            return onLoginFailure(req, resp, id);
        }

        Map<String, String[]> parameters = req.getParameterMap();
        // check for payload and overlay parameters
        // TODO implement payload
        //        String body = req.getReader().lines()
        //                .reduce("", (accumulator, actual) -> accumulator + actual);

        // create call context object
        CallContext callContext =
                new CallContext(
                        req,
                        resp,
                        new CachedRestRequest(
                                parameters,
                                null,
                                new Provider<InputStream>() {

                                    @Override
                                    public InputStream get() {
                                        try {
                                            return req.getInputStream();
                                        } catch (IOException e) {
                                            log.d(e);
                                            return null;
                                        }
                                    }
                                }),
                        MHttp.toMethod(method),
                        CFG_TRACE_RETURN.value());

        RestResult res = null;

        if (method.equals(MHttp.METHOD_HEAD)) {
            // nothing more to do
            return null;
        }

        try {
            if (!restService.checkSecurityPrepared(callContext)) {
                log.d("request blocked by security", id, path);
                return null;
            }

            Node item = restService.lookup(parts, null, callContext);

            if (item == null) {
                sendError(
                        id,
                        req,
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Resource Not Found",
                        null,
                        null,
                        subject);
                return null;
            }

            // log access
            logAccess(
                    id,
                    getRestService().getRemoteAddress(req),
                    req.getRemotePort(),
                    subject,
                    method,
                    req.getPathInfo(),
                    req.getParameterMap());

            if (method.equals(MHttp.METHOD_GET)) {
                restService.checkPermission(item, "read", callContext);
                res = item.doRead(callContext);
            } else if (method.equals(MHttp.METHOD_POST)) {

                if (callContext.hasAction()) {
                    restService.checkPermission(item, callContext.getAction(), callContext);
                    res = item.doAction(callContext);
                } else {
                    restService.checkPermission(item, "create", callContext);
                    res = item.doCreate(callContext);
                }
            } else if (method.equals(MHttp.METHOD_PUT)) {
                restService.checkPermission(item, "update", callContext);
                res = item.doUpdate(callContext);
            } else if (method.equals(MHttp.METHOD_DELETE)) {
                restService.checkPermission(item, "delete", callContext);
                res = item.doDelete(callContext);
            } else if (method.equals(MHttp.METHOD_TRACE)) {

            }

            if (res == null) {
                sendError(
                        id,
                        req,
                        resp,
                        HttpServletResponse.SC_NOT_IMPLEMENTED,
                        "unknown request type",
                        null,
                        null,
                        subject);
                return null;
            }

            try {
                if (res != null) {
                    //                    resp.setHeader("Encapsulated", "result");
                    if (!restService.checkSecurityResult(callContext, res)) {
                        log.d("result blocked by security", id, res);
                        return null;
                    }
                    log.d("result", id, res);
                    int rc = res.getReturnCode();
                    if (rc < 0)                      // should not happen any more - legacy
                        resp.setStatus(RC.normalize(-rc));
                    else
                    if (rc == 0)                     // legacy
                        resp.setStatus(RC.OK);
                    else                             // default
                        resp.setStatus(RC.normalize(rc));
                    resp.setContentType(res.getContentType(callContext));
                    res.write(callContext, resp.getWriter());
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
                        null,
                        subject);
                return null;
            }

        } catch (AccessDeniedException e) {
            log.d(e);
            sendError(id, req, resp, 404, e.getMessage(), e, null, subject);
            return null;
        } catch (RestException t) {
            log.d(t);
            sendError(
                    id,
                    req,
                    resp,
                    t.getReturnCode(),
                    t.getMessage(),
                    t,
                    t.getParameters(),
                    subject);
            return null;
        } catch (MException t) {
            log.d(t);
            sendError(id, req, resp, t.getReturnCode(), t.getMessage(), t, null, subject);
        } catch (MRuntimeException t) {
            log.d(t);
            sendError(id, req, resp, t.getReturnCode(), t.getMessage(), t, null, subject);
        } catch (Throwable t) {
            log.d(t);
            sendError(
                    id,
                    req,
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    t.getMessage(),
                    t,
                    null,
                    subject);
            return null;
        }
        return null;
    }

    public boolean isPublicPath(String path) {
        return path.startsWith(PUBLIC_PATH_START) || path.equals(PUBLIC_PATH);
    }

    private Object onLoginFailure(
            AuthenticationToken authToken,
            AuthenticationException e,
            HttpServletRequest req,
            HttpServletResponse resp,
            long id)
            throws IOException {

        resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");
        sendError(
                id, req, resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage(), e, null, null);
        return null;
    }

    private Object onLoginFailure(HttpServletRequest req, HttpServletResponse resp, long id)
            throws IOException {

        resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");
        sendError(id, req, resp, HttpServletResponse.SC_UNAUTHORIZED, "", null, null, null);
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
                "restaccess",
                id,
                (subject == null ? "?" : subject.getPrincipal()),
                ITracer.get().getCurrentId(),
                method,
                pathInfo,
                "\n Remote: "
                        + remoteAddr
                        + ":"
                        + remotePort
                        + "\n Subject: "
                        + (subject == null ? "?" : subject.getPrincipal())
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
            IReadProperties parameters,
            Subject user)
            throws IOException {

        log.d("error", id, errNr, errMsg, t);

        if (errMsg == null && t != null) errMsg = t.getMessage();
        if (errMsg == null && t != null) errMsg = t.getClass().getSimpleName();

        // error result type
        String errorResultType = req.getParameter("_errorResult");
        if (errorResultType == null) errorResultType = RESULT_TYPE_JSON;

        if (errorResultType.equals(RESULT_TYPE_HTTP)) {
            if (!resp.isCommitted()) resp.sendError(RC.normalize(errNr), errMsg);
            //            resp.getWriter().print(errMsg);
            return;
        }

        if (errorResultType.equals(RESULT_TYPE_JSON)) {

            if (!resp.isCommitted()) resp.setStatus(RC.normalize(errNr));

            PrintWriter w = resp.getWriter();
            ObjectMapper m = new ObjectMapper();

            ObjectNode json = m.createObjectNode();
            if (parameters != null)
                parameters.forEach(entry -> MJson.setValue(json, entry.getKey(), entry.getValue()));
            json.put("_timestamp", System.currentTimeMillis());
            json.put("_sequence", id);
            if (user != null) json.put("_user", String.valueOf(user.getPrincipal()));
            json.put("_error", errNr);
            json.put("_errorMessage", errMsg);
            Locale locale = req.getLocale();
            if (errMsg != null && errMsg.startsWith("[") && errMsg.endsWith("]")) {
                try {
                    JsonNode errArray = MJson.load(errMsg);
                    json.set("_errorArray", errArray);

                    // if array was successful, translate error message
                    String localized = translateError(locale, errArray);
                    if (localized != null) json.put("_errorMessage", localized);
                } catch (Throwable t2) {
                }
            } else {
                String localized = translateError(locale, errMsg);
                if (localized != null) {
                    json.put("_errorMessage", localized);
                    json.put("_errorString", errMsg);
                }
            }
            if (CFG_TRACE_RETURN.value() && ITracer.get().current() != null)
                try {
                    ITracer.get()
                            .tracer()
                            .inject(
                                    ITracer.get().current().context(),
                                    Format.Builtin.TEXT_MAP,
                                    new TraceJsonMap(json, "_"));
                } catch (Throwable t2) {
                    log.d(t2);
                }
            resp.setContentType("application/json");
            m.writeValue(w, json);

            return;
        }
    }

    protected String translateError(Locale locale, JsonNode errArray) {
        RestApi service = getRestService();
        if (service == null) return null;
        RestTranslationService translator = service.getTranslationService();
        if (translator == null) return null;
        return translator.translateError(locale, errArray);
    }

    protected String translateError(Locale locale, String msg) {
        RestApi service = getRestService();
        if (service == null) return null;
        RestTranslationService translator = service.getTranslationService();
        if (translator == null) return null;
        return translator.translateError(locale, msg);
    }

    public LinkedList<RestAuthenticator> getAuthenticators() {
        return authenticators;
    }
}
