package de.mhus.rest.osgi;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jetty.websocket.api.UpgradeRequest;

import io.opentracing.propagation.TextMap;

public class SocketTraceMap implements TextMap {

    private UpgradeRequest request;

    public SocketTraceMap(UpgradeRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        final Iterator<Entry<String, List<String>>> iter = request.getHeaders().entrySet().iterator();
        return new Iterator<Entry<String, String>>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Entry<String, String> next() {
                
                final Entry<String, List<String>> entry = iter.next();
                return new Entry<String, String>() {

                    @Override
                    public String getKey() {
                        return entry.getKey();
                    }

                    @Override
                    public String getValue() {
                        if (entry.getValue().size() < 1)
                            return null;
                        return entry.getValue().get(0);
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
