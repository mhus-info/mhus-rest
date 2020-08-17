/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
