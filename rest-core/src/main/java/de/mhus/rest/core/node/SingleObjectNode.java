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

import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.result.JsonResult;
import de.mhus.rest.core.transform.ObjectTransformer;

public abstract class SingleObjectNode<T> extends JsonSingleNode<T> {

    protected ObjectTransformer transformer = null;

    public SingleObjectNode() {
        loadTransformer();
    }

    protected void loadTransformer() {
        transformer = ObjectTransformer.create(this.getClass());
    }

    @Override
    public void doRead(JsonResult result, CallContext callContext) throws Exception {

        T obj = getObjectFromContext(callContext, getManagedClassName());
        if (obj == null) throw new NotFoundException();

        doPrepareForOutput(obj, callContext, false);

        JsonNode out = transformToJsonNode(obj);
        if (out == null) throw new NotFoundException();
        result.setJson(out);
    }

    protected JsonNode transformToJsonNode(T obj) {
        return transformer.toJsonNode(obj);
    }

    protected void doPrepareForOutput(T obj, CallContext context, boolean listMode)
            throws MException {}

    @Override
    protected void doUpdate(JsonResult result, CallContext callContext) throws Exception {
        T obj = getObjectFromContext(callContext);
        if (obj == null) throw new NotFoundException();

        doUpdate(obj, callContext);
        doPrepareForOutput(obj, callContext, false);
        JsonNode jItem = transformer.toJsonNode(obj);
        result.setJson(jItem);
    }

    protected void doUpdate(T obj, CallContext context) throws MException {}
}
