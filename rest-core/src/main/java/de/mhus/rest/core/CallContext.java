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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;

public class CallContext {

    public static final String ACTION_PARAMETER = "_action";

    private RestRequest req;
    private MHttp.METHOD method;
    private IProperties context;
    private RestAuthorisation authorisation;
    private Object orgRequest;
    private Object orgResponse;
    private String nodeIdent;
    private boolean returnTrace;

    public CallContext(
            Object orgRequest,
            Object orgResponse,
            RestRequest req,
            MHttp.METHOD method,
            boolean returnTrace) {
        this.orgRequest = orgRequest;
        this.orgResponse = orgResponse;
        this.req = req;
        this.method = method;
        this.returnTrace = returnTrace;
    }

    public boolean hasAction() {
        return req.getParameter(ACTION_PARAMETER) != null;
    }

    public String getAction() {
        return getParameter(ACTION_PARAMETER);
    }

    public String getParameter(String key) {
        String val = req.getParameter(key);
        return val;
    }

    public int getParameter(String key, int def) {
        String val = req.getParameter(key);
        return MCast.toint(val, def);
    }

    public long getParameter(String key, long def) {
        String val = req.getParameter(key);
        return MCast.tolong(val, def);
    }

    public boolean getParameter(String key, boolean def) {
        String val = req.getParameter(key);
        return MCast.toboolean(val, def);
    }

    public Date getParameterDate(String key, Date def) {
        String val = req.getParameter(key);
        return MCast.toDate(val, def);
    }

    public String getParameter(String key, String def) {
        String val = req.getParameter(key);
        if (val == null) return def;
        return val;
    }

    public IProperties getParameters() {
        MProperties out = new MProperties();
        for (String n : getParameterNames()) out.put(n, getParameter(n));
        return out;
    }

    public Object get(String key) {
        synchronized (this) {
            if (context == null) return null;
        }
        return context.get(key);
    }

    public RestRequest getRequest() {
        return req;
    }

    public void put(String key, Object value) {
        synchronized (this) {
            if (context == null) context = new MProperties();
        }
        context.put(key, value);
    }

    public String[] getNames() {
        synchronized (this) {
            if (context == null) return new String[0];
        }
        return context.keySet().toArray(new String[0]);
    }

    public Set<String> getParameterNames() {
        return req.getParameterNames();
    }

    public MHttp.METHOD getMethod() {
        return method;
    }

    public Node lookup(List<String> parts, Class<? extends Node> lastNode) throws Exception {
        RestApi restService = M.l(RestApi.class);
        return restService.lookup(parts, lastNode, this);
    }

    public RestAuthorisation getAuthorisation() {
        return authorisation;
    }

    public void setAuthorisation(RestAuthorisation authorisation) {
        this.authorisation = authorisation;
    }

    public InputStream getLoadContent() {
        return req.getLoadContent();
    }

    public void setResponseEncoding(String charset) {
        if (orgResponse == null || !(orgResponse instanceof HttpServletResponse))
            throw new NotSupportedException("response is not HttpServletResponse");
        ((HttpServletResponse) orgResponse).setCharacterEncoding(charset);
    }

    public void setResponseHeader(String name, String value) {
        if (orgResponse == null || !(orgResponse instanceof HttpServletResponse))
            throw new NotSupportedException("response is not HttpServletResponse");
        if (value == null) return;
        ((HttpServletResponse) orgResponse).setHeader(name, value);
    }

    public void setResponseHeader(String name, int value) {
        if (orgResponse == null || !(orgResponse instanceof HttpServletResponse))
            throw new NotSupportedException("response is not HttpServletResponse");
        ((HttpServletResponse) orgResponse).setIntHeader(name, value);
    }

    public void setResponseHeader(String name, Date value) {
        if (orgResponse == null || !(orgResponse instanceof HttpServletResponse))
            throw new NotSupportedException("response is not HttpServletResponse");
        if (value == null) return;
        ((HttpServletResponse) orgResponse).setDateHeader(name, value.getTime());
    }

    /**
     * The original request depends on the underlying technology. It could be a HttpServletRequest
     * for Servlets or a jetty UpgradeRequest for WebSocket calls. Sometimes it's needed to interact
     * with the original call objects.
     *
     * @return The original request
     */
    public Object getOriginalRequest() {
        return orgRequest;
    }

    /**
     * The original request depends on the underlying technology. It could be a HttpServletResponse
     * for Servlets or a jetty Session for WebSocket calls. Sometimes it's needed to interact with
     * the original call objects.
     *
     * @return The original response
     */
    public Object getOriginalResponse() {
        return orgResponse;
    }

    public String getNodeIdent() {
        return nodeIdent;
    }

    public void setNodeIdent(String nodeIdent) {
        this.nodeIdent = nodeIdent;
    }

    public boolean isReturnTrace() {
        return returnTrace;
    }
}
