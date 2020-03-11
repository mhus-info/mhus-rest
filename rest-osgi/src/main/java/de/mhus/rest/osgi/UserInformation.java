/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.rest.osgi;

import java.util.Date;
import java.util.Locale;

import de.mhus.lib.annotations.generic.Public;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.security.AaaContext;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.Trust;
import de.mhus.lib.errors.NotSupportedException;

public class UserInformation {

    private String name;
    private String displayName;
    private IReadProperties attributes;
    private boolean syntetic;
    private boolean valid;
    private String trustName;
    private String trustTrust;
    private Locale locale;
    private boolean adminMode;
    private String[] groups;
    private Date created;
    private Date modified;

    public UserInformation(AaaContext context) {
        Account acc = context.getAccount();
        name = acc.getName();
        displayName = acc.getDisplayName();
        attributes = acc.getAttributes();
        created = acc.getCreationDate();
        modified = acc.getModifyDate();
        syntetic = acc.isSynthetic();
        valid = acc.isValid();

        Trust trust = context.getTrust();
        if (trust != null) {
            trustName = trust.getName();
            trustTrust = trust.getTrust();
        }

        locale = context.getLocale();
        adminMode = context.isAdminMode();

        try {
            groups = acc.getGroups();
        } catch (NotSupportedException e) {
        }
    }

    @Public
    public String getName() {
        return name;
    }

    @Public
    public String getDisplayName() {
        return displayName;
    }

    @Public
    public IReadProperties getAttributes() {
        return attributes;
    }

    @Public
    public boolean isSyntetic() {
        return syntetic;
    }

    @Public
    public boolean isValid() {
        return valid;
    }

    @Public
    public String getTrustName() {
        return trustName;
    }

    @Public
    public String getTrustTrust() {
        return trustTrust;
    }

    @Public
    public Locale getLocale() {
        return locale;
    }

    @Public
    public boolean isAdminMode() {
        return adminMode;
    }

    @Public
    public String[] getGroups() {
        return groups;
    }

    @Public
    public Date getCreationDate() {
        return created;
    }

    @Public
    public Date getModifyDate() {
        return modified;
    }
}
