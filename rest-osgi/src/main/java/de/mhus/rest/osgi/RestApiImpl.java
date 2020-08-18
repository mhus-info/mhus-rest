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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.util.WeakMapList;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.RestRegistry;
import de.mhus.rest.core.RestSocket;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestNodeService;

@Component(immediate = true)
public class RestApiImpl extends MLog implements RestApi {

    private BundleContext context;
    private ServiceTracker<RestNodeService, RestNodeService> nodeTracker;
    private RestRegistry register = new RestRegistry();
    private WeakMapList<String, RestSocket> sockets = new WeakMapList<>();

    public static final CfgBoolean RELAXED = new CfgBoolean(RestApi.class, "aaaRelaxed", true);

    @Activate
    public void doActivate(ComponentContext ctx) {

        context = ctx.getBundleContext();
        nodeTracker =
                new ServiceTracker<>(
                        context, RestNodeService.class, new RestNodeServiceTrackerCustomizer());
        nodeTracker.open();
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        nodeTracker.close();
        context = null;
        nodeTracker = null;
        register.getRegistry().clear();
    }

    private class RestNodeServiceTrackerCustomizer
            implements ServiceTrackerCustomizer<RestNodeService, RestNodeService> {

        @Override
        public RestNodeService addingService(ServiceReference<RestNodeService> reference) {

            RestNodeService service = context.getService(reference);
            if (service != null) {
                for (String x : service.getParentNodeCanonicalClassNames()) {
                    if (x != null) {
                        if (x.length() > 0
                                && !x.contains(
                                        ".")) // print a warning - class name without dot should be
                            // a mistake
                            log().w(
                                            "Register RestNode with malformed parent name - should be a class",
                                            service.getClass(),
                                            service.getNodeName(),
                                            x);
                        String key = x + "-" + service.getNodeName();
                        log().i("register", key, service.getClass().getCanonicalName());
                        register.getRegistry().put(key, service);
                    }
                }
            }

            return service;
        }

        @Override
        public void modifiedService(
                ServiceReference<RestNodeService> reference, RestNodeService service) {}

        @Override
        public void removedService(
                ServiceReference<RestNodeService> reference, RestNodeService service) {

            if (service != null) {
                
                String nodeId = service.getNodeName();
                for (String x : service.getParentNodeCanonicalClassNames()) {
                    if (x != null) {
                        String key = x + "-" + nodeId;
                        log().i("unregister", key, service.getClass().getCanonicalName());
                        register.getRegistry().remove(key);
                    }
                }
                sockets.getClone(nodeId).forEach(v -> v.close(HttpServletResponse.SC_RESET_CONTENT, null));
                sockets.remove(nodeId);
            }
        }
    }

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
        //return node instanceof RestNodeService ? ((RestNodeService)node).getNodeId() : node.getClass().getCanonicalName();
        return node.getClass().getCanonicalName();
    }
    
    @Override
    public Node getNode(String ident) {
//        String suffix = "-" + ident;
//        for (Entry<String, RestNodeService> entry : register.getRegistry().entrySet())
//            if (entry.getKey().endsWith(suffix)) return entry.getValue();
        for (RestNodeService entry : register.getRegistry().values())
            if (entry.getClass().getCanonicalName().equals(ident))
                return entry;
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
        list.forEach(v -> {
                if (!v.isClosed())
                    f.accept(v);
//                    v.getSubject().execute(() -> f.accept(v) ); // not needed should be done by caller if recommended
                }
            );
    }

}
