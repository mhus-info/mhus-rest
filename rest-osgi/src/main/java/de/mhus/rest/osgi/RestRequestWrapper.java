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

import java.io.IOException;
import java.io.InputStream;
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
        while (enu.hasMoreElements()) out.add(enu.nextElement());
        return out;
    }

    @Override
    public InputStream getLoadContent() {
        try {
            return request.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }
}
