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
import org.apache.shiro.authc.BearerToken;

public class RestAuthenticatorByJwt implements RestAuthenticator {

    @Override
    public AuthenticationToken authenticate(RestRequest req) {
        String token = req.getParameter("_jwt_token");
        if (token != null) return new BearerToken(token);
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null; // we only do Bearer
        }
        token = auth.substring(7);
        return new BearerToken(token);
    }
}
