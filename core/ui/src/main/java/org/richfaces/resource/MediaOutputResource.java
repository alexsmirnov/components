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



package org.richfaces.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.richfaces.component.AbstractMediaOutput;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
@DynamicResource
public class MediaOutputResource implements StateHolder, UserResource, CacheableResource {
    
    private String contentType;
    
    private boolean cacheable;
    
    private MethodExpression contentProducer;
    private ValueExpression expiresExpression;

    /*
     * TODO: add handling for expressions:
     *
     * 1. State saving
     * 2. Evaluation
     */
    private ValueExpression lastModifiedExpression;
    private ValueExpression timeToLiveExpression;
    private Object userData;

    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FacesContext facesContext = FacesContext.getCurrentInstance();

        contentProducer.invoke(facesContext.getELContext(), new Object[] {baos, userData});

        return new ByteArrayInputStream(baos.toByteArray());
    }

    public boolean isTransient() {
        return false;
    }

    public void setTransient(boolean newTransientValue) {
        throw new UnsupportedOperationException();
    }

    public Object saveState(FacesContext context) {
        Object[] state = new Object[4];

        // parent fields state saving
        state[0] = isCacheable(context) ? Boolean.TRUE : Boolean.FALSE;
        state[1] = getContentType();
        state[2] = UIComponentBase.saveAttachedState(context, userData);
        state[3] = UIComponentBase.saveAttachedState(context, contentProducer);

        return state;
    }

    public void restoreState(FacesContext context, Object stateObject) {
        Object[] state = (Object[]) stateObject;

        setCacheable((Boolean) state[0]);
        setContentType((String) state[1]);
        userData = UIComponentBase.restoreAttachedState(context, state[2]);
        contentProducer = (MethodExpression) UIComponentBase.restoreAttachedState(context, state[3]);
    }

    /**
     * @param uiMediaOutput
     */

    // TODO use ResourceComponent or exchange object as argument?
    public void initialize(AbstractMediaOutput uiMediaOutput) {
        this.setCacheable(uiMediaOutput.isCacheable());
        this.setContentType(uiMediaOutput.getMimeType());
        this.userData = uiMediaOutput.getValue();
        this.contentProducer = uiMediaOutput.getCreateContentExpression();
        this.lastModifiedExpression = uiMediaOutput.getValueExpression("lastModfied");
        this.expiresExpression = uiMediaOutput.getValueExpression("expires");
        this.timeToLiveExpression = uiMediaOutput.getValueExpression("timeToLive");
    }

    public boolean isCacheable(FacesContext context) {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }
    
    public Date getExpires(FacesContext context) {
        return null;
    }

    public int getTimeToLive(FacesContext context) {
        return -1;
    }

    public String getEntityTag(FacesContext context) {
        return null;
    }

    public Map<String, String> getResponseHeaders() {
        return null;
    }

    public Date getLastModified() {
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public int getContentLength() {
        return -1;
    }
}
