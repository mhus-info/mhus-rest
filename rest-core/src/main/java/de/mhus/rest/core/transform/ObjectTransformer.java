package de.mhus.rest.core.transform;

import com.fasterxml.jackson.databind.JsonNode;

import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.rest.core.annotation.RestTransformer;

public interface ObjectTransformer {

    ObjectTransformer DEFAULT = new PojoTransformer();

    static ObjectTransformer create(Class<?> owner) {
        if (owner == null) return DEFAULT;
        RestTransformer anno = owner.getAnnotation(RestTransformer.class);
        if (anno == null) return DEFAULT;
        Class<?>[] value = anno.value();
        if (value == null || value.length == 0) return new TransformList(); // empty
        if (value.length == 1) {
            try {
                return (ObjectTransformer)value[0].getConstructor().newInstance();
            } catch (Throwable t) {
                MLogUtil.log().f(owner,value[0],t);
                return new TransformList(); // empty
            }
        } else {
            TransformList out = new TransformList();
            for (Class<?> v : value) {
                try {
                    ObjectTransformer t = (ObjectTransformer)v.getConstructor().newInstance();
                    out.add(t);
                } catch (Throwable t) {
                    MLogUtil.log().f(owner,v,t);
                }
            }
            return out;
        }
    }

    /**
     * Transform an object to a jsonNode or return null if it is not supported.
     * @param obj
     * @return Node or null
     */
    JsonNode toJsonNode(Object obj);

}
