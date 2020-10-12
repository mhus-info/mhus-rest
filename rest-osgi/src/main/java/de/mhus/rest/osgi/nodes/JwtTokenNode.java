package de.mhus.rest.osgi.nodes;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.shiro.BearerConfiguration;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.VoidNode;
import de.mhus.rest.core.result.JsonResult;

@ServiceComponent(service = RestNodeService.class)
@RestNode(parent = Node.ROOT_PARENT, name="jwt_token")
public class JwtTokenNode extends VoidNode {

    BearerConfiguration config = new BearerConfiguration( MPeriod.HOUR_IN_MILLISECOUNDS );
    
    @Override
    protected void doCreate(JsonResult result, CallContext callContext) throws Exception {
        Subject subject = AccessUtil.getSubject();
        if (subject == null || !subject.isAuthenticated()) {
            result.createObjectNode().put("rc", -1);
            return;
        }
        String token = AccessUtil.createBearerToken(subject, null, config);
        if (token == null) {
            result.createObjectNode().put("rc", -2);
            return;
        }
        result.createObjectNode().put("token", token).put("rc", 0);
    }

}
