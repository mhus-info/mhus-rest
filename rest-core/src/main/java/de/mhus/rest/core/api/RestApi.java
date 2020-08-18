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
package de.mhus.rest.core.api;

import java.util.List;
import java.util.Map;

import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.RestSocket;

public interface RestApi {

    Node lookup(List<String> parts, Class<? extends Node> lastNode, CallContext context)
            throws Exception;

    Map<String, RestNodeService> getRestNodeRegistry();

    String getNodeId(Node node);

    Node getNode(String ident);

    void unregister(RestSocket socket);

    void register(RestSocket socket);
    
}
