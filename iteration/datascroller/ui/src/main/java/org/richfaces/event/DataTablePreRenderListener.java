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

package org.richfaces.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import org.ajax4jsf.Messages;
import org.richfaces.DataScrollerUtils;
import org.richfaces.component.UIDataAdaptor;
import org.richfaces.component.UIDataScroller;
import org.richfaces.component.util.MessageUtil;
import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

public class DataTablePreRenderListener implements SystemEventListener {

    private static final Logger LOG = RichfacesLogger.COMPONENTS.getLogger();

    public boolean isListenerForSource(Object source) {
        return ((source instanceof UIDataAdaptor) || (source instanceof UIData));
    }

    public UIDataScroller processActiveDatascroller(FacesContext facesContext, List<UIDataScroller> dataScrollers,
        UIComponent dataTable) {
        UIDataScroller activeComponent = null;
        List<Object> values = new ArrayList<Object>(dataScrollers.size());

        String stateKey = dataTable.getClientId(facesContext) + UIDataScroller.SCROLLER_STATE_ATTRIBUTE;
        Map<String, Object> attributes = dataTable.getAttributes();
        Object pageValue = attributes.get(stateKey);

        boolean valid = true;

        if (pageValue == null) {

            for (UIDataScroller datascroller : dataScrollers) {
                Object nextPageValue = null;

                if (datascroller.isLocalPageSet()) {
                    nextPageValue = datascroller.getPage();
                    attributes.put(stateKey, nextPageValue);
                    datascroller.resetLocalPage();
                } else {
                    nextPageValue = datascroller.getValueExpression("page");
                }

                if (!values.isEmpty() && !same(values.get(values.size() - 1), nextPageValue)) {
                    valid = false;
                }

                values.add(nextPageValue);

                if (nextPageValue != null) {
                    activeComponent = datascroller;
                }
            }

        }

        if (activeComponent == null) {
            activeComponent = dataScrollers.get(dataScrollers.size() - 1);
        }

        if (!valid) {
            String formattedMessage = getPageDifferentMessage(facesContext, activeComponent, dataScrollers, values);
            LOG.error(formattedMessage);

        }

        return activeComponent;
    }

    public void processEvent(SystemEvent event) throws AbortProcessingException {
        UIComponent dataTable = (UIComponent) event.getSource();
        List<UIDataScroller> dataScrollers = DataScrollerUtils.findDataScrollers(dataTable);

        if (!dataScrollers.isEmpty()) {

            FacesContext facesContext = FacesContext.getCurrentInstance();

            UIDataScroller activeComponent = processActiveDatascroller(facesContext, dataScrollers, dataTable);

            int rowCount = DataScrollerUtils.getRowCount(dataTable);
            int rows = DataScrollerUtils.getRows(dataTable);

            Integer pageCount = DataScrollerUtils.getPageCount(dataTable, rowCount, rows);

            int page = activeComponent.getPage();
            int newPage = -1;

            if (page < 1) {
                newPage = 1;
            } else if (page > pageCount) {
                newPage = (pageCount != 0 ? pageCount : 1);
            }

            if (newPage != -1) {
                Object label = MessageUtil.getLabel(facesContext, activeComponent);
                String formattedMessage = Messages.getMessage(Messages.DATASCROLLER_PAGE_MISSING, new Object[] { label,
                    page, pageCount, newPage });

                LOG.warn(formattedMessage);

                page = newPage;
                dataTable.getAttributes().put(
                    dataTable.getClientId(facesContext) + UIDataScroller.SCROLLER_STATE_ATTRIBUTE, page);
            }

            int first;

            String lastPageMode = activeComponent.getLastPageMode();

            if (lastPageMode == null) {
                lastPageMode = UIDataScroller.PAGEMODE_SHORT;
            } else if (!UIDataScroller.PAGEMODE_SHORT.equals(lastPageMode)
                && !UIDataScroller.PAGEMODE_FULL.equals(lastPageMode)) {
                throw new IllegalArgumentException("Illegal value of 'lastPageMode' attribute: '" + lastPageMode + "'");
            }

            if (page != pageCount || UIDataScroller.PAGEMODE_SHORT.equals(lastPageMode)) {
                first = (page - 1) * rows;
            } else {
                first = rowCount - rows;
                if (first < 0) {
                    first = 0;
                }
            }
            dataTable.getAttributes().put("first", first);
        }
    }

    private String getPageDifferentMessage(FacesContext facesContext, UIDataScroller activeComponent,
        List<UIDataScroller> dataScrollers, List<Object> values) {
        StringBuilder builder = new StringBuilder("\n[");
        Iterator<UIDataScroller> scrollerItr = dataScrollers.iterator();
        Iterator<Object> valueItr = values.iterator();

        while (scrollerItr.hasNext()) {
            UIDataScroller next = scrollerItr.next();
            builder.append(MessageUtil.getLabel(facesContext, next));
            builder.append(": ");

            Object value = valueItr.next();
            if (value instanceof ValueExpression) {
                builder.append(((ValueExpression) value).getExpressionString());
            } else {
                builder.append(value);
            }

            builder.append(scrollerItr.hasNext() ? ",\n" : "]");
        }

        return Messages.getMessage(Messages.DATASCROLLER_PAGES_DIFFERENT, new Object[] {
            MessageUtil.getLabel(facesContext, activeComponent), builder });

    }

    private static boolean same(Object o1, Object o2) {
        if (o1 instanceof ValueExpression && o2 instanceof ValueExpression) {
            ValueExpression ve1 = (ValueExpression) o1;
            ValueExpression ve2 = (ValueExpression) o2;

            if (same(ve1.getExpressionString(), ve2.getExpressionString())
                && same(ve1.getExpectedType(), ve2.getExpectedType())) {
                return true;
            }
        }

        return (o1 != null && o1.equals(o2)) || (o1 == null && o2 == null);
    }
}
