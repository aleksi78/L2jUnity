/*
 * Copyright (C) 2004-2016 L2J Unity
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
package org.l2junity.gameserver.instancemanager;

import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.holders.WarpedSpaceHolder;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.pathfinding.AbstractNodeLoc;
import org.l2junity.gameserver.util.Util;

/**
 * @author Sdw
 */
public class WarpedSpaceManager
{
	private volatile ConcurrentHashMap<Creature, WarpedSpaceHolder> _warpedSpace = null;
	
	public void addWarpedSpace(Creature creature, int radius)
	{
		if (_warpedSpace == null)
		{
			synchronized (this)
			{
				if (_warpedSpace == null)
				{
					_warpedSpace = new ConcurrentHashMap<>();
				}
			}
		}
		_warpedSpace.put(creature, new WarpedSpaceHolder(creature, radius));
	}
	
	public void removeWarpedSpace(Creature creature)
	{
		_warpedSpace.remove(creature);
	}
	
	public boolean checkForWarpedSpace(AbstractNodeLoc start, AbstractNodeLoc end, Instance instance)
	{
		return checkForWarpedSpace(new Location(start.getX(), start.getY(), start.getZ()), new Location(end.getX(), end.getY(), end.getZ()), instance);
	}
	
	public boolean checkForWarpedSpace(Location origin, Location destination, Instance instance)
	{
		if (_warpedSpace != null)
		{
			for (WarpedSpaceHolder holder : _warpedSpace.values())
			{
				final Creature creature = holder.getCreature();
				if (creature.getInstanceWorld() != instance)
				{
					continue;
				}
				int radius = creature.getTemplate().getCollisionRadius();
				final boolean originInRange = Util.calculateDistance(creature, origin, false, false) <= (holder.getRange() + radius);
				final boolean destinationInRange = Util.calculateDistance(creature, destination, false, false) <= (holder.getRange() + radius);
				return destinationInRange ? !originInRange : originInRange;
			}
		}
		return false;
	}
	
	public static WarpedSpaceManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final WarpedSpaceManager _instance = new WarpedSpaceManager();
	}
}
