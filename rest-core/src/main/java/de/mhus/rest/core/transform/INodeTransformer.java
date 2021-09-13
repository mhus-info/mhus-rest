package de.mhus.rest.core.transform;

import com.fasterxml.jackson.databind.JsonNode;

import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.JsonNodeBuilder;

public class INodeTransformer implements ObjectTransformer {

    @Override
    public JsonNode toJsonNode(Object obj) {
        if (obj instanceof INode) {
            JsonNodeBuilder builder = new JsonNodeBuilder();
            JsonNode out = builder.writeToJsonNode((INode) obj);
            return out;
        }
        return null;
    }

}
