package de.mhus.rest.core.node;

import java.util.List;

import de.mhus.lib.errors.NotSupportedException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestResult;
import de.mhus.rest.core.result.JsonResult;

public class JsonRestNode extends AbstractNode {

    @Override
    public Node lookup(List<String> parts, CallContext callContext) throws Exception {

        if (parts.size() < 1) return this;
        return callContext.lookup(parts, getClass());
    }

    @Override
    public RestResult doRead(CallContext context) throws Exception {
        JsonResult result = new JsonResult();
        doRead(result, context);
        return result;
    }

    @Override
    public RestResult doCreate(CallContext context) throws Exception {
        JsonResult result = new JsonResult();
        doCreate(result, context);
        return result;
    }
    
    @Override
    public RestResult doDelete(CallContext context) throws Exception {
        JsonResult result = new JsonResult();
        doDelete(result, context);
        return result;
    }
    
    @Override
    public RestResult doUpdate(CallContext context) throws Exception {
        JsonResult result = new JsonResult();
        doUpdate(result, context);
        return result;
    }
    
    @Override
    public RestResult doAction(CallContext context) throws Exception {
        JsonResult result = new JsonResult();
        doAction(result, context);
        return result;
    }

    protected void doRead(JsonResult result, CallContext context) {
        throw new NotSupportedException();
    }

    protected void doCreate(JsonResult result, CallContext context) {
        throw new NotSupportedException();
    }

    protected void doUpdate(JsonResult result, CallContext context) {
        throw new NotSupportedException();
    }

    protected void doDelete(JsonResult result, CallContext context) {
        throw new NotSupportedException();
    }

    protected void doAction(JsonResult result, CallContext context) {
        throw new NotSupportedException();
    }

}
