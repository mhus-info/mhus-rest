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
package de.mhus.rest.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.util.WeakMapList;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.RestRegistry;
import de.mhus.rest.core.RestSocket;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestNodeService;

public abstract class AbstractRestApi extends MLog implements RestApi {

    protected RestRegistry register = new RestRegistry();
    protected WeakMapList<String, RestSocket> sockets = new WeakMapList<>();

    public static final CfgBoolean RELAXED = new CfgBoolean(RestApi.class, "aaaRelaxed", true);

    @Override
    public Map<String, RestNodeService> getRestNodeRegistry() {
        return register.getRegistry();
    }

    @Override
    public Node lookup(List<String> parts, Class<? extends Node> lastNode, CallContext context)
            throws Exception {
        return register.lookup(parts, lastNode, context);
    }

    @Override
    public String getNodeId(Node node) {
        // return node instanceof RestNodeService ? ((RestNodeService)node).getNodeId() :
        // node.getClass().getCanonicalName();
        return node.getClass().getCanonicalName();
    }

    @Override
    public Node getNode(String ident) {
        //        String suffix = "-" + ident;
        //        for (Entry<String, RestNodeService> entry : register.getRegistry().entrySet())
        //            if (entry.getKey().endsWith(suffix)) return entry.getValue();
        for (RestNodeService entry : register.getRegistry().values())
            if (entry.getClass().getCanonicalName().equals(ident)) return entry;
        return null;
    }

    @Override
    public void unregister(RestSocket socket) {
        String nodeId = socket.getNodeId();
        synchronized (sockets) {
            sockets.removeEntry(nodeId, socket);
        }
    }

    @Override
    public void register(RestSocket socket) {
        String nodeId = socket.getNodeId();
        synchronized (sockets) {
            sockets.putEntry(nodeId, socket);
        }
    }

    @Override
    public void forEachSocket(Node node, Consumer<RestSocket> f) {
        String nodeId = getNodeId(node);
        List<RestSocket> list = null;
        synchronized (sockets) {
            list = sockets.getClone(nodeId);
        }
        list.forEach(
                v -> {
                    if (!v.isClosed()) f.accept(v);
                    //                    v.getSubject().execute(() -> f.accept(v) ); // not needed
                    // should be done by caller if recommended
                });
    }

    @Override
    public List<String> getSocketIds() {
        return new ArrayList<>(sockets.keySet());
    }

    @Override
    public int getSocketCount(String nodeId) {
        List<RestSocket> list = sockets.get(nodeId);
        if (list == null) return 0;
        return list.size();
    }

}
