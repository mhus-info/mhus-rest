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

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.rest.core.api.RestApi;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.AbstractNode;

@Command(scope = "mhus", name = "rest", description = "REST Call")
@Service
public class RestCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description = "Command:\n" + " list\n" + "",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "parameters",
            required = false,
            description = "More Parameters",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        RestApi restService = M.l(RestApi.class);

        if (cmd.equals("list")) {
            HashMap<RestNodeService, LinkedList<String>> list =
                    new HashMap<RestNodeService, LinkedList<String>>();
            for (Entry<String, RestNodeService> entry :
                    restService.getRestNodeRegistry().entrySet()) {
                LinkedList<String> item = list.get(entry.getValue());
                if (item == null) {
                    item = new LinkedList<String>();
                    list.put(entry.getValue(), item);
                }
                item.add(entry.getKey());
            }

            ConsoleTable table = new ConsoleTable(tblOpt);
            table.setHeaderValues("Class", "Node Id", "Managed", "Parents", "Registrations");
            for (Entry<RestNodeService, LinkedList<String>> entry : list.entrySet()) {
                String managed = "";
                if (entry.getKey() instanceof AbstractNode)
                    managed = ((AbstractNode) entry.getKey()).getManagedClassName();

                table.addRowValues(
                        entry.getKey().getClass().getCanonicalName(),
                        entry.getKey().getNodeName(),
                        managed,
                        entry.getKey().getParentNodeCanonicalClassNames(),
                        entry.getValue());
            }
            table.print();
        }
        return null;
    }
}
