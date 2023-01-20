/**
 * Copyright (c) 2023, OpenMS Team.
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
package com.genericworkflownodes.knime.nodegeneration.model.meta;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import org.dom4j.DocumentException;

import com.genericworkflownodes.knime.nodegeneration.exceptions.DuplicateNodeNameException;
import com.genericworkflownodes.knime.nodegeneration.exceptions.InvalidNodeNameException;
import com.genericworkflownodes.knime.nodegeneration.model.directories.Directory.PathnameIsNoDirectoryException;
import com.genericworkflownodes.knime.nodegeneration.model.directories.FeatureSourceDirectory;
import com.genericworkflownodes.knime.nodegeneration.model.directories.UpdateSiteSourceDirectory;

/**
 * Meta information of the generated feature. The feature will bundle all
 * generated plugins, fragments, and additionally provided plugins.
 * 
 * @author aiche, bkahlert
 */
public class UpdateSiteMeta /* extends PluginMeta */ {
// does not extend PluginMeta since it does not have version and id
	
    // TODO categories
    
    public final ArrayList<FeatureMeta> featureMetas;
    private final String groupId;

    /**
     * Constructs the feature meta information given a singleton node source directory.
     * 
     * @param sourceDirectory
     * @throws DuplicateNodeNameException 
     * @throws InvalidNodeNameException 
     * @throws DocumentException 
     * @throws PathnameIsNoDirectoryException 
     */
    public UpdateSiteMeta(
    		UpdateSiteSourceDirectory sourceDirectory,
    		String nodeGeneratorLastChangeDate)
    				throws PathnameIsNoDirectoryException,
    					DocumentException,
    					InvalidNodeNameException,
    					DuplicateNodeNameException {
        try {
            groupId = sourceDirectory.getProperty("group_id", "knimeproject.update");
            featureMetas = new ArrayList<FeatureMeta>();
        	for (File dir : sourceDirectory.listFiles())
        	{
        		if (dir.isDirectory())
        		{
            		FeatureSourceDirectory fdir = new FeatureSourceDirectory(dir);
            		FeatureMeta fmeta = new FeatureMeta(fdir, nodeGeneratorLastChangeDate);
            		featureMetas.add(fmeta);
        		}

        	}
        } catch (IOException e) {
            throw new InvalidParameterException(
                    "Could not read meta information.\n" + e.getMessage());
        }
    }

    /**
     * Constructs an update site out of a single feature. Does not need a directory for that.
     * @param featureMeta
     */
	public UpdateSiteMeta(FeatureMeta featureMeta) {
		featureMetas = new ArrayList<FeatureMeta>();
		featureMetas.add(featureMeta);
		groupId = featureMeta.getGroupid();
	}

	public String getGroupId() {
		return groupId;
	}
}
