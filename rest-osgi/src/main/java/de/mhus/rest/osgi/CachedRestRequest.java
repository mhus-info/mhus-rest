package de.mhus.rest.osgi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.mhus.rest.core.RestRequest;

public class CachedRestRequest implements RestRequest {

    private Map<String, String[]> parameters;
    private Map<String, String[]> headers;

    public CachedRestRequest(Map<String, String[]> parametersMap, Map<String, String[]> headersMap) {
        this.parameters = parametersMap;
        this.headers = headersMap;
    }

    @Override
    public String getHeader(String key) {
        if (headers == null) return null;
        Object out = headers.get(key);
        if (out == null) return null;
        if (out instanceof String[]) {
            String[] outArray = (String[]) out;
            if (outArray.length > 0) return outArray[0];
            return null;
        }
        return String.valueOf(out);
    }

    @Override
    public String getParameter(String key) {
        if (parameters == null) return null;
        Object out = parameters.get(key);
        if (out == null) return null;
        if (out instanceof String[]) {
            String[] outArray = (String[]) out;
            if (outArray.length > 0) return outArray[0];
            return null;
        }
        return String.valueOf(out);
    }

    @Override
    public Set<String> getParameterNames() {
        if (parameters == null) return Collections.emptySet();
        return parameters.keySet();
    }

    public static RestRequest transformFromLists(Map<String, List<String>> parameterMap,
            Map<String, List<String>> headersMap) {
        Map<String, String[]> parameters = new HashMap<>();
        if (parameterMap != null) {
            for (Entry<String, List<String>> entry : parameterMap.entrySet()) {
                parameters.put(entry.getKey(), entry.getValue().toArray(new String[0]));
            }
        }
        Map<String, String[]> headers = new HashMap<>();
        if (headersMap != null) {
            for (Entry<String, List<String>> entry : headersMap.entrySet()) {
                headers.put(entry.getKey(), entry.getValue().toArray(new String[0]));
            }
        }
        return new CachedRestRequest(parameters, headers);
    }

}
