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
package de.mhus.rest.core;

import java.util.List;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.errors.MException;

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

}
