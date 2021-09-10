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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.JsonNodeBuilder;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.api.RestException;
import de.mhus.rest.core.result.JsonResult;
import de.mhus.rest.core.util.RestUtil;

public abstract class SingleObjectNode<T> extends JsonSingleNode<T> {

    @Override
    public void doRead(JsonResult result, CallContext callContext) throws Exception {

        PojoModelFactory schema = getPojoModelFactory();

        T obj = getObjectFromContext(callContext, getManagedClassName());
        if (obj == null) throw new NotFoundException();

        doPrepareForOutput(obj, callContext, false);

        if (obj instanceof JsonNode) {
            result.setJson((JsonNode)obj);
        } else
        if (obj instanceof INode) {
            JsonNodeBuilder builder = new JsonNodeBuilder();
            JsonNode out = builder.writeToJsonNode((INode) obj);
            result.setJson(out);
        } else {
            ObjectNode jRoot = result.createObjectNode();
            MPojo.pojoToJson(obj, jRoot, schema, true);
        }
    }

    protected PojoModelFactory getPojoModelFactory() {
        return RestUtil.getPojoModelFactory();
    }

    protected void doPrepareForOutput(T obj, CallContext context, boolean listMode)
            throws MException {}

    @Override
    protected void doUpdate(JsonResult result, CallContext callContext) throws Exception {
        T obj = getObjectFromContext(callContext);
        if (obj == null) throw new RestException(OperationResult.NOT_FOUND);

        doUpdate(obj, callContext);
    }

    protected void doUpdate(T obj, CallContext context) throws MException {}
}
