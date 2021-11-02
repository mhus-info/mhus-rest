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

import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.mhus.osgi.api.MOsgi;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.api.RestSecurityService;
import de.mhus.rest.core.impl.AbstractRestApi;

@Component(immediate = true, service = RestApi.class)
public class RestApiImpl extends AbstractRestApi {

    private BundleContext context;
    private ServiceTracker<RestNodeService, RestNodeService> nodeTracker;
    private ServiceTracker<RestSecurityService, RestSecurityService> securityTracker;
    public RestSecurityService securityService;
    public ServiceReference<RestSecurityService> securityReference;

    @Activate
    public void doActivate(ComponentContext ctx) {

        context = ctx.getBundleContext();
        nodeTracker =
                new ServiceTracker<>(
                        context, RestNodeService.class, new RestNodeServiceTrackerCustomizer());
        nodeTracker.open();

        securityTracker =
                new ServiceTracker<>(
                        context,
                        RestSecurityService.class,
                        new RestSecurityServiceTrackerCustomizer());
        securityTracker.open();
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        nodeTracker.close();
        securityTracker.close();
        context = null;
        nodeTracker = null;
        securityTracker = null;
        securityReference = null;
        securityService = null;
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
                sockets.getClone(nodeId)
                        .forEach(v -> v.close(HttpServletResponse.SC_RESET_CONTENT, null));
                sockets.remove(nodeId);
            }
        }
    }

    private class RestSecurityServiceTrackerCustomizer
            implements ServiceTrackerCustomizer<RestSecurityService, RestSecurityService> {

        @Override
        public RestSecurityService addingService(ServiceReference<RestSecurityService> reference) {

            RestSecurityService service = context.getService(reference);
            if (service != null) {
                if (securityReference != null)
                    log().i("Drop Rest Security", securityReference.getBundle().getBundleId());
                securityReference = reference;
                securityService = service;
                log().i("Found Rest Security", securityReference.getBundle().getBundleId());
            }

            return service;
        }

        @Override
        public void modifiedService(
                ServiceReference<RestSecurityService> reference, RestSecurityService service) {}

        @Override
        public void removedService(
                ServiceReference<RestSecurityService> reference, RestSecurityService service) {

            if (service != null
                    && (securityReference == null
                            || reference.getBundle().getBundleId()
                                    == securityReference.getBundle().getBundleId())) {
                log().i("Remove Rest Security", securityReference.getBundle().getBundleId());
                securityReference = null;
                securityService = null;
            }
        }
    }

    @Override
    public void reset() {
        register.getRegistry().clear();
        for (RestNodeService service : MOsgi.getServices(RestNodeService.class, null)) {

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
    }

    @Override
    public void checkPermission(Node item, String action, CallContext callContext) {
        register.checkPermission(callContext, action);
    }

    @Override
    public boolean checkSecurityPost(CallContext callContext) {
        RestSecurityService s = securityService;
        if (s == null) {
            if (REQUIRE_SECURITY.value()) {
                log().d("deny access to rest - wait for security");
                callContext.setResponseStatus(503);
                return false;
            }
            return true;
        }
        return s.checkSecurityPost(callContext);
    }

    @Override
    public boolean checkSecurityPre(Object request, Object response) {
        RestSecurityService s = securityService;
        if (s == null) {
            if (REQUIRE_SECURITY.value()) {
                log().d("deny access to rest - wait for security");
                if (response instanceof HttpServletResponse)
                    try {
                        ((HttpServletResponse)response).sendError(503);
                    } catch (IOException e) {
                        log().d(e);
                    }
                return false;
            }
            return true;
        }
        return s.checkSecurityPre(request, response);
    }
}
