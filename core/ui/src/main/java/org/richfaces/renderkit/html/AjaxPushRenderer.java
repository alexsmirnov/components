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
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

import org.ajax4jsf.javascript.JSFunction;
import org.ajax4jsf.javascript.JSFunctionDefinition;
import org.ajax4jsf.javascript.JSReference;
import org.ajax4jsf.renderkit.AjaxEventOptions;
import org.ajax4jsf.renderkit.AjaxRendererUtils;
import org.ajax4jsf.renderkit.HandlersChain;
import org.ajax4jsf.renderkit.RendererBase;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.richfaces.cdk.annotations.JsfRenderer;
import org.richfaces.component.AbstractPush;
import org.richfaces.resource.PushResource;

/**
 * @author shura
 */
@ResourceDependencies({@ResourceDependency(library = "javax.faces", name = "jsf.js") ,
    @ResourceDependency(name = "jquery.js") , @ResourceDependency(name = "richfaces.js")})
@JsfRenderer
public class AjaxPushRenderer extends RendererBase {

    public static final String COMPONENT_FAMILY = "org.richfaces.Push";

    public static final String RENDERER_TYPE = "org.richfaces.PushRenderer";

    public static final String PUSH_INTERVAL_PARAMETER = "A4J.AJAX.Push.INTERVAL";

    public static final int DEFAULT_PUSH_INTERVAL = 1000;

    public static final int DEFAULT_PUSH_WAIT = Integer.MIN_VALUE;

    @Override
    protected void queueComponentEventForBehaviorEvent(FacesContext context, UIComponent component, String eventName) {
        super.queueComponentEventForBehaviorEvent(context, component, eventName);
        
        if (AbstractPush.DATA_AVAILABLE.equals(eventName) || AbstractPush.ON_DATA_AVAILABLE.equals(eventName)) {
            new ActionEvent(component).queue();
        } 
    }
    
    /* (non-Javadoc)
      * @see org.ajax4jsf.renderkit.RendererBase#doEncodeEnd(javax.faces.context.ResponseWriter,
      * javax.faces.context.FacesContext, javax.faces.component.UIComponent)
      */
    protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        AbstractPush push = (AbstractPush) component;
        writer.startElement(HTML.SPAN_ELEM, component);
        writer.writeAttribute(HTML.STYLE_ATTRIBUTE, "display:none;", null);
        getUtils().encodeId(context, component);

        //TODO - ?
        getUtils().encodeBeginFormIfNessesary(context, component);
        // pushing script.
        writer.startElement(HTML.SCRIPT_ELEM, component);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
        StringBuffer script = new StringBuffer("\n");
        if (push.isEnabled()) {
            JSFunction function = new JSFunction("RichFaces.startPush");
            // Set dummy form id, if nessesary.
            Map<String, Object> options = new HashMap<String, Object>();

            int interval = push.getInterval();
            if (!getUtils().shouldRenderAttribute(interval)) {
                String intervalInitParameter = context.getExternalContext().getInitParameter(PUSH_INTERVAL_PARAMETER);
                if (null != intervalInitParameter) {
                    interval = Integer.parseInt(intervalInitParameter);
                } else {
                    interval = DEFAULT_PUSH_INTERVAL;
                }
            }

            options.put("interval", interval);
            options.put("pushResourceUrl", new PushResource().getRequestPath());
            options.put("pushId", push.getListenerId(context));
            options.put("clientId", component.getClientId(context));

            HandlersChain handlersChain = new HandlersChain(push);
            handlersChain.addInlineHandlerFromAttribute(context, AbstractPush.ON_DATA_AVAILABLE);
            handlersChain.addBehaviors(context, AbstractPush.DATA_AVAILABLE);

            if (!handlersChain.hasSubmittingBehavior()) {
                JSFunction ajaxFunction = AjaxRendererUtils
                    .buildAjaxFunction(context, push, AjaxRendererUtils.AJAX_FUNCTION_NAME);
                AjaxEventOptions eventOptions = AjaxRendererUtils.buildEventOptions(context, push);
                if (!eventOptions.isEmpty()) {
                    ajaxFunction.addParameter(eventOptions);
                }
                handlersChain.addInlineHandlerAsValue(context, ajaxFunction.toScript());
            }

            String handler = handlersChain.toScript();

            if (handler != null) {
                JSFunctionDefinition dataAvailableHandler = new JSFunctionDefinition(JSReference.EVENT);
                dataAvailableHandler.addToBody(handler);
                options.put(AbstractPush.ON_DATA_AVAILABLE, dataAvailableHandler);
            }
            function.addParameter(options);
            script.append(function.toScript());
        } else {
            script.append("RichFaces.stopPush('").append(push.getListenerId(context)).append("')");
        }
        script.append(";\n");
        writer.writeText(script.toString(), null);
        writer.endElement(HTML.SCRIPT_ELEM);
        getUtils().encodeEndFormIfNessesary(context, component);
        writer.endElement(HTML.SPAN_ELEM);
    }

    /* (non-Javadoc)
      * @see org.ajax4jsf.renderkit.RendererBase#getComponentClass()
      */
    protected Class<? extends UIComponent> getComponentClass() {
        // only push component is allowed.
        return AbstractPush.class;
    }

    @Override
    protected void doDecode(FacesContext context, UIComponent component) {
        super.doDecode(context, component);

        AbstractPush push = (AbstractPush) component;
        if (push.isEnabled()) {
            Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
            if (requestParameterMap.get(push.getClientId(context)) != null) {
                new ActionEvent(push).queue();
            }
        }
    }

}
