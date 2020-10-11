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
package de.mhus.rest.osgi;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.SingleObjectNode;

@ServiceComponent(service = RestNodeService.class)
@RestNode(parentNode = PublicRestNode.class, name="uid")
public class UserInformationRestNode extends SingleObjectNode<UserInformation> {

    //	@Override
    //	public Class<UserInformation> getManagedClass() {
    //		return UserInformation.class;
    //	}

    @Override
    protected UserInformation getObject(CallContext context) throws Exception {

        Subject subject = SecurityUtils.getSubject();

        return new UserInformation(subject);
    }
}
