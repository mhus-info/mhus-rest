package de.mhus.rest.core.api;

import de.mhus.rest.core.CallContext;

public interface RestSecurityService {

    boolean checkSecurity(CallContext callContext);

}
