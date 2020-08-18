package de.mhus.rest.osgi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.websocket.api.UpgradeRequest;

import de.mhus.rest.core.RestRequest;

public class SocketRequestWrapper implements RestRequest {

    private UpgradeRequest request;

    public SocketRequestWrapper(UpgradeRequest request) {
        this.request = request;
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public String getParameter(String name) {
        Map<String, List<String>> map = request.getParameterMap();
        if (map == null) return null;
        List<String> value = map.get(name);
        if (value == null || value.size() < 1) return null;
        return value.get(0);
    }

    @Override
    public Set<String> getParameterNames() {
        return request.getParameterMap().keySet();
    }

}
