package de.mhus.rest.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestNodeService;

public class RestRegistry extends MLog {

    public static final CfgBoolean RELAXED = new CfgBoolean(RestApi.class, "aaaRelaxed", true);
    
    private Map<String, RestNodeService> register = Collections.synchronizedMap(new HashMap<>());
    private AccessApi accessApi;
    
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

        AccessApi aaa = getAccesssApi();
        if (aaa != null) {
            try {
                String def = next.getDefaultAcl();
                if (!aaa.hasResourceAccess(
                        aaa.getCurrentAccount(), "rest.node", name, "execute", def)) {
                    log().d(
                                    "access denied",
                                    aaa.getCurrentAccount(),
                                    "rest.node",
                                    name,
                                    "execute",
                                    def);
                    throw new AccessDeniedException("access denied");
                }
                log().d(
                                "access granted",
                                aaa.getCurrentAccount(),
                                "rest.node",
                                name,
                                "execute",
                                def);
            } catch (Throwable t) {
                throw new AccessDeniedException("internal error", t);
            }
        } else if (!RELAXED.value()) throw new AccessDeniedException("Access api not found");

        return next.lookup(parts, context);
    }

    public AccessApi getAccesssApi() {
        return accessApi;
    }
    
    public void setAccessApi(AccessApi accessApi) {
        this.accessApi = accessApi;
    }
    
}
