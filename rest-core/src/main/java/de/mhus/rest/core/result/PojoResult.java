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
package de.mhus.rest.core.result;

import java.io.PrintWriter;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.rest.core.api.RestResult;

public class PojoResult implements RestResult {

    private String contentType;
    private Object obj;

    public PojoResult(Object obj, String contentType) {
        if (MString.isEmpty(contentType)) contentType = MHttp.CONTENT_TYPE_JSON;
        this.contentType = contentType;
        this.obj = obj;
    }

    @Override
    public void write(PrintWriter writer) throws Exception {
        if (obj == null) return;
        JsonResult json = new JsonResult();
        PojoModelFactory factory = MPojo.getDefaultModelFactory();
        if (obj.getClass().isArray()) {
            if (obj.getClass().getComponentType().isPrimitive()) {
                ArrayNode jArray = json.createArrayNode();
                for (Object aObj : (Object[]) obj) {
                    if (aObj == null) continue; // should not happen
                    MPojo.addJsonValue(jArray, aObj, factory, true, false, 0);
                }
            } else {
                ArrayNode jArray = json.createArrayNode();
                for (Object aObj : (Object[]) obj) {
                    ObjectNode jObj = jArray.objectNode();
                    if (aObj != null) MPojo.pojoToJson(aObj, jObj, factory);
                    jArray.add(jObj);
                }
            }
        } else if (obj.getClass().isPrimitive()) {
            ObjectNode jObj = json.createObjectNode();
            MPojo.setJsonValue(jObj, "value", obj, factory, true, false, 0);
        } else {
            ObjectNode jObj = json.createObjectNode();
            MPojo.pojoToJson(obj, jObj, factory);
        }
        json.write(writer);
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
