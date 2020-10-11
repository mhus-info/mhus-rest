package de.mhus.rest.core;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;

public class RestAuthenticatorByJwt implements RestAuthenticator {

    @Override
    public AuthenticationToken authenticate(RestRequest req) {
        String token = req.getParameter("jwt_ticket");
        if (token != null)
            return new BearerToken(token);
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.toUpperCase().startsWith("Bearer ")) {
            return null; // we only do BASIC
        }
        token = auth.substring(7);
        return new BearerToken(token);
    }

}
