package de.mhus.rest.core;

import java.io.InputStream;
import java.util.Set;

public interface RestRequest {

    String getHeader(String name);

    String getParameter(String name);

    Set<String> getParameterNames();

    /**
     * Return the load content of the request.
     * @return The load as input stream
     */
    InputStream getLoadContent();

}
