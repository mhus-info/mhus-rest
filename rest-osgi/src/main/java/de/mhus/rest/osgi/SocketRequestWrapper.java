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

    @Override
    public InputStream getLoadContent() {
        return null;
    }
}
