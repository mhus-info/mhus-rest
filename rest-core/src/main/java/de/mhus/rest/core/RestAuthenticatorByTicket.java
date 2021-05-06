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

import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;

public class RestAuthenticatorByTicket implements RestAuthenticator {

    @Override
    public AuthenticationToken authenticate(RestRequest req) {
        String ticket = req.getParameter("_ticket");
        if (MString.isEmptyTrim(ticket)) return null;

        AuthenticationToken token = Aaa.createToken(ticket);
        return token;
    }
}
