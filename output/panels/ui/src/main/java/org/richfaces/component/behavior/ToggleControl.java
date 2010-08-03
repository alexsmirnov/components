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

package org.richfaces.component.behavior;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehaviorContext;

import org.ajax4jsf.component.behavior.ClientBehavior;
import org.ajax4jsf.renderkit.RendererUtils;
import org.richfaces.component.AbstractTogglePanel;

/**
 * @author akolonitsky
 *
 */
public class ToggleControl extends ClientBehavior {

    public static final String BEHAVIOR_ID = "org.richfaces.component.behavior.ToggleControl";

    private enum PropertyKeys {
        event,
        targetItem,
        forPanel,
        disableDefault
    }

    public String getEvent() {
        return (String) getStateHelper().eval(PropertyKeys.event);
    }

    public void setEvent(String eventName) {
        getStateHelper().eval(PropertyKeys.event, eventName);
    }

    public String getTargetItem() {
        return (String) getStateHelper().eval(PropertyKeys.targetItem);
    }

    public void setTargetItem(String target) {
        getStateHelper().put(PropertyKeys.targetItem, target);
    }

    public String getForPanel() {
        //TODO nick - get, not eval
        return (String) getStateHelper().eval(PropertyKeys.forPanel);
    }

    public void setForPanel(String selector) {
        getStateHelper().put(PropertyKeys.forPanel, selector);
    }

    //TODO nick - argument type is incorrect
    public void setDisableDefault(String disableDefault) {
        getStateHelper().put(PropertyKeys.disableDefault, disableDefault);
    }

    public Boolean getDisableDefault() {
        return Boolean.valueOf(String.valueOf(getStateHelper().eval(PropertyKeys.disableDefault, true)));
    }

    public String getPanelId(ClientBehaviorContext behaviorContext) throws FacesException {
        return getPanel(behaviorContext.getComponent()).getClientId();
    }

    public AbstractTogglePanel getPanel(UIComponent comp) throws FacesException {
        String target = this.getForPanel();

        if (target != null) {

            UIComponent targetComponent = RendererUtils.getInstance()
                    .findComponentFor(comp, target);

            if (null != targetComponent) {
                return (AbstractTogglePanel) targetComponent;
            } else {
                throw new FacesException("Parent panel for control (id="
                        + comp.getClientId(getFacesContext()) + ") has not been found.");
            }
        } else {
            UIComponent control = comp;
            while (control != null) {
                if (control instanceof AbstractTogglePanel) {
                    return (AbstractTogglePanel) control;
                }

                control = control.getParent();
            }
            throw new FacesException("Parent panel for control (id="
                    + comp.getClientId(getFacesContext()) + ") has not been found.");
        }
    }

    @Override
    public String getRendererType() {
        return BEHAVIOR_ID;
    }

    @Override
    public void setLiteralAttribute(String name, Object value) {
//        if (compare(PropertyKeys.operation, name)) {
//            setOperation((String) value);
//        } else if (compare(PropertyKeys.target, name)) {
//            setTargetItem((String) value);
//        } else if (compare(PropertyKeys.selector, name)) {
//            setForPanel((String) value);
//        }
    }
}