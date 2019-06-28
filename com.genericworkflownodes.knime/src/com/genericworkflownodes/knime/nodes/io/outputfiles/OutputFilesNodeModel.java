/**
 * Copyright (c) 2011-2013, Marc Röttig, Stephan Aiche.
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
package com.genericworkflownodes.knime.nodes.io.outputfiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.knime.core.data.uri.IURIPortObject;
import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObjectSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.util.FileUtil;

import com.genericworkflownodes.util.MIMETypeHelper;

/**
 * This is the model implementation of OutputFiles Node.
 *
 * @author roettig, aiche
 */
public class OutputFilesNodeModel extends NodeModel {

    static final String CFG_FILENAME = "FILENAME";
    static final String CFG_OVERWRITE = "OVERWRITE";

    SettingsModelString m_filename = new SettingsModelString(
            OutputFilesNodeModel.CFG_FILENAME, "");

    SettingsModelBoolean m_overwrite = new SettingsModelBoolean(CFG_OVERWRITE, false);
    
    /**
     * Constructor for the node model.
     */
    protected OutputFilesNodeModel() {
        super(new PortType[] { PortTypeRegistry.getInstance().getPortType(IURIPortObject.class) },
                new PortType[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_filename.saveSettingsTo(settings);
        m_overwrite.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filename.loadSettingsFrom(settings);
        if (settings.containsKey(CFG_OVERWRITE)) {
            m_overwrite.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filename.validateSettings(settings);
        if (settings.containsKey(CFG_OVERWRITE)) {
            m_overwrite.validateSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        if (!(inSpecs[0] instanceof URIPortObjectSpec)) {
            throw new InvalidSettingsException(
                    "No URIPortObjectSpec compatible port object at the input port.");
        }

        // check the selected file
        if ("".equals(m_filename.getStringValue())) {
            throw new InvalidSettingsException(
                    "Please select a basename for the Output Files.");
        }
        
        //we do not need to check extension since the extension from the input
        // is taken over anyways

        return new PortObjectSpec[] {};
    }

    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
            throws Exception {
        IURIPortObject obj = (IURIPortObject) inObjects[0];
        List<URIContent> uris = obj.getURIContents();

        if (uris.size() == 0) {
            throw new Exception(
                    "There were no URIs in the supplied IURIPortObject");
        }

        List<File> outputs = new ArrayList<>();
        int idx = 1;
        int c = 0;
        for (int i = 0; i < uris.size(); i++) {
            String outfilename = insertIndex(m_filename.getStringValue(), obj
                    .getSpec().getFileExtensions().get(c), idx++);
            File out = FileUtil.getFileFromURL(FileUtil.toURL(outfilename));
            if (out.exists()) {
                if (!m_overwrite.getBooleanValue()) {
                    throw new InvalidSettingsException("File " + out.getAbsolutePath() + " exists and cannot be overwritten.");
                }
                if (!out.canWrite()) {
                    throw new Exception("Cannot write to file: "
                            + out.getAbsolutePath());
                } else if (!out.getParentFile().canWrite()) {
                    throw new Exception("Cannot write to containing directoy: "
                            + out.getParentFile().getAbsolutePath());
                }
            }
            outputs.add(out);
            c++;
        }
      
        idx = 0;
        for (URIContent uri : uris) {
            File in = FileUtil.getFileFromURL(uri.getURI().toURL());
            if (!in.canRead()) {
                throw new Exception("Cannot read file to export: "
                        + in.getAbsolutePath());
            }
            File out = outputs.get(idx++);
            FileUtils.copyFile(in, out);
        }
        return null;
    }

    private static String insertIndex(String filename, String extension, int idx) {
        if (filename.equals("") || filename.length() == 0) {
            return filename;
        }

        String filename_ = filename.toLowerCase();
        String ext = extension.toLowerCase();

        int idx1 = filename_.lastIndexOf(ext);

        if (idx == -1) {
            return filename;
        }

        String s1 = filename.substring(0, idx1);
        return s1 + idx + "." + extension;
    }
}
