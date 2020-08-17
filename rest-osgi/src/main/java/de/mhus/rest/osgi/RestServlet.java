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

import javax.servlet.Servlet;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.M;
import de.mhus.rest.core.AbstractRestServlet;
import de.mhus.rest.core.RestAuthenticatorByBasicAuth;
import de.mhus.rest.core.RestAuthenticatorByTicket;
import de.mhus.rest.core.api.RestApi;

/*
 * Activate: sb-create de.mhus.rest.osgi.RestServlet
 * Test: http://localhost:8182/rest/public/?_action=ping&_method=POST
 */
@ServiceComponent(
        name = "RestServlet",
        service = Servlet.class,
        property = "alias=/rest/*")
public class RestServlet extends AbstractRestServlet {

    private static final long serialVersionUID = 1L;

    public RestServlet() {
        getAuthenticators().add(new RestAuthenticatorByBasicAuth());
        getAuthenticators().add(new RestAuthenticatorByTicket());
    }

    @Override
    public RestApi getRestService() {
        return M.l(RestApi.class);
    }
}
