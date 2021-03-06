/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.data.xml.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2junity.gameserver.data.xml.IGameXmlReader;
import org.l2junity.gameserver.model.base.ClassId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Holds all skill learn data for all npcs.
 * @author xban1x
 */
public final class SkillLearnData implements IGameXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SkillLearnData.class);
	
	private final Map<Integer, List<ClassId>> _skillLearn = new HashMap<>();
	
	protected SkillLearnData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_skillLearn.clear();
		parseDatapackFile("data/skillLearn.xml");
		LOGGER.info("Loaded {} Skill Learn data.", _skillLearn.size());
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equalsIgnoreCase(node.getNodeName()))
			{
				for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
				{
					if ("npc".equalsIgnoreCase(list_node.getNodeName()))
					{
						final List<ClassId> classIds = new ArrayList<>();
						for (Node c = list_node.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("classId".equalsIgnoreCase(c.getNodeName()))
							{
								classIds.add(ClassId.getClassId(Integer.parseInt(c.getTextContent())));
							}
						}
						_skillLearn.put(parseInteger(list_node.getAttributes(), "id"), classIds);
					}
				}
			}
		}
	}
	
	/**
	 * @param npcId
	 * @return {@link List} of {@link ClassId}'s that this npcId can teach.
	 */
	public List<ClassId> getSkillLearnData(int npcId)
	{
		return _skillLearn.get(npcId);
	}
	
	/**
	 * Gets the single instance of SkillLearnData.
	 * @return single instance of SkillLearnData
	 */
	public static SkillLearnData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillLearnData _instance = new SkillLearnData();
	}
}
