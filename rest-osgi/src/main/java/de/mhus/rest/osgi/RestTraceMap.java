package de.mhus.rest.osgi;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import io.opentracing.propagation.TextMap;

public class RestTraceMap implements TextMap {

    private HttpServletRequest request;

    public RestTraceMap(HttpServletRequest request) {
        this.request = request;
    }

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
    public void put(String key, String value) {
    }

}
