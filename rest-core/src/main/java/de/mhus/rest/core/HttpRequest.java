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

import java.util.Map;
import java.util.Set;

public class HttpRequest {

    private Map<String, String[]> parameters;

    public HttpRequest(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    //	public HttpRequest(Map<String,Object> parameters) {
    //		this.parameters =parameters;
    //	}

    public String getParameter(String key) {
        Object out = parameters.get(key);
        if (out == null) return null;
        if (out instanceof String[]) {
            String[] outArray = (String[]) out;
            if (outArray.length > 0) return outArray[0];
            return null;
        }
        return String.valueOf(out);
    }

    public Set<String> getParameterNames() {
        return parameters.keySet();
    }
}
