package de.mhus.rest.core;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

public class RestAuthenticatorByTicket implements RestAuthenticator {

    @Override
    public AuthenticationToken authenticate(HttpServletRequest req) {
        String ticket = req.getParameter("_ticket");
        if (ticket == null) return null;

        String[] parts = ticket.split(":", 2);
        if (parts.length != 2) return null;

        UsernamePasswordToken token = new UsernamePasswordToken(parts[0], parts[1]);
        return token;
    }
}
