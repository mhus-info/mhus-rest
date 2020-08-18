package de.mhus.rest.core;

import java.util.Set;

public interface RestRequest {

    String getHeader(String name);

    String getParameter(String name);

    Set<String> getParameterNames();

}
