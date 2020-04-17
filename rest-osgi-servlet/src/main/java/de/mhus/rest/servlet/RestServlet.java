/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.rest.servlet;

import javax.servlet.Servlet;

import org.osgi.service.component.annotations.Component;

import de.mhus.rest.core.AbstractRestServlet;

/*
 * Test: http://localhost:8182/rest/public/?_action=ping&_method=POST
 */
@Component(
        immediate = true,
        name = "RestServlet",
        service = Servlet.class,
        property = "alias=/rest/*")
public class RestServlet extends AbstractRestServlet {

    private static final long serialVersionUID = 1L;

}
