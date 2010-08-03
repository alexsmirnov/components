/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ajax4jsf.component.behavior;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.behavior.ClientBehaviorHint;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorListener;
import javax.faces.event.BehaviorEvent;

import org.ajax4jsf.component.AjaxClientBehavior;
import org.ajax4jsf.renderkit.AjaxRendererUtils;
import org.richfaces.cdk.annotations.JsfBehavior;
import org.richfaces.cdk.annotations.Tag;
import org.richfaces.cdk.annotations.TagType;

/**
 * @author Anton Belevich
 * 
 */

@JsfBehavior(id = "org.ajax4jsf.behavior.Ajax", tag = @Tag(name = "ajax", handler = "org.richfaces.view.facelets.html.AjaxHandler", type = TagType.Facelets))
public class AjaxBehavior extends ClientBehavior implements AjaxClientBehavior {

    public static final String BEHAVIOR_ID = "org.ajax4jsf.behavior.Ajax";

    private static final Set<ClientBehaviorHint> HINTS = Collections.unmodifiableSet(EnumSet
        .of(ClientBehaviorHint.SUBMITTING));
    
    private static final Set<String> ALL_SINGLETON_SET = Collections.singleton(AjaxRendererUtils.ALL);
    private static final Set<String> FORM_SINGLETON_SET = Collections.singleton(AjaxRendererUtils.FORM);
    private static final Set<String> THIS_SINGLETON_SET = Collections.singleton(AjaxRendererUtils.THIS);
    private static final Set<String> REGION_SINGLETON_SET = Collections.singleton(AjaxRendererUtils.REGION);
    private static final Set<String> NONE_SINGLETON_SET = Collections.singleton(AjaxRendererUtils.NONE);
    
    enum PropertyKeys {
        data, execute, onbeforedomupdate, onbegin, oncomplete, onerror, onevent, queueId, render,
        status, disabled, limitRender, immediate, bypassUpdates
    }
    
    private Set<String> execute;
         
    private Set<String> render;
    
    @Override
    public void setLiteralAttribute(String name, Object value) {
        if (compare(PropertyKeys.data, name)) {
            setData(value);
        } else if (compare(PropertyKeys.execute, name)) {
            setExecute(toSet(PropertyKeys.execute, value));
        } else if (compare(PropertyKeys.render, name)) {
            setRender(toSet(PropertyKeys.render, value));
        } else if (compare(PropertyKeys.onbeforedomupdate, name)) {
            setOnbeforedomupdate((String) value);
        } else if (compare(PropertyKeys.onbegin, name)) {
            setOnbegin((String) value);
        } else if (compare(PropertyKeys.oncomplete, name)) {
            setOncomplete((String) value);
        } else if (compare(PropertyKeys.onerror, name)) {
            setOnerror((String) value);
        } else if (compare(PropertyKeys.onevent, name)) {
            setOnevent((String) value);
        } else if (compare(PropertyKeys.queueId, name)) {
            setQueueId((String) value);
        } else if (compare(PropertyKeys.status, name)) {
            setStatus((String) value);
        } else if (compare(PropertyKeys.disabled, name)) {
            setDisabled((Boolean) value);
        } else if (compare(PropertyKeys.limitRender, name)) {
            setLimitRender((Boolean) value);
        } else if (compare(PropertyKeys.immediate, name)) {
            setImmediate((Boolean) value);
        } else if (compare(PropertyKeys.bypassUpdates, name)) {
            setBypassUpdates((Boolean) value);
        }
    }

    private Set<String> toSet(Serializable propertyName, Object value) {

        Set<String> result = null;

        if (value instanceof String) {
            result = toSingletonSet(propertyName.toString(), (String) value);
        }

        if (result == null) {
            result = AjaxRendererUtils.asSet(value);
        }

        if (result == null) {
            throw new FacesException(
                propertyName.toString()
                    + "' attribute value must be Collection, List, array, String, comma-separated String, whitespace-separate String'");
        }

        return result;
    }
   
    private Set<String> toSingletonSet(String propertyName, String value) {
        
        if(value == null) {
            return null;
        }
        
        if(value.trim().length() == 0) {
            return null;
        }
        
        if(value.charAt(0) == '@') {
            if (AjaxRendererUtils.ALL.equals(value)) {
                return ALL_SINGLETON_SET;
            } else if (AjaxRendererUtils.FORM.equals(value)) {
                return FORM_SINGLETON_SET;
            } else if (AjaxRendererUtils.THIS.equals(value)) {
                return THIS_SINGLETON_SET;
            } else if (AjaxRendererUtils.REGION.equals(value)) {
                return REGION_SINGLETON_SET;
            } else if (AjaxRendererUtils.NONE.equals(value)) {
                return NONE_SINGLETON_SET;
            } else {
                throw new FacesException(value + " : Invalid id keyword specified for '"
                    + propertyName + "' attribute");
            }
        }
        
        return null;
    }

    public Object getData() {
        return getStateHelper().eval(PropertyKeys.data);
    }

    public void setData(Object data) {
        getStateHelper().put(PropertyKeys.data, data);
    }

    public Collection<String> getExecute() {
        return getCollectionValue(PropertyKeys.execute, execute);
    }

    public void setExecute(Collection<String> execute) {
        this.execute = copyToSet(execute);
        clearInitialState();
    }
    
    public String getOnbeforedomupdate() {
        return (String) getStateHelper().eval(PropertyKeys.onbeforedomupdate);
    }

