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
package de.mhus.rest.core.api;

import java.util.List;

import de.mhus.rest.core.CallContext;

public interface Node {

    String ACTION = "_action";

    String FOUNDATION_NODE_NAME = "foundation";
    String PUBLIC_NODE_NAME = "public";
    String GENERAL_NODE_NAME = "general";

    String ROOT_PARENT = "";
    String FOUNDATION_PARENT = "de.mhus.osgi.sop.foundation.rest.FoundationNode";
    String PUBLIC_PARENT = "de.mhus.osgi.sop.rest.PublicRestNode";
    // String GENERAL_PARENT = "general";

    Node lookup(List<String> parts, CallContext callContext) throws Exception;

    RestResult doRead(CallContext callContext) throws Exception;

    RestResult doAction(CallContext callContext) throws Exception;

    RestResult doCreate(CallContext callContext) throws Exception;

    RestResult doUpdate(CallContext callContext) throws Exception;

    RestResult doDelete(CallContext callContext) throws Exception;
}
