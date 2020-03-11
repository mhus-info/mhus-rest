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

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.M;
import de.mhus.lib.core.security.AaaContext;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.RestNodeService;
import de.mhus.rest.core.SingleObjectNode;

@Component(immediate = true, service = RestNodeService.class)
public class UserInformationRestNode extends SingleObjectNode<UserInformation> {

    @Override
    public String[] getParentNodeCanonicalClassNames() {
        return new String[] {PUBLIC_PARENT};
    }

    @Override
    public String getNodeId() {
        return "uid";
    }

    //	@Override
    //	public Class<UserInformation> getManagedClass() {
    //		return UserInformation.class;
    //	}

    @Override
    protected UserInformation getObject(CallContext context) throws Exception {
        AccessApi aaa = M.l(AccessApi.class);
        if (aaa == null) throw new NotFoundException("AccessApi not configured");

        AaaContext acc = aaa.getCurrentOrGuest();

        return new UserInformation(acc);
    }
}
