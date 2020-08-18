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

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

public class RestAuthenticatorByTicket implements RestAuthenticator {

    @Override
    public AuthenticationToken authenticate(RestRequest req) {
        String ticket = req.getParameter("_ticket");
        if (ticket == null) return null;

        String[] parts = ticket.split(":", 2);
        if (parts.length != 2) return null;

        UsernamePasswordToken token = new UsernamePasswordToken(parts[0], parts[1]);
        return token;
    }
}
