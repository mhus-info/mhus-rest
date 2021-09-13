package de.mhus.rest.core.transform;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeTransformer implements ObjectTransformer {

    @Override
    public JsonNode toJsonNode(Object obj) {
        if (obj instanceof JsonNode) {
            return (JsonNode)obj;
        }
        return null;
    }

}
