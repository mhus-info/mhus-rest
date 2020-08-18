package de.mhus.rest.osgi;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import de.mhus.rest.core.RestRequest;

public class RestRequestWrapper implements RestRequest {

    private HttpServletRequest request;

    public RestRequestWrapper(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public Set<String> getParameterNames() {
        HashSet<String> out = new HashSet<>();
        Enumeration<String> enu = request.getParameterNames();
        while (enu.hasMoreElements())
            out.add(enu.nextElement());
        return out;
    }

}
