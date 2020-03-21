package de.mhus.rest.core;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;

public interface RestAuthenticator {

    AuthenticationToken authenticate(HttpServletRequest req);
    
}
