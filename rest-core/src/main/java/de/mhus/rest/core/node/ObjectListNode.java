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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.result.JsonResult;
import de.mhus.rest.core.transform.ObjectTransformer;

public abstract class ObjectListNode<T, L> extends JsonListNode<T> {

    protected ObjectTransformer transformer;

    public ObjectListNode() {
        loadTransformer();
    }

    protected void loadTransformer() {
        transformer = ObjectTransformer.create(this.getClass());
    }

    @Override
    public void doRead(JsonResult result, CallContext callContext) throws Exception {

        T obj = getObjectFromContext(callContext, getManagedClassName());
        if (obj != null) {
            doPrepareForOutput(obj, callContext);
            JsonNode jItem = transformer.toJsonNode(obj);
            if (jItem == null) throw new NotFoundException();
            result.setJson(jItem);
        } else {
            ArrayNode jList = result.createArrayNode();

            for (L item : getObjectList(callContext)) {
                doPrepareForOutputList(item, callContext);
                JsonNode jItem = transformer.toJsonNode(item);
                if (jItem != null)
                    jList.add(jItem);
            }
        }
    }

    protected abstract List<L> getObjectList(CallContext callContext) throws MException;

    protected void doPrepareForOutputList(L obj, CallContext context) throws MException {}

    protected void doPrepareForOutput(T obj, CallContext context) throws MException {}

    // Not by default
    //	@Override
    //	protected void doUpdate(JsonResult result, CallContext callContext)
    //			throws Exception {
    //		T obj = getObjectFromContext(callContext);
    //		if (obj == null) throw new RestException(OperationResult.NOT_FOUND);
    //
    //		RestUtil.updateObject(callContext, obj, true);
    //	}

    @Override
    protected void doUpdate(JsonResult result, CallContext callContext) throws Exception {

        T obj = getObjectFromContext(callContext, getManagedClassName());
        if (obj == null) throw new NotFoundException();

        doUpdateObj(obj, callContext);

        doPrepareForOutput(obj, callContext);
        JsonNode jItem = transformer.toJsonNode(obj);
        result.setJson(jItem);
    }

    @Override
    protected void doCreate(JsonResult result, CallContext callContext) throws Exception {

        T obj = doCreateObj(callContext);

        doPrepareForOutput(obj, callContext);
        JsonNode jItem = transformer.toJsonNode(obj);
        result.setJson(jItem);
    }

    @Override
    protected void doDelete(JsonResult result, CallContext callContext) throws Exception {
        T obj = getObjectFromContext(callContext, getManagedClassName());
        if (obj == null) throw new NotFoundException();

        doDeleteObj(obj, callContext);

        doPrepareForOutput(obj, callContext);
        JsonNode jItem = transformer.toJsonNode(obj);
        result.setJson(jItem);
    }

    protected T doCreateObj(CallContext callContext) throws Exception {
        throw new NotSupportedException();
    }

    protected void doUpdateObj(T obj, CallContext callContext) throws Exception {
        throw new NotSupportedException();
    }

    protected void doDeleteObj(T obj, CallContext callContext) throws Exception {
        throw new NotSupportedException();
    }
}
