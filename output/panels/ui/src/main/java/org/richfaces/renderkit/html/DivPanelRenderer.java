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

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.renderkit.RendererBase;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.richfaces.component.AbstractDivPanel;

/**
 * @author akolonitsky
 * 
 */
//TODO nick - use Renderer instead of RendererBase
public class DivPanelRenderer extends RendererBase {

    public static final String[] ATTRIBUTES = new String[] {
        "lang",
        "onclick",
        "ondblclick",
        "onmousedown",
        "onmousemove",
        "onmouseout",
        "onmouseover",
        "onmouseup",
        "title",
        "style",
        "styleClass",
        "dir",
    };

    @Override
    protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        super.doEncodeBegin(writer, context, component);

        writer.startElement(HTML.DIV_ELEM, component);
        writer.writeAttribute("id", component.getClientId(context), "clientId");

        writeAttributes(writer, component, ATTRIBUTES);
    }

    private void writeAttributes(ResponseWriter writer, UIComponent component, String[] attributes) throws IOException {
        Map<String, Object> componentAttributes = component.getAttributes();
        for (String attrName : attributes) {
            Object attrValue = componentAttributes.get(attrName);
            //TODO nick - ???
            if (!"null".equalsIgnoreCase(String.valueOf(attrValue))) {
                writer.writeAttribute(attrName, attrValue, attrName); // TODO Use RendererUtils
            }
        }
    }

    @Override
    protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        super.doEncodeEnd(writer, context, component);

        writeJavaScript(writer, context, component);

        writer.endElement(HTML.DIV_ELEM);
    }

    protected void writeJavaScript(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        Object script = getScriptObject(context, component);
        if (script != null) {
            //TODO nick - how does script relate to DIV?
            writer.startElement(HTML.SCRIPT_ELEM, component);
            writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", "type");
            writer.writeText(script, null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }

    protected Object getScriptObject(FacesContext context, UIComponent component) {
        return null;
    }

    protected Map<String, Object> getScriptObjectOptions(FacesContext context, UIComponent component) {
        return null;
    }

    @Override
    protected Class<? extends UIComponent> getComponentClass() {
        return AbstractDivPanel.class;
    }
}