    public void setOnbeforedomupdate(String onbeforedomupdate) {
        getStateHelper().put(PropertyKeys.onbeforedomupdate, onbeforedomupdate);
    }

    public String getOnbegin() {
        return (String) getStateHelper().eval(PropertyKeys.onbegin);
    }

    public void setOnbegin(String onbegin) {
        getStateHelper().put(PropertyKeys.onbegin, onbegin);
    }

    public String getOncomplete() {
        return (String) getStateHelper().eval(PropertyKeys.oncomplete);
    }

    public void setOncomplete(String oncomplete) {
        getStateHelper().put(PropertyKeys.oncomplete, oncomplete);
    }

    public String getOnerror() {
        return (String) getStateHelper().eval(PropertyKeys.onerror);
    }

    public void setOnerror(String onerror) {
        getStateHelper().put(PropertyKeys.onerror, onerror);
    }

    public String getOnevent() {
        return (String) getStateHelper().eval(PropertyKeys.onevent);
    }

    public void setOnevent(String onevent) {
        getStateHelper().put(PropertyKeys.onevent, onevent);
    }

    public String getQueueId() {
        return (String) getStateHelper().eval(PropertyKeys.queueId);
    }

    public void setQueueId(String queueId) {
        getStateHelper().put(PropertyKeys.queueId, queueId);
    }

    public Collection<String> getRender() {
        return getCollectionValue(PropertyKeys.render, render);
    }

    public void setRender(Collection<String> render) {
        this.render = copyToSet(render);
        clearInitialState();
    }
    
    public String getStatus() {
        return (String) getStateHelper().eval(PropertyKeys.status);
    }

    public void setStatus(String status) {
        getStateHelper().put(PropertyKeys.status, status);
    }

    public boolean isDisabled() {
        return (Boolean) getStateHelper().eval(PropertyKeys.disabled, false);
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put(PropertyKeys.disabled, disabled);
    }

    public boolean isLimitRender() {
        return (Boolean) getStateHelper().eval(PropertyKeys.limitRender, false);
    }

    public void setLimitRender(boolean limitRender) {
        getStateHelper().put(PropertyKeys.limitRender, limitRender);
    }

    public boolean isImmediate() {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, false);
    }

    public void setImmediate(boolean immediate) {
        getStateHelper().put(PropertyKeys.limitRender, immediate);
    }

    public boolean isBypassUpdates() {
        return (Boolean) getStateHelper().eval(PropertyKeys.bypassUpdates, false);
    }

    public void setBypassUpdates(boolean bypassUpdates) {
        getStateHelper().put(PropertyKeys.bypassUpdates, bypassUpdates);
    }
    
    @Override
    public String getRendererType() {
        return BEHAVIOR_ID;
    }

    @Override
    public Set<ClientBehaviorHint> getHints() {
        return HINTS;
    }

    public void addAjaxBehaviorListener(AjaxBehaviorListener listener) {
        addBehaviorListener(listener);
    }

    public void removeAjaxBehaviorListener(AjaxBehaviorListener listener) {
        removeBehaviorListener(listener);
    }

    @Override
    public void broadcast(BehaviorEvent event) throws AbortProcessingException {
        if (this.equals(event.getBehavior()) && isBypassUpdates()) {
            FacesContext.getCurrentInstance().renderResponse();
        }
        
        super.broadcast(event);
    }
    
    private  Object saveSet(Serializable propertyName, Set<String> set) {
        if ((set == null) || set.isEmpty()) {
            return null;
        }

        int size = set.size();

        if (size == 1) {
            return set.toArray(new String[1])[0];
        }

        return set.toArray(new String[size]);
    }

    private Set<String> restoreSet(Serializable propertyName, Object state) {
        if (state == null) {
            return null;
        }
        
        Set<String> set = toSet(propertyName, state);
        return set;
    }
    
    private Set<String> copyToSet(Collection<String> collection) {
        return Collections.unmodifiableSet(new HashSet<String>(collection));
    }
    
    private Collection<String> getCollectionValue(Serializable propertyName, Collection<String> collection) {
        if (collection!= null) {
            return collection;
        }

        Collection<String> result = null;

        ValueExpression expression = getValueExpression(propertyName.toString());
        if (expression != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Object value = expression.getValue(facesContext.getELContext());

            if (value != null) {

                if (value instanceof Collection) {
                    return (Collection<String>)value;
                }

                result = toSet(propertyName, value);
            }
        }
        return result == null ? Collections.<String>emptyList() : result;
    }
    
    @Override
    public void restoreState(FacesContext context, Object state) {
        if (state != null) {
            Object[] values = (Object[]) state;
            super.restoreState(context, values[0]);

            if (values.length != 1) {
                execute = restoreSet(PropertyKeys.execute, values[1]);
                render = restoreSet(PropertyKeys.render, values[2]);

                clearInitialState();
            }
        }
    }
    
    @Override
    public Object saveState(FacesContext context) {
        Object parentState = super.saveState(context);
        Object state = null;
        
        if (initialStateMarked()) {
            
            if (parentState != null) {
                state = new Object[] { parentState };
            }
            
        } else {
            Object [] values = new Object[3];
            values[0] = parentState;
            values[1] = saveSet(PropertyKeys.execute, execute);
            values[2] = saveSet(PropertyKeys.render, render);
            
            state = values;
        }    

        return state;
    }
}
