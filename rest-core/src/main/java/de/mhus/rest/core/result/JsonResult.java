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
import java.io.StringWriter;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.logging.LevelMapper;
import de.mhus.lib.core.logging.TrailLevelMapper;
import de.mhus.lib.core.security.AaaContext;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.rest.core.api.RestResult;

public class JsonResult implements RestResult {

    // private static Log log = Log.getLog(JsonResult.class);
    private static int nextId = 0;
    private org.codehaus.jackson.JsonNode json;
    private long id;
    private static ObjectMapper m = new ObjectMapper();

    public JsonResult() {
        id = newId();
    }

    @Override
    public void write(PrintWriter writer) throws Exception {

        // log.d("result",id,json);
        if (json == null) {
            createObjectNode();
        }
        if (json.isObject()) {
            ((ObjectNode) json).put("_timestamp", System.currentTimeMillis());
            ((ObjectNode) json).put("_sequence", id);

            AaaContext user = M.l(AccessApi.class).getCurrentOrGuest();
            ((ObjectNode) json).put("_user", user.getAccountId());
            if (user.isAdminMode()) ((ObjectNode) json).put("_admin", true);

            LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
            if (lm != null
                    && lm instanceof TrailLevelMapper
                    && ((TrailLevelMapper) lm).isLocalTrail())
                ((ObjectNode) json).put("_trail", ((TrailLevelMapper) lm).getTrailId());
        }

        m.writeValue(writer, json);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    private static synchronized long newId() {
        return nextId++;
    }

    public JsonNode getJson() {
        return json;
    }

    public void setJson(ObjectNode json) {
        this.json = json;
    }

    public ObjectNode createObjectNode() {
        json = m.createObjectNode();
        return (ObjectNode) json;
    }

    @Override
    public String toString() {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        try {
            write(p);
        } catch (Exception e) {
        }
        p.flush();
        return w.toString();
    }

    public ArrayNode createArrayNode() {
        json = m.createArrayNode();
        return (ArrayNode) json;
    }
}
