/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.rest.core.node;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.result.JsonResult;
import de.mhus.rest.core.util.RestUtil;

public abstract class ObjectListNode<T, L> extends JsonListNode<T> {

    @Override
    public void doRead(JsonResult result, CallContext callContext) throws Exception {

        PojoModelFactory schema = getPojoModelFactory();

        T obj = getObjectFromContext(callContext, getManagedClassName());
        if (obj != null) {
            doPrepareForOutput(obj, callContext);
            ObjectNode jRoot = result.createObjectNode();
            MPojo.pojoToJson(obj, jRoot, schema, true);
        } else {
            ArrayNode jList = result.createArrayNode();

            for (L item : getObjectList(callContext)) {
                doPrepareForOutputList(item, callContext);
                ObjectNode jItem = jList.objectNode();
                jList.add(jItem);
                MPojo.pojoToJson(item, jItem, schema, true);
            }
        }
    }

    protected PojoModelFactory getPojoModelFactory() {
        return RestUtil.getPojoModelFactory();
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

        PojoModelFactory schema = getPojoModelFactory();
        doPrepareForOutput(obj, callContext);
        ObjectNode jRoot = result.createObjectNode();
        MPojo.pojoToJson(obj, jRoot, schema, true);
    }

    @Override
    protected void doCreate(JsonResult result, CallContext callContext) throws Exception {

        T obj = doCreateObj(callContext);

        PojoModelFactory schema = getPojoModelFactory();
        doPrepareForOutput(obj, callContext);
        ObjectNode jRoot = result.createObjectNode();
        MPojo.pojoToJson(obj, jRoot, schema, true);
    }

    @Override
    protected void doDelete(JsonResult result, CallContext callContext) throws Exception {
        T obj = getObjectFromContext(callContext, getManagedClassName());
        if (obj == null) throw new NotFoundException();

        doDeleteObj(obj, callContext);

        PojoModelFactory schema = getPojoModelFactory();
        doPrepareForOutput(obj, callContext);
        ObjectNode jRoot = result.createObjectNode();
        MPojo.pojoToJson(obj, jRoot, schema, true);
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
