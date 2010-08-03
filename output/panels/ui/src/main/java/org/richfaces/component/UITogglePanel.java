/*
 * JBoss, Home of Professional Open Source
 * Copyright ${year}, Red Hat, Inc. and individual contributors
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

package org.richfaces.component;

import javax.el.MethodExpression;

/**
 * @author akolonitsky
 * @since -4712-01-01
 */
//TODO nick - no behaviors support!
public class UITogglePanel extends AbstractTogglePanel {

    public static final String COMPONENT_TYPE = "org.richfaces.panels.TogglePanel";

    public static final String COMPONENT_FAMILY = "org.richfaces.panels.TogglePanel";

    private enum PropertyKeys {
        switchType,
        bypassUpdates,
        limitToList,
        data,
        status,
        execute,
        render,
        selectedItemChangeListener
    }


    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public Method getSwitchType() {
        return (Method) getStateHelper().eval(PropertyKeys.switchType, Method.ajax);
    }

    public void setSwitchType(Method switchType) {
        getStateHelper().put(PropertyKeys.switchType, switchType);
    }

    public boolean isBypassUpdates() {
        return Boolean.valueOf(String.valueOf(getStateHelper().eval(PropertyKeys.bypassUpdates)));
    }

    public void setBypassUpdates(boolean bypassUpdates) {
        getStateHelper().put(PropertyKeys.bypassUpdates, bypassUpdates);
    }

    public boolean isLimitToList() {
        return Boolean.valueOf(String.valueOf(getStateHelper().eval(PropertyKeys.limitToList)));
    }

    public void setLimitToList(boolean limitToList) {
        getStateHelper().put(PropertyKeys.limitToList, limitToList);
    }

    public Object getData() {
        return getStateHelper().eval(PropertyKeys.data);
    }

    public void setData(Object data) {
        getStateHelper().put(PropertyKeys.data, data);
    }

    public String getStatus() {
        return String.valueOf(getStateHelper().eval(PropertyKeys.status));
    }

    public void setStatus(String status) {
        getStateHelper().put(PropertyKeys.status, status);
    }

    public Object getExecute() {
        return getStateHelper().eval(PropertyKeys.execute);
    }

    public void setExecute(Object execute) {
        getStateHelper().put(PropertyKeys.execute, execute);
    }

    public Object getRender() {
        return getStateHelper().eval(PropertyKeys.render);
    }

    public void setRender(Object render) {
        getStateHelper().put(PropertyKeys.render, render);
    }

    public MethodExpression getSelectedItemChangeListener() {
        //TODO nick - get, not eval
        return (MethodExpression) getStateHelper().eval(PropertyKeys.selectedItemChangeListener);
    }

    public void setSelectedItemChangeListener(MethodExpression selectedItemChangeListener) {
        getStateHelper().put(PropertyKeys.selectedItemChangeListener, selectedItemChangeListener);
    }


}
