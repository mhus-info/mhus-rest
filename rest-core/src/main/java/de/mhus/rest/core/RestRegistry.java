package de.mhus.rest.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestNodeService;

public class RestRegistry extends MLog {

    public static final CfgBoolean RELAXED = new CfgBoolean(RestApi.class, "aaaRelaxed", true);
    
    private Map<String, RestNodeService> register = Collections.synchronizedMap(new HashMap<>());
    
    public Map<String, RestNodeService> getRegistry() {
        return register;
    }

    public Node lookup(List<String> parts, CallContext context)
            throws Exception {
        return lookup(parts, null, context);
    }

    public Node lookup(List<String> parts, Class<? extends Node> lastNode, CallContext context)
            throws Exception {
        if (parts.size() < 1) return null;
        String name = parts.get(0);
        parts.remove(0);
        String lastNodeId =
                lastNode == null ? RestNodeService.ROOT_PARENT : lastNode.getCanonicalName();
        RestNodeService next = register.get(lastNodeId + "-" + name);
        if (next == null) return null;

        Subject subject = SecurityUtils.getSubject();
        try {
            subject.checkPermission(new WildcardPermission("rest.node:execute:" + name) );
            log().d(
                    "access granted",
                    subject,
                    "rest.node",
                    name,
                    "execute");
        } catch (AuthorizationException e) {
            log().d(
                    "access denied",
                    subject,
                    "rest.node",
                    name,
                    "execute");
            throw new AccessDeniedException("access denied");
        }
        return next.lookup(parts, context);
    }

}
