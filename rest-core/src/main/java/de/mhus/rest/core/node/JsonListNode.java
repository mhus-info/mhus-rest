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

import de.mhus.lib.errors.NotSupportedException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.api.RestResult;
import de.mhus.rest.core.result.JsonResult;

public abstract class JsonListNode<T> extends ListNode<T> {

    @Override
    public RestResult doRead(CallContext callContext) throws Exception {
        JsonResult result = new JsonResult();
        doRead(result, callContext);
        return result;
    }

    public abstract void doRead(JsonResult result, CallContext callContext) throws Exception;

    @Override
    public RestResult doCreate(CallContext callContext) throws Exception {
        JsonResult result = new JsonResult();
        doCreate(result, callContext);
        return result;
    }

    @Override
    public RestResult doUpdate(CallContext callContext) throws Exception {
        JsonResult result = new JsonResult();
        doUpdate(result, callContext);
        return result;
    }

    @Override
    public RestResult doDelete(CallContext callContext) throws Exception {
        JsonResult result = new JsonResult();
        doDelete(result, callContext);
        return result;
    }

    protected void doUpdate(JsonResult result, CallContext callContext) throws Exception {
        throw new NotSupportedException();
    }

    protected void doCreate(JsonResult result, CallContext callContext) throws Exception {
        throw new NotSupportedException();
    }

    protected void doDelete(JsonResult result, CallContext callContext) throws Exception {
        throw new NotSupportedException();
    }

    /*
    @Override
    public RestResult doAction(CallContext callContext) throws Exception {
    	String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
    	JsonResult result = new JsonResult();
    	Method method = getClass().getMethod(methodName, JsonResult.class, CallContext.class);
    	method.invoke(this, result, callContext);
    	return result;

    }
    */

}
