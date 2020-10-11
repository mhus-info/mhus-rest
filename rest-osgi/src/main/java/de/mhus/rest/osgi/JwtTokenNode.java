package de.mhus.rest.osgi;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.VoidNode;
import de.mhus.rest.core.result.JsonResult;

@ServiceComponent(service = RestNodeService.class)
@RestNode(parent = Node.ROOT_PARENT, name="uid")
public class JwtTokenNode extends VoidNode {

    @Override
    protected void doCreate(JsonResult result, CallContext callContext) throws Exception {
        if (!AccessUtil.isAuthenticated())
            return;
        Subject subject = AccessUtil.getSubject();
        if (subject == null || !subject.isAuthenticated())
            return;
        String token = AccessUtil.createBearerToken(subject, null);
        if (token == null)
            return;
        result.createObjectNode().put("token", token);
    }

}
