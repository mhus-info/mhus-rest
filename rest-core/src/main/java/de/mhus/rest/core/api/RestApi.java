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
import java.util.function.Consumer;

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

    void forEachSocket(Node node, Consumer<RestSocket> f);

    List<String> getSocketIds();

    int getSocketCount(String nodeId);

    void reset();

    void checkPermission(Node item, String string, CallContext callContext);

    /**
     * Check access security after preparing the request for processing. For web requests and
     * web socket requests.
     * 
     * If the request is denied the response must be set by the function.
     * 
     * @param callContext The context of the call
     * @return true if the request is allowed
     */
    boolean checkSecurityPrepared(CallContext callContext);

    /**
     * Check access security before processing the request for web requests and web socket connects.
     * 
     * If the request is denied the response must be set by the function.
     * 
     * @param request HttpServletRequest or RestWebSocket
     * @param response HttpServletResponse or Session
     * @return true if the request is allowed
     */
    boolean checkSecurityRequest(Object request, Object response);

    /**
     * Check security after processing the request. The result could be manipulated if necesary.
     * 
     * @param callContext The context of the call
     * @param result The result or the request
     * @return true if the request is allowed
     */
    boolean checkSecurityResult(CallContext callContext, RestResult result);

    /**
     * Return the real remote address.
     * 
     * @param request HttpServletRequest or RestWebSocket
     * @return Remote IP
     */
    String getRemoteAddress(Object request);
    
}
