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

package org.richfaces.renderkit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.javascript.JSFunction;
import org.ajax4jsf.renderkit.AjaxEventOptions;
import org.ajax4jsf.renderkit.AjaxRendererUtils;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.richfaces.component.Row;
import org.richfaces.component.UIDataTable;
import org.richfaces.component.UIDataTableBase;
import org.richfaces.component.UISubTable;
import org.richfaces.component.util.HtmlUtil;

/**
 * @author Anton Belevich
 */
@ResourceDependencies({@ResourceDependency(library = "javax.faces", name = "jsf.js"),
    @ResourceDependency(name = "jquery.js"), @ResourceDependency(name = "richfaces.js"),
    @ResourceDependency(name = "richfaces-event.js"), @ResourceDependency(name = "richfaces-base-component.js"),
    @ResourceDependency(name = "datatable.js"), @ResourceDependency(name = "datatable.ecss")})
public class DataTableRenderer extends AbstractTableRenderer {

    private class  DataTableHiddenEncodeStrategy implements EncodeStrategy {
        public void begin(ResponseWriter writer, FacesContext context, UIComponent component, Object[] params) throws IOException {
            UIDataTable dataTable = (UIDataTable)component;
            
            writer.startElement(HTML.TBODY_ELEMENT, dataTable);
            writer.writeAttribute(HTML.ID_ATTRIBUTE, dataTable.getClientId(context) + HIDDEN_CONTAINER_ID, null);
            writer.writeAttribute(HTML.STYLE_ATTRIBUTE, "display: none", null);
            writer.startElement(HTML.TR_ELEMENT, dataTable);
            writer.startElement(HTML.TD_ELEM, dataTable);
        }

        public void end(ResponseWriter writer, FacesContext context, UIComponent component, Object[] params) throws IOException {
            writer.endElement(HTML.TD_ELEM);
            writer.endElement(HTML.TR_ELEMENT);
            writer.endElement(HTML.TBODY_ELEMENT);
        }
    };
    
    private class RichHeaderEncodeStrategy implements EncodeStrategy {

        public void begin(ResponseWriter writer, FacesContext context, UIComponent component, Object [] params) throws IOException {
            org.richfaces.component.UIColumn column = (org.richfaces.component.UIColumn) component;
            writer.writeAttribute(HTML.ID_ATTRIBUTE, column.getClientId(context), null);

            if (isSortable(column)) {
                //TODO :anton -> should component be selfSorted
                writer.startElement(HTML.SPAN_ELEM, column);
                writer.writeAttribute(HTML.CLASS_ATTRIBUTE, "rich-table-sortable-header", null);
            }
        }

        public void end(ResponseWriter writer, FacesContext context, UIComponent component, Object [] params) throws IOException {
            org.richfaces.component.UIColumn column = (org.richfaces.component.UIColumn) component;
            if (isSortable(column)) {
                writer.endElement(HTML.SPAN_ELEM);
            }
        }
    }
    
    public void encodeTableStructure(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable)
        throws IOException {
        if (dataTable instanceof UIDataTable) {
            encodeCaption(writer, context, (UIDataTable) dataTable);
            // TODO nick - do we need this element if "columnsWidth" is absent?
            writer.startElement(HTML.COLGROUP_ELEMENT, dataTable);
           
            int columns = getColumnsCount(dataTable);
            writer.writeAttribute(HTML.SPAN_ELEM, String.valueOf(columns), null);
            String columnsWidth = (String) dataTable.getAttributes().get("columnsWidth");

            if (columnsWidth != null) {

                String[] widths = columnsWidth.split(",");
                for (int i = 0; i < widths.length; i++) {
                    writer.startElement(HTML.COL_ELEMENT, dataTable);
                    writer.writeAttribute(HTML.WIDTH_ATTRIBUTE, widths[i], null);
                    writer.endElement(HTML.COL_ELEMENT);
                }
                
            }
            
            writer.endElement(HTML.COLGROUP_ELEMENT);
        }
    }

