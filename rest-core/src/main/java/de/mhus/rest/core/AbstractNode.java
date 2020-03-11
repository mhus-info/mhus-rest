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
package de.mhus.rest.core;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.errors.UsageException;
import de.mhus.rest.core.annotation.RestAction;
import de.mhus.rest.core.annotation.RestNode;

public abstract class AbstractNode extends MLog implements RestNodeService {

    public static final String ID = "_id";
    public static final String OBJECT = "_obj";

    private RestNode nodeDef;
    private HashMap<String, Method> actions = null;

    @Override
    public Node lookup(List<String> parts, CallContext callContext) throws Exception {
        return this;
    }

    @Override
    public RestResult doRead(CallContext context) throws Exception {
        Object ret = doReadObject(context);
        return doTransform(new Object() {}.getClass().getEnclosingMethod(), ret);
    }

    public Object doReadObject(CallContext context) throws Exception {
        return null;
    }

    @Override
    public RestResult doCreate(CallContext context) throws Exception {
        Object ret = doCreateObject(context);
        return doTransform(new Object() {}.getClass().getEnclosingMethod(), ret);
    }

    public Object doCreateObject(CallContext context) throws Exception {
        return null;
    }

    @Override
    public RestResult doDelete(CallContext context) throws Exception {
        Object ret = doDeleteObject(context);
        return doTransform(new Object() {}.getClass().getEnclosingMethod(), ret);
    }

    public Object doDeleteObject(CallContext context) throws Exception {
        return null;
    }

    @Override
    public RestResult doUpdate(CallContext context) throws Exception {
        Object ret = doUpdateObject(context);
        return doTransform(new Object() {}.getClass().getEnclosingMethod(), ret);
    }

    public Object doUpdateObject(CallContext context) throws Exception {
        return null;
    }

    public AbstractNode() {
        nodeDef = getClass().getAnnotation(RestNode.class);
        for (Method method : MSystem.getMethods(getClass())) {
            RestAction action = method.getAnnotation(RestAction.class);
            if (action == null) continue;
            if (actions == null) actions = new HashMap<>();
            actions.put(action.name(), method);
        }
    }

    @Override
    public RestResult doAction(CallContext callContext) throws Exception {
        if (actions != null) {
            String actionName = callContext.getAction();
            try {
                Method action = actions.get(actionName);
                if (action != null) {
                    if (action.getParameterCount() == 2) {
                        JsonResult result = new JsonResult();
                        action.invoke(this, result, callContext);
                        return result;
                    } else if (action.getParameterCount() == 1) {
                        Object res = action.invoke(this, callContext);
                        return doTransform(action, res);
                    } else if (action.getParameterCount() == 0) {
                        Object res = action.invoke(this);
                        return doTransform(action, res);
                    } else log().w("action wrong number of parameters", actionName, action);
                } else {
                    log().w("action unknown", actionName);
                }
            } catch (Throwable t) {
                log().d(actionName, callContext, t);
            }
        } else {
            String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
            try {
                JsonResult result = new JsonResult();
                Method method =
                        getClass().getMethod(methodName, JsonResult.class, CallContext.class);
                method.invoke(this, result, callContext);
                return result;
            } catch (java.lang.NoSuchMethodException e) {
                log().d("action method not found", methodName);
            } catch (Throwable t) {
                log().d(methodName, callContext, t);
            }
        }
        return null;
    }

    public RestResult doTransform(Method action, Object res) {
        if (res == null) return null;
        if (res instanceof RestResult) return (RestResult) res;

        RestAction actionAnno = action.getAnnotation(RestAction.class);
        String type = actionAnno == null ? "text/plain" : actionAnno.contentType();

        if (res instanceof InputStream) return new BinaryResult((InputStream) res, type);
        if (res instanceof Reader) return new BinaryResult((Reader) res, type);
        if (res instanceof String) return new PlainTextResult((String) res, type);
        return new PojoResult(res, type);
    }

    // root by default
    @Override
    public String[] getParentNodeCanonicalClassNames() {
        if (nodeDef == null) throw new UsageException("parent node not defined");
        if (nodeDef.parentNode().length != 0) {
            String[] out = new String[nodeDef.parentNode().length];
            for (int i = 0; i < out.length; i++) {
                String parentName = nodeDef.parentNode()[i].getCanonicalName();
                if (parentName != null) // could be a inner class
                out[i] = parentName;
            }
            return out;
        }
        return nodeDef.parent();
    }

    @Override
    public String getNodeId() {
        if (nodeDef == null) throw new UsageException("parent node not defined");
        return nodeDef.name();
    }

    @Override
    public String getDefaultAcl() {
        if (nodeDef == null) return null;
        return nodeDef.acl();
    }

    /**
     * Return a the managed class as class
     *
     * @return x
     */
    public String getManagedClassName() {
        String ret = MSystem.getTemplateCanonicalName(getClass(), 0);
        return ret == null ? Void.class.getCanonicalName() : ret;
    }

    public static <T> String getIdFromContext(CallContext callContext, Class<T> clazz) {
        return (String) callContext.get(clazz.getCanonicalName() + ID);
    }

    public static <T> String getIdFromContext(CallContext callContext, String clazz) {
        return (String) callContext.get(clazz + ID);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObjectFromContext(CallContext callContext, Class<T> clazz) {
        return (T) callContext.get(clazz.getCanonicalName() + OBJECT);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObjectFromContext(CallContext callContext, String clazz) {
        return (T) callContext.get(clazz + OBJECT);
    }
}
