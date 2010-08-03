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

package org.ajax4jsf.renderkit;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.faces.render.ClientBehaviorRenderer;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.RenderKitFactory;

import org.ajax4jsf.component.AjaxClientBehavior;
import org.ajax4jsf.component.behavior.AjaxBehavior;
import org.ajax4jsf.javascript.JSFunction;

/**
 * @author Anton Belevich
 *
 */
@FacesBehaviorRenderer(rendererType = "org.ajax4jsf.behavior.Ajax",
                       renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT)
                       
@ResourceDependencies(
    {@ResourceDependency(library = "javax.faces", name = "jsf.js"),
        @ResourceDependency(name = "jquery.js", target = "head"), 
        @ResourceDependency(name = "richfaces.js", target = "head")
    }
)

public class AjaxBehaviorRenderer extends ClientBehaviorRenderer {
    
    private final RendererUtils utils = RendererUtils.getInstance();
    
    @Override
    public void decode(FacesContext context, UIComponent component, ClientBehavior behavior) {
        if (null == context || null == component || null == behavior) {
            throw new NullPointerException();
        }

        if (!(behavior instanceof AjaxBehavior)) {
            throw new IllegalArgumentException("org.ajax4jsf.component.behavior.AjaxBehavior required: " + behavior);
        }

        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;

        if (ajaxBehavior.isDisabled()) {
            return;
        }

        component.queueEvent(createEvent(component, ajaxBehavior));
    }

    private AjaxBehaviorEvent createEvent(UIComponent component, AjaxBehavior ajaxBehavior) {
        AjaxBehaviorEvent event = new AjaxBehaviorEvent(component, ajaxBehavior);
        PhaseId phaseId;
        
        if (isImmediate(component, ajaxBehavior)) {
            phaseId = PhaseId.APPLY_REQUEST_VALUES;
        } else if (isBypassUpdates(component, ajaxBehavior)) {
            //TODO nick - what if behavior is immediate & bypassUpdates in the same time?
            phaseId = PhaseId.PROCESS_VALIDATIONS;
        } else {
            phaseId = PhaseId.INVOKE_APPLICATION;
        }

        event.setPhaseId(phaseId);

        return event;
    }

    private boolean isImmediate(UIComponent component, AjaxBehavior ajaxBehavior) {
        boolean immediate = ajaxBehavior.isImmediate();
        if (!immediate) {
            if (component instanceof EditableValueHolder) {
                immediate = ((EditableValueHolder) component).isImmediate();
            } else if (component instanceof ActionSource) {
                immediate = ((ActionSource) component).isImmediate();
            }
        }
        
        return immediate;
    }

    private boolean isBypassUpdates(UIComponent component, AjaxBehavior ajaxBehavior) {
        boolean bypassUpdates = ajaxBehavior.isBypassUpdates();
        if (!bypassUpdates) {
            bypassUpdates = utils.isBooleanAttribute(component, "bypassUpdates");
        }
        
        return bypassUpdates;
    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior) {
        String script = null;
        if (behavior instanceof AjaxBehavior) {
            script = buildAjaxCommand(behaviorContext, (AjaxBehavior) behavior);
        }

        return script;
    }

    public String buildAjaxCommand(ClientBehaviorContext bContext, AjaxBehavior behavior) {
        UIComponent parent = bContext.getComponent();
        FacesContext context = bContext.getFacesContext();
        JSFunction ajaxFunction = AjaxRendererUtils.buildAjaxFunction(context, parent,
                                      AjaxRendererUtils.AJAX_FUNCTION_NAME);
        AjaxEventOptions options = buildOptions(context, bContext, behavior);
        ajaxFunction.addParameter(options);
        return ajaxFunction.toString();
    }

    public AjaxEventOptions buildOptions(FacesContext context, ClientBehaviorContext bContext,
            AjaxClientBehavior behavior) {
        UIComponent parent = bContext.getComponent();
        String eventName = bContext.getEventName();
        AjaxEventOptions options = AjaxRendererUtils.buildEventOptions(context, parent, behavior);
        options.setParameter(AjaxRendererUtils.BEHAVIOR_EVENT_PARAMETER, eventName);
        return options;
    }
}
