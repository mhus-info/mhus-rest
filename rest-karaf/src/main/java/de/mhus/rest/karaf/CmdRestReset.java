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
package de.mhus.rest.karaf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.AbstractNode;

@Command(scope = "mhus", name = "rest-reset", description = "Reset all rest providers")
@Service
public class CmdRestReset extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {

        RestApi restService = M.l(RestApi.class);

        restService.reset();

        System.out.println("OK");
        return null;
    }
}
