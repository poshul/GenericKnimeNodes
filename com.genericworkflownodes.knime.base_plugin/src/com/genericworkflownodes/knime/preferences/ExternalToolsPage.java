/**
 * Copyright (c) 2012, Marc Röttig, Stephan Aiche.
 *
 * This file is part of GenericKnimeNodes.
 * 
 * GenericKnimeNodes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.genericworkflownodes.knime.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.ballproject.knime.GenericNodesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.genericworkflownodes.knime.toolfinderservice.ExternalTool;
import com.genericworkflownodes.knime.toolfinderservice.IToolLocatorService;

/**
 * Preferences page for to manage tool installations.
 * 
 * @author aiche
 */
public class ExternalToolsPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * The list of all available tool pathes.
	 */
	private List<ToolFieldEditor> toolPathes = new ArrayList<ToolFieldEditor>();

	/**
	 * Default c'tor.
	 */
	public ExternalToolsPage() {
		super();
		IPreferenceStore store = GenericNodesPlugin.getDefault()
				.getPreferenceStore();
		setPreferenceStore(store);
		// we do not need the apply key and do not support the restore default
		// key
		this.noDefaultAndApplyButton();
	}

	@Override
	public void init(IWorkbench wb) {
	}

	@Override
	protected Control createContents(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite c = new Composite(sc, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		c.setLayout(fillLayout);

		sc.setContent(c);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		IToolLocatorService toolLocator = (IToolLocatorService) PlatformUI
				.getWorkbench().getService(IToolLocatorService.class);

		if (toolLocator != null) {

			Map<String, List<ExternalTool>> plugin2tools = toolLocator
					.getToolsByPlugin();

			for (String pluginname : plugin2tools.keySet()) {
				// create a new group for each plugin
				Group group = new Group(c, SWT.SHADOW_ETCHED_IN);
				group.setText(pluginname);

				List<ExternalTool> tools = plugin2tools.get(pluginname);

				// sort each plugin by name
				Collections.sort(tools, new Comparator<ExternalTool>() {
					@Override
					public int compare(final ExternalTool o1,
							final ExternalTool o2) {
						return o1.getToolName().compareToIgnoreCase(
								o2.getToolName());
					}
				});

				// add each tool shipped with the current plugin to the GUI
				// group
				for (ExternalTool tool : tools) {
					ToolFieldEditor gToolEditor = new ToolFieldEditor(tool,
							group);
					gToolEditor.load();
					toolPathes.add(gToolEditor);
				}
			}
		}

		return sc;
	}

	@Override
	public boolean performOk() {
		saveToPreferenceStore();

		// Return true to allow dialog to close
		return true;
	}

	@Override
	protected void performApply() {
		saveToPreferenceStore();
	}

	/**
	 * Saves the entries of the FileFieldEditor.
	 */
	private void saveToPreferenceStore() {
		for (ToolFieldEditor gFieldEditor : toolPathes) {
			gFieldEditor.store();
		}
	}
}
