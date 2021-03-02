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
package de.mhus.rest.osgi.nodes;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.aaa.BearerConfiguration;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.VoidNode;
import de.mhus.rest.core.result.JsonResult;

@ServiceComponent(service = RestNodeService.class)
@RestNode(parent = Node.ROOT_PARENT, name = "jwt_token")
public class JwtTokenNode extends VoidNode {

    BearerConfiguration config = new BearerConfiguration(MPeriod.HOUR_IN_MILLISECOUNDS);

    @Override
    protected void doCreate(JsonResult result, CallContext callContext) throws Exception {
        Subject subject = Aaa.getSubject();
        if (subject == null || !subject.isAuthenticated()) {
            result.createObjectNode().put("rc", -1);
            return;
        }
        String token = Aaa.createBearerToken(subject, null, config);
        if (token == null) {
            result.createObjectNode().put("rc", -2);
            return;
        }
        result.createObjectNode().put("token", token).put("rc", 0);
    }
}
