package de.mhus.rest.core.transform;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class TransformList implements ObjectTransformer {
    
    protected List<ObjectTransformer> list = new ArrayList<>();
    
    @Override
    public JsonNode toJsonNode(Object obj) {
        for (ObjectTransformer entry : list) {
            JsonNode node = entry.toJsonNode(obj);
            if (node != null)
                return node;
        }
        return null;
    }

    public void add(ObjectTransformer t) {
        list.add(t);
    }

}
