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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.mhus.lib.core.util.Provider;
import de.mhus.rest.core.RestRequest;

public class CachedRestRequest implements RestRequest {

    private Map<String, String[]> parameters;
    private Map<String, String[]> headers;
    private Provider<InputStream> loadProvider;

    public CachedRestRequest(
            Map<String, String[]> parametersMap,
            Map<String, String[]> headersMap,
            Provider<InputStream> loadProvider) {
        this.parameters = parametersMap;
        this.headers = headersMap;
        this.loadProvider = loadProvider;
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

    public static RestRequest transformFromLists(
            Map<String, List<String>> parameterMap,
            Map<String, List<String>> headersMap,
            Provider<InputStream> loadProvider) {
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
        return new CachedRestRequest(parameters, headers, loadProvider);
    }

    @Override
    public InputStream getLoadContent() {
        return loadProvider == null ? null : loadProvider.get();
    }
}
