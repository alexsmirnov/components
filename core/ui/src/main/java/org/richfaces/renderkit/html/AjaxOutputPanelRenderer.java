/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.richfaces.renderkit.html;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.context.AjaxContext;
import org.ajax4jsf.renderkit.RendererBase;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.richfaces.cdk.annotations.JsfRenderer;
import org.richfaces.component.AbstractOutputPanel;

/**
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/02/01 15:31:27 $
 */
@JsfRenderer(type = "org.richfaces.OutputPanelRenderer", family = AbstractOutputPanel.COMPONENT_FAMILY)
public class AjaxOutputPanelRenderer extends RendererBase {

    private static final String[] STYLE_ATTRIBUTES = new String[]{"style", "class"};

    private boolean hasNoneLayout(UIComponent component) {
        //TODO - A1 won't support 'none' layout
        return false; //"none".equals(component.getAttributes().get("layout"));
    }

    /* (non-Javadoc)
      * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext,
      * javax.faces.component.UIComponent)
      */
    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //
        AbstractOutputPanel panel = (AbstractOutputPanel) component;
        if (hasNoneLayout(panel)) {
            if (component.getChildCount() > 0) {
                AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
                boolean ajaxRequest = ajaxContext.isAjaxRequest();
                Set<String> ajaxRenderedAreas = ajaxContext.getAjaxRenderedAreas();
                for (UIComponent child : component.getChildren()) {
                    String childId = child.getClientId(context);
                    if (child.isRendered()) {
                        child.encodeAll(context);
                    } else {
                        // Render "dummy" span.
                        ResponseWriter out = context.getResponseWriter();
                        out.startElement(HTML.SPAN_ELEM, child);
                        out.writeAttribute(HTML.ID_ATTRIBUTE, childId, HTML.ID_ATTRIBUTE);
                        out.writeAttribute(HTML.STYLE_ATTRIBUTE, "display: none;", "style");
                        out.endElement(HTML.SPAN_ELEM);
                    }
                    // register child as rendered
                    if (ajaxRequest && null != ajaxRenderedAreas) {
                        ajaxRenderedAreas.add(childId);
                    }
                }
            }
        } else {
            renderChildren(context, component);
        }
    }

    /* (non-Javadoc)
      * @see javax.faces.render.Renderer#getRendersChildren()
      */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /* (non-Javadoc)
      * @see org.ajax4jsf.renderkit.RendererBase#getComponentClass()
      */
    @Override
    protected Class<? extends UIComponent> getComponentClass() {
        return AbstractOutputPanel.class;
    }

    /* (non-Javadoc)
      * @see org.ajax4jsf.renderkit.RendererBase#doEncodeBegin(javax.faces.context.ResponseWriter,
      * javax.faces.context.FacesContext, javax.faces.component.UIComponent)
      */
    @Override
    protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component)
        throws IOException {

        AbstractOutputPanel panel = (AbstractOutputPanel) component;
        if (!hasNoneLayout(component)) {
            writer.startElement(getTag(panel), panel);
            getUtils().encodeId(context, component);
            getUtils().encodePassThru(context, component, null);
            getUtils().encodeAttributesFromArray(context, component, STYLE_ATTRIBUTES);
        }
    }

    /**
     * @param panel
     * @return
     */
    private String getTag(AbstractOutputPanel panel) {
        Object layout = panel.getAttributes().get("layout");
        return "block".equals(layout) ? HTML.DIV_ELEM : HTML.SPAN_ELEM;
    }

    /* (non-Javadoc)
      * @see org.ajax4jsf.renderkit.RendererBase#doEncodeEnd(javax.faces.context.ResponseWriter,
      * javax.faces.context.FacesContext, javax.faces.component.UIComponent)
      */
    @Override
    protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        AbstractOutputPanel panel = (AbstractOutputPanel) component;
        if (!hasNoneLayout(component)) {
            writer.endElement(getTag(panel));
        }
        if (panel.isKeepTransient()) {
            markNoTransient(component);
        }
    }

    /**
     * Set "transient" flag to false for component and all its children ( recursive ).
     *
     * @param component
     */
    private void markNoTransient(UIComponent component) {
        for (Iterator<UIComponent> iter = component.getFacetsAndChildren(); iter.hasNext();) {
            UIComponent element = iter.next();
            markNoTransient(element);
            element.setTransient(false);
        }

    }

}
