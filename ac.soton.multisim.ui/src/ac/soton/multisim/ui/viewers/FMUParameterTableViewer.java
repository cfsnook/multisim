/**
 * Copyright (c) 2014 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.multisim.ui.viewers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Objects;

import ac.soton.multisim.FMUParameter;
import ac.soton.multisim.ui.providers.ColumnProvider;

/**
 * Table viewer for editing FMU parameters.
 * 
 * @author vitaly
 *
 */
public class FMUParameterTableViewer extends ColumnProviderTableViewer {
	
	public static final String MODIFIED_PARAMETERS = "modified_key";
	private Set<Object> modified = new HashSet<Object>();
	
	@Override
	public Object getData(String key) {
		if (key.equals(MODIFIED_PARAMETERS))
			return modified;
		return super.getData(key);
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public void setData(String key, Object value) {
		if (key.equals(MODIFIED_PARAMETERS))
			modified = (Set<Object>) value;
		super.setData(key, value);
	}



	public FMUParameterTableViewer(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected List<ColumnProvider> createColumnProviders() {
		ArrayList<ColumnProvider> providers = new ArrayList<ColumnProvider>();
		providers.add(new ColumnProvider("Name", 168, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FMUParameter) element).getName();
			}}));
		providers.add(new ColumnProvider("Type", 70, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FMUParameter) element).getType().toString();
			}}));
		providers.add(new ColumnProvider("Value", 70, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object value = ((FMUParameter) element).getStartValue();
				return value == null ? null : value.toString();
			}}, 
			new StartValueEditingSupport(this)));
		providers.add(new ColumnProvider("Description", 170, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FMUParameter) element).getComment();
			}}));
		return providers;
	}
	
	private class StartValueEditingSupport extends EditingSupport {
		
		public StartValueEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new AbstractVariableValueCellEditor(((TableViewer) getViewer()).getTable(), (FMUParameter) element);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			Object value = ((FMUParameter) element).getStartValue();
			return value == null ? "" : value.toString();
		}

		@Override
		protected void setValue(Object element, Object value) {
			FMUParameter param = (FMUParameter) element;
			String valueStr = String.valueOf(value);
			Object startValue = null;
			if (valueStr == null || valueStr.trim().isEmpty()) {
				startValue = param.getValue();
			} else {
				switch (param.getType()) {
				case BOOLEAN:
					startValue = Boolean.parseBoolean(valueStr);
					break;
				case INTEGER:
					startValue = Integer.parseInt(valueStr);
					break;
				case REAL:
					startValue = Double.parseDouble(valueStr);
					break;
				case STRING:
					startValue = valueStr;
				}
			}
			param.setStartValue(startValue);
			getViewer().update(element, null);
			if (Objects.equal(startValue, param.getValue()))
				modified.remove(param);
			else
				modified.add(param);
		}
	}

}