    @Override
    public void encodeBeforeRows(ResponseWriter writer, FacesContext facesContext,UIDataTableBase dataTableBase, boolean encodeParentTBody, boolean partialUpdate) throws IOException {
        if(encodeParentTBody) {
            if(partialUpdate) {
                partialStart(facesContext, dataTableBase.getClientId(facesContext) +":tb");
            }
            encodeTableBodyStart(writer, facesContext, dataTableBase);
        }    
    }
    
    @Override
    public void encodeAfterRows(ResponseWriter writer, FacesContext facesContext, UIDataTableBase dataTableBase, boolean encodeParentTBody, boolean partialUpdate) throws IOException {
        if(encodeParentTBody) {
            encodeTableBodyEnd(writer);
            if(partialUpdate) {
                partialEnd(facesContext);
            }
        }
    }
    
        
    public void encodeRow(ResponseWriter writer, FacesContext facesContext, RowHolderBase holder) throws IOException {
        RowHolder rowHolder = (RowHolder) holder;
        Row row = rowHolder.getRow();

        UIDataTable dataTable = (UIDataTable)row;
        
        boolean partialUpdate = rowHolder.isUpdatePartial(); 
        boolean parentTbodyStart = rowHolder.isEncodeParentTBody();
        boolean tbodyStart = parentTbodyStart;
                
        rowHolder.setRowStart(true);

        Iterator<UIComponent> components = row.columns();
        while (components.hasNext()) {
        
            UIComponent child = components.next();
            if(child instanceof Row) {
                boolean isSubtable = (child instanceof UISubTable); 
                //new row -> close </tr>
                if (rowHolder.getProcessCell() != 0) {
                    encodeRowEnd(writer);

                    if(isSubtable) {
                        encodeTableBodyEnd(writer);
                        tbodyStart = false;
                        
                        if (partialUpdate) {
                            partialEnd(facesContext);
                        }
                    }
                }
                
                rowHolder.nextCell();
                
                if(isSubtable && partialUpdate){
                    String id = dataTable.getRelativeClientId(facesContext) + ":"+ child.getId() +":c";
                    partialStart(facesContext, id);
                } 
                
                child.encodeAll(facesContext);
                
                if (isSubtable && partialUpdate) {
                    partialEnd(facesContext);
                }
                
            } else if(child instanceof UIColumn) {
                
                if(!parentTbodyStart && !tbodyStart) {
                    if (partialUpdate) {
                        partialStart(facesContext, dataTable.getRelativeClientId(facesContext) + ":tb");
                    }
                    
                    encodeTableBodyStart(writer, facesContext, dataTable);
                    rowHolder.setRowStart(true);
                    tbodyStart = true;
                }
                
                encodeColumn(facesContext, writer, (UIColumn)child, rowHolder);
                
                if(!components.hasNext()) {
                    encodeRowEnd(writer);
                    
                    if(!parentTbodyStart && tbodyStart) {
                        encodeTableBodyEnd(writer);
                        tbodyStart = false;
                    
                        if(partialUpdate) {
                            partialEnd(facesContext);
                        }
                    }
                }
            }
        }
    }

