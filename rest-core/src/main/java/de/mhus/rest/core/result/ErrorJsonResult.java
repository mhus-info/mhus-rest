package de.mhus.rest.core.result;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ErrorJsonResult extends JsonResult {

    public ErrorJsonResult(int rc, String msg) {
        ObjectNode out = createObjectNode();
        out.put("successful", false);
        out.put("rc", rc);
        if (msg != null) out.put("msg", msg);
    }
}
