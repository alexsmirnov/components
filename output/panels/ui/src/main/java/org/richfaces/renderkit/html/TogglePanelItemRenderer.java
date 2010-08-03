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

package org.richfaces.renderkit.html;

import org.ajax4jsf.javascript.JSObject;
import org.richfaces.component.AbstractTogglePanelItem;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author akolonitsky
 * @since -4712-01-01
 */
@ResourceDependencies({
    @ResourceDependency(library = "javax.faces", name = "jsf.js"),
    @ResourceDependency(name = "jquery.js"),
    @ResourceDependency(name = "richfaces.js"),
    @ResourceDependency(name = "richfaces-event.js"),
    @ResourceDependency(name = "richfaces-base-component.js"),
    @ResourceDependency(name = "script/TogglePanelItem.js") })
public class TogglePanelItemRenderer extends DivPanelRenderer {

    @Override
    protected void writeJavaScript(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        // All script must be written by TogglePanel using method getScriptObject 
    }

    @Override
    protected JSObject getScriptObject(FacesContext context, UIComponent component) {
        return new JSObject("RichFaces.ui.TogglePanelItem", component.getClientId(), getScriptObjectOptions(context, component));
    }

    @Override
    protected Map<String, Object> getScriptObjectOptions(FacesContext context, UIComponent component) {
        AbstractTogglePanelItem panelItem = (AbstractTogglePanelItem) component;

        Map<String, Object> options = new HashMap<String, Object>(2);
        options.put("name", panelItem.getName());
        options.put("togglePanelId", panelItem.getParent().getClientId());
        options.put("switchMode", panelItem.getSwitchType());

        return options;
    }

    @Override
    protected Class<? extends UIComponent> getComponentClass() {
        return AbstractTogglePanelItem.class;
    }
}

