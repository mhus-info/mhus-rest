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
