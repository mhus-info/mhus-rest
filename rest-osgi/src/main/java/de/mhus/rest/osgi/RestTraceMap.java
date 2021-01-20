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
        final Enumeration<String> enu = request.getHeaderNames();
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
}