    public boolean encodeParentTBody(UIDataTableBase dataTableBase) {
        Iterator<UIComponent> iterator = dataTableBase.columns();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof Row) {
                return false;
            }
        }
        return true;
    }
  
    protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        UIDataTableBase dataTable = (UIDataTableBase) component;
        encodeTableStart(writer, context, dataTable);
        encodeTableFacets(writer, context, dataTable);
    }

    protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        encodeTableEnd(writer);
    }

    protected Class<? extends UIComponent> getComponentClass() {
        return UIDataTable.class;
    }

    public void encodeCaption(ResponseWriter writer, FacesContext context, UIDataTable dataTable) throws IOException {
        UIComponent caption = dataTable.getCaption();

        if (caption == null) {
            return;
        }

        if (!caption.isRendered()) {
            return;
        }

        writer.startElement(HTML.CAPTION_ELEMENT, dataTable);

        String captionClass = (String) dataTable.getAttributes().get("captionClass");
        String captionSkinClass = getCaptionSkinClass();

        captionClass = HtmlUtil.concatClasses(captionClass, captionSkinClass);
        writer.writeAttribute(HTML.CLASS_ATTRIBUTE, captionClass, "captionClass");
        
        String captionStyle = (String) dataTable.getAttributes().get("captionStyle");
        if (captionStyle != null && captionStyle.trim().length() != 0) {
            writer.writeAttribute(HTML.STYLE_ATTRIBUTE, captionStyle, "captionStyle");
        }

        caption.encodeAll(context);

        writer.endElement(HTML.CAPTION_ELEMENT);
    }

    public EncodeStrategy getHeaderEncodeStrategy(UIComponent column, String facetName) {
        return (column instanceof org.richfaces.component.UIColumn && UIDataTableBase.HEADER.equals(facetName)) ? new RichHeaderEncodeStrategy() : new SimpleHeaderEncodeStrategy();
    }

    public boolean containsThead() {
        return true;
    }

    public boolean isSortable(UIColumn column) {
        if (column instanceof org.richfaces.component.UIColumn) {
            //TODO: anton - add check for the "comparator" property
            return ((org.richfaces.component.UIColumn) column).getValueExpression("sortBy") != null;
        }
        return false;
    }

    public void encodeClientScript(ResponseWriter writer, FacesContext facesContext, UIDataTableBase dataTableBase) throws IOException {
        UIDataTable dataTable = (UIDataTable) dataTableBase;
       
        writer.startElement(HTML.SCRIPT_ELEM, dataTable);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.JAVASCRIPT_TYPE, null);

        JSFunction function = new JSFunction("new RichFaces.ui.DataTable");
        function.addParameter(dataTable.getClientId(facesContext));

        AjaxEventOptions ajaxEventOptions = AjaxRendererUtils.buildEventOptions(facesContext, dataTable);

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("ajaxEventOptions", ajaxEventOptions.getParameters());
        function.addParameter(options);

        writer.writeText(function.toScript(), null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }

    @Override
    public void encodeHiddenInput(ResponseWriter writer, FacesContext context, UIDataTableBase component) throws IOException {
    }
    
    public String getTableSkinClass() {
        return "rf-dt";
    }

    public String getCaptionSkinClass() {
        return "rf-dt-cap";
    }

    public String getRowSkinClass() {
        return "rf-dt-r";
    }

    public String getFirstRowSkinClass() {
        return "rf-dt-f-r";
    }

    public String getCellSkinClass() {
        return "rf-dt-c";
    }

    public String getHeaderSkinClass() {
        return "rf-dt-h";
    }

    public String getHeaderFirstSkinClass() {
        return "rf-dt-h-f";
    }

    public String getHeaderCellSkinClass() {
        return "rf-dt-h-c";
    }

    public String getColumnHeaderSkinClass() {
        return "rf-dt-sh";
    }

    public String getColumnHeaderFirstSkinClass() {
        return "rf-dt-sh-f";
    }

    public String getColumnHeaderCellSkinClass() {
        return "rf-dt-sh-c";
    }

    public String getColumnFooterSkinClass() {
        return "rf-dt-sf";
    }

    public String getColumnFooterFirstSkinClass() {
        return "rf-dt-sf-f";
    }

    public String getColumnFooterCellSkinClass() {
        return "rf-dt-sf-c";
    }

    public String getFooterSkinClass() {
        return "rf-dt-f";
    }

    public String getFooterFirstSkinClass() {
        return "rf-dt-f-f";
    }

    public String getFooterCellSkinClass() {
        return "rf-dt-f-c";
    }

    public String getNoDataClass() {
        return "rf-dt-nd-c";
    }

    protected void setupTableStartElement(FacesContext context, UIComponent component) {
        setupTableStartElement(context, component, HTML.TH_ELEM);
    }
    
    public EncodeStrategy getHiddenContainerStrategy(UIDataTableBase dataTableBase) {
        return new DataTableHiddenEncodeStrategy();
    }
    
}
