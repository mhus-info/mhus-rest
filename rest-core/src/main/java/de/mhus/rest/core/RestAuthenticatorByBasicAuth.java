package de.mhus.rest.core;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import de.mhus.lib.core.util.Base64;
import de.mhus.lib.core.util.MUri;

public class RestAuthenticatorByBasicAuth implements RestAuthenticator {

    @Override
    public AuthenticationToken authenticate(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.toUpperCase().startsWith("BASIC ")) {
            return null; // we only do BASIC
        }
        // Get encoded user and password, comes after "BASIC "
        String userpassEncoded = auth.substring(6);
        // Decode it, using any base 64 decoder
        String userpassDecoded = new String(Base64.decode(userpassEncoded));
        // Check our user list to see if that user and password are "allowed"
        String[] parts = userpassDecoded.split(":", 2);

        String account = null;
        String pass = null;
        if (parts.length > 0) account = MUri.decode(parts[0]);
        if (parts.length > 1) pass = MUri.decode(parts[1]);

        UsernamePasswordToken token = new UsernamePasswordToken(account, pass);
        return token;
    }
}
