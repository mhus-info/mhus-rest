package de.mhus.rest.core.transform;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.rest.core.util.RestUtil;

public class PojoTransformer extends MLog implements ObjectTransformer {

    @Override
    public JsonNode toJsonNode(Object obj) {

        PojoModelFactory schema = getPojoModelFactory();
        ObjectNode jRoot = MJson.createObjectNode();
        try {
            MPojo.pojoToJson(obj, jRoot, schema, true);
        } catch (IOException e) {
            log().e(obj,e);
        }
        return jRoot;
    }

    protected PojoModelFactory getPojoModelFactory() {
        return RestUtil.getPojoModelFactory();
    }

}
