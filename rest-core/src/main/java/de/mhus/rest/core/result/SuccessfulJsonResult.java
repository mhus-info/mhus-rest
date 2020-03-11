package de.mhus.rest.core.result;

import org.codehaus.jackson.node.ObjectNode;


public class SuccessfulJsonResult extends JsonResult {

    private ObjectNode data;

    public SuccessfulJsonResult(String msg) {
        data = createObjectNode();
        data.put("successful", true);
        data.put("rc", 0);
        if (msg != null) data.put("msg", msg);
    }

    public ObjectNode getData() {
        return data;
    }
}
