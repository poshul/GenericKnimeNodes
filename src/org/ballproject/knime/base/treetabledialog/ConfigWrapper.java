/*
 * Copyright (c) 2011, Marc Röttig.
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

package org.ballproject.knime.base.treetabledialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ballproject.knime.base.config.NodeConfiguration;
import org.ballproject.knime.base.parameter.Parameter;

public class ConfigWrapper 
{
	private NodeConfiguration config;
	
	public ConfigWrapper(NodeConfiguration config)
	{
		this.config = config;
		init();
	}
	
	private Node<Parameter<?>> root = new Node<Parameter<?>>(null,null,"root");
	
	public Node<Parameter<?>> getRoot()
	{
		return root;
	}
	
	private static List<String> getPrefixes(String key)
	{
		List<String> ret = new ArrayList<String>();
		String[] toks = key.split("\\.");
		String pref="";
		for(String tok: toks)
		{
			pref+=tok+".";
			ret.add(pref.substring(0,pref.length()-1));
		}
		return ret;
	}
	
	public static String getSuffix(String s)
	{
		String[] toks = s.split("\\.");
		return toks[toks.length-1];
	}
	
	private void init()
	{
		Map<String,Node<Parameter<?>>> key2node = new HashMap<String,Node<Parameter<?>>>();
		
		for(String key: config.getParameterKeys())
		{
			//System.out.println("key="+key);
			List<String> prefixes = getPrefixes(key);
			Node<Parameter<?>> last = root;
			for(int i=0;i<prefixes.size()-1;i++)
			{
				String prefix = prefixes.get(i);
				//System.out.println("\tprefix="+prefix);
				if(!key2node.containsKey(prefix))
				{
					//System.out.println("\t\tcreating internal node "+prefix);
					Node<Parameter<?>> nn = new Node<Parameter<?>>(last, null, getSuffix(prefix));
					last.addChild(nn);
					last = nn;
					key2node.put(prefix, last);
				}
				else
				{
					last = key2node.get(prefix);
				}
			}
			//System.out.println("adding param node to last="+last.getName());
			Parameter<?>       p = config.getParameter(key);
			Node<Parameter<?>> n = new Node<Parameter<?>>(last, p, p.getKey());
			last.addChild(n);
		}
	}
}
