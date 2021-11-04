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
package de.mhus.rest.core.nodes;

import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.aaa.BearerConfiguration;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.VoidNode;
import de.mhus.rest.core.result.JsonResult;

@ServiceComponent(service = RestNodeService.class)
@RestNode(parent = Node.ROOT_PARENT, name = "jwt_token")
public class JwtTokenNode extends VoidNode {

    private CfgString CFG_ROLE_FILTER = new CfgString(JwtTokenNode.class, "roleFilter", "");
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
        ObjectNode obj = result.createObjectNode();
        obj.put("token", token).put("rc", 0);

        if (MString.isSet(CFG_ROLE_FILTER.value())) {
            ArrayNode roles = MJson.createArrayNode();
            obj.set("roles", roles);
            for (String role : Aaa.getRoles(Aaa.getPrincipal()))
                if (role.matches(CFG_ROLE_FILTER.value()))
                    roles.add(role);
        }
    }
}
