/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.model.instancezone;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.l2junity.Config;
import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.enums.GroupType;
import org.l2junity.gameserver.enums.InstanceReenterType;
import org.l2junity.gameserver.enums.InstanceRemoveBuffType;
import org.l2junity.gameserver.enums.InstanceTeleportType;
import org.l2junity.gameserver.instancemanager.InstanceManager;
import org.l2junity.gameserver.model.AbstractPlayerGroup;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.Playable;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.ListenersContainer;
import org.l2junity.gameserver.model.holders.InstanceReenterTimeHolder;
import org.l2junity.gameserver.model.holders.SpawnHolder;
import org.l2junity.gameserver.model.instancezone.conditions.Condition;
import org.l2junity.gameserver.model.instancezone.conditions.ConditionCommandChannel;
import org.l2junity.gameserver.model.instancezone.conditions.ConditionGroupMax;
import org.l2junity.gameserver.model.instancezone.conditions.ConditionGroupMin;
import org.l2junity.gameserver.model.interfaces.IIdentifiable;
import org.l2junity.gameserver.model.interfaces.INamable;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.variables.PlayerVariables;

/**
 * Template holder for instances.
 * @author malyelfik
 */
public class InstanceTemplate extends ListenersContainer implements IIdentifiable, INamable
{
	// Basic instance parameters
	private int _templateId = -1;
	private String _name = "UnknownInstance";
	private int _duration = -1;
	private long _emptyDestroyTime = -1;
	private int _ejectTime = Config.EJECT_DEAD_PLAYER_TIME;
	private int _maxWorldCount = -1;
	private boolean _isPvP = false;
	private boolean _allowPlayerSummon = false;
	private StatsSet _parameters = StatsSet.EMPTY_STATSET;
	private final Map<Integer, Boolean> _doors = new HashMap<>();
	private final Map<String, List<SpawnHolder>> _spawns = new HashMap<>();
	// Locations
	private InstanceTeleportType _enterLocationType = InstanceTeleportType.NONE;
	private List<Location> _enterLocations = null;
	private InstanceTeleportType _exitLocationType = InstanceTeleportType.NONE;
	private List<Location> _exitLocations = null;
	// Reenter data
	private InstanceReenterType _reenterType = InstanceReenterType.NONE;
	private List<InstanceReenterTimeHolder> _reenterData = Collections.emptyList();
	// Buff remove data
	private InstanceRemoveBuffType _removeBuffType = InstanceRemoveBuffType.NONE;
	private List<Integer> _removeBuffExceptions = Collections.emptyList();
	// Conditions
	private List<Condition> _conditions = Collections.emptyList();
	private int _groupMask = GroupType.NONE.getMask();
	
	// -------------------------------------------------------------
	// Setters
	// -------------------------------------------------------------
	/**
	 * Set template ID of instance world.
	 * @param id instance template id
	 */
	public void setId(int id)
	{
		_templateId = id;
	}
	
	/**
	 * Set name of instance world.
	 * @param name instance name
	 */
	public void setName(String name)
	{
		if ((name != null) && !name.isEmpty())
		{
			_name = name;
		}
	}
	
	/**
	 * Set instance world duration.
	 * @param duration time in minutes
	 */
	public void setDuration(int duration)
	{
		if (duration > 0)
		{
			_duration = duration;
		}
	}
	
	/**
	 * Set time after empty instance will be destroyed.
	 * @param emptyDestroyTime time in minutes
	 */
	public void setEmptyDestroyTime(long emptyDestroyTime)
	{
		if (emptyDestroyTime >= 0)
		{
			_emptyDestroyTime = TimeUnit.MINUTES.toMillis(emptyDestroyTime);
		}
	}
	
	/**
	 * Set time after death player will be ejected from instance world.<br>
	 * Default: {@link Config#EJECT_DEAD_PLAYER_TIME}
	 * @param ejectTime time in minutes
	 */
	public void setEjectTime(int ejectTime)
	{
		if (ejectTime >= 0)
		{
			_ejectTime = ejectTime;
		}
	}
	
	/**
	 * Allow summoning players (that are out of instance) to instance world by players inside.
	 * @param val {@code true} means summon is allowed, {@code false} means summon is prohibited
	 */
	public void allowPlayerSummon(boolean val)
	{
		_allowPlayerSummon = val;
	}
	
	/**
	 * Set instance as PvP world.
	 * @param val {@code true} world is PvP zone, {@code false} world use classic zones
	 */
	public void setIsPvP(boolean val)
	{
		_isPvP = val;
	}
	
	/**
	 * Set parameters shared between instances with same template id.
	 * @param set map containing parameters
	 */
	public void setParameters(Map<String, Object> set)
	{
		if (!set.isEmpty())
		{
			_parameters = new StatsSet(Collections.unmodifiableMap(set));
		}
	}
	
	/**
	 * Add door into instance world.
	 * @param templateId template id of door
	 * @param open open status of door, meaning of each possible value:
	 *            <ul>
	 *            <li><b>null</b> - default value from door template</li>
	 *            <li><b>true</b> - opened</li>
	 *            <li><b>false</b> - closed</li>
	 *            </ul>
	 */
	public void addDoor(int templateId, Boolean open)
	{
		_doors.put(templateId, open);
	}
	
	/**
	 * Add new group of NPC spawns into instance world.<br>
	 * Group with name "general" will be spawned on instance world create.
	 * @param name name of group
	 * @param spawns list of NPC spawn data
	 */
	public void addSpawnGroup(String name, List<SpawnHolder> spawns)
	{
		_spawns.put(name.toLowerCase(), spawns);
	}
	
	/**
	 * Set enter locations for instance world.
	 * @param type type of teleport ({@link InstanceTeleportType#FIXED} or {@link InstanceTeleportType#RANDOM} are supported)
	 * @param locations list of locations used for determining final enter location
	 */
	public void setEnterLocation(InstanceTeleportType type, List<Location> locations)
	{
		_enterLocationType = type;
		_enterLocations = locations;
	}
	
	/**
	 * Set exit locations for instance world.
	 * @param type type of teleport (see {@link InstanceTeleportType} for all possible types)
	 * @param locations list of locations used for determining final exit location
	 */
	public void setExitLocation(InstanceTeleportType type, List<Location> locations)
	{
		_exitLocationType = type;
		_exitLocations = locations;
	}
	
	/**
	 * Set re-enter data for instance world.<br>
	 * This method also enable re-enter condition for instance world.
	 * @param type reenter type means when reenter restriction should be applied (see {@link InstanceReenterType} for more info)
	 * @param holder data which are used to calculate reenter time
	 */
	public void setReenterData(InstanceReenterType type, List<InstanceReenterTimeHolder> holder)
	{
		_reenterType = type;
		_reenterData = holder;
	}
	
	/**
	 * Set remove buff list for instance world.<br>
	 * These data are used to restrict player buffs when he enters into instance.
	 * @param type type of list like blacklist, whitelist, ... (see {@link InstanceRemoveBuffType} for more info)
	 * @param exceptionList
	 */
	public void setRemoveBuff(InstanceRemoveBuffType type, List<Integer> exceptionList)
	{
		_removeBuffType = type;
		_removeBuffExceptions = exceptionList;
	}
	
	/**
	 * Set count of worlds which can run concurrently (with same template id).
	 * @param val count
	 */
	public void setMaxWorlds(int val)
	{
		_maxWorldCount = val;
	}
	
	/**
	 * Register conditions to instance world.<br>
	 * This method also set new enter group mask according to given conditions.
	 * @param conditions list of conditions
	 */
	public void setConditions(List<Condition> conditions)
	{
		// Set conditions
		_conditions = conditions;
		
		// Now iterate over conditions and determine enter group data
		boolean onlyCC = false;
		int min = 1, max = 1;
		for (Condition cond : _conditions)
		{
			if (cond instanceof ConditionCommandChannel)
			{
				onlyCC = true;
			}
			else if (cond instanceof ConditionGroupMin)
			{
				min = ((ConditionGroupMin) cond).getLimit();
			}
			else if (cond instanceof ConditionGroupMax)
			{
				max = ((ConditionGroupMax) cond).getLimit();
			}
		}
		
		// Reset group mask before setting new group
		_groupMask = 0;
		// Check if player can enter in other group then Command channel
		if (!onlyCC)
		{
			// Player
			if (min == 1)
			{
				_groupMask |= GroupType.NONE.getMask();
			}
			// Party
			final int partySize = Config.ALT_PARTY_MAX_MEMBERS;
			if (((max > 1) && (max <= partySize)) || ((min <= partySize) && (max > partySize)))
			{
				_groupMask |= GroupType.PARTY.getMask();
			}
		}
		// Command channel
		if (onlyCC || (max > 7))
		{
			_groupMask |= GroupType.COMMAND_CHANNEL.getMask();
		}
	}
	
	// -------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------
	@Override
	public int getId()
	{
		return _templateId;
	}
	
	@Override
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Get all enter locations defined in XML template.
	 * @return list of enter locations
	 */
	public List<Location> getEnterLocations()
	{
		return _enterLocations;
	}
	
	/**
	 * Get enter location to instance world.
	 * @return enter location if instance has any, otherwise {@code null}
	 */
	public Location getEnterLocation()
	{
		Location loc = null;
		switch (_enterLocationType)
		{
			case RANDOM:
				loc = _enterLocations.get(Rnd.get(_enterLocations.size()));
				break;
			case FIXED:
				loc = _enterLocations.get(0);
				break;
		}
		return loc;
	}
	
	/**
	 * Get type of exit location.
	 * @return exit location type (see {@link InstanceTeleportType} for possible values)
	 */
	public InstanceTeleportType getExitLocationType()
	{
		return _exitLocationType;
	}
	
	/**
	 * Get exit location from instance world.
	 * @param player player who wants to leave instance
	 * @return exit location if instance has any, otherwise {@code null}
	 */
	public Location getExitLocation(PlayerInstance player)
	{
		Location location = null;
		switch (_exitLocationType)
		{
			case RANDOM:
				location = _exitLocations.get(Rnd.get(_exitLocations.size()));
				break;
			case FIXED:
				location = _exitLocations.get(0);
				break;
			case ORIGIN:
			{
				final PlayerVariables vars = player.getVariables();
				final int[] loc = vars.getIntArray("INSTANCE_ORIGIN", ";");
				if (loc.length == 3)
				{
					location = new Location(loc[0], loc[1], loc[2]);
					vars.remove("INSTANCE_ORIGIN");
				}
				break;
			}
		}
		return location;
	}
	
	/**
	 * Get time after empty instance is destroyed.
	 * @return time in milliseconds
	 */
	public long getEmptyDestroyTime()
	{
		return _emptyDestroyTime;
	}
	
	/**
	 * Get instance duration time.
	 * @return time in minutes
	 */
	public int getDuration()
	{
		return _duration;
	}
	
	/**
	 * Get time after dead player is ejected from instance world.
	 * @return time in minutes
	 */
	public int getEjectTime()
	{
		return _ejectTime;
	}
	
	/**
	 * Check if summoning player into instance is allowed.
	 * @return {@code true} if summon is allowed, otherwise {@code false}
	 */
	public boolean isPlayerSummonAllowed()
	{
		return _allowPlayerSummon;
	}
	
	/**
	 * Check if instance is PvP zone.
	 * @return {@code true} if instance is PvP, otherwise {@code false}
	 */
	public boolean isPvP()
	{
		return _isPvP;
	}
	
	/**
	 * Get doors data for instance world.
	 * @return map in form <i>doorId, openStatus</i>, where <i>openStatus</i> value means:
	 *         <ul>
	 *         <li><b>null</b> - default value from door template</li>
	 *         <li><b>true</b> - opened</li>
	 *         <li><b>false</b> - closed</li>
	 *         </ul>
	 */
	public Map<Integer, Boolean> getDoors()
	{
		return _doors;
	}
	
	/**
	 * Get spawn group by group name.
	 * @param name name of group
	 * @return list which contains spawn data from spawn group
	 */
	public List<SpawnHolder> getSpawnGroup(String name)
	{
		return _spawns.get(name.toLowerCase());
	}
	
	/**
	 * Get count of instance worlds which can run concurrently with same template ID.
	 * @return count of worlds
	 */
	public int getMaxWorlds()
	{
		return _maxWorldCount;
	}
	
	/**
	 * Get instance template parameters.
	 * @return parameters of template
	 */
	public StatsSet getParameters()
	{
		return _parameters;
	}
	
	/**
	 * Check if buffs are removed upon instance enter.
	 * @return {@code true} if any buffs should be removed, otherwise {@code false}
	 */
	public boolean isRemoveBuffEnabled()
	{
		return _removeBuffType != InstanceRemoveBuffType.NONE;
	}
	
	/**
	 * Remove buffs from player according to remove buff data
	 * @param player player which loose buffs
	 */
	public void removePlayerBuff(PlayerInstance player)
	{
		// Make list of affected playable objects
		final List<Playable> affected = new ArrayList<>();
		affected.add(player);
		player.getServitors().values().forEach(affected::add);
		if (player.hasPet())
		{
			affected.add(player.getPet());
		}
		
		// Now remove buffs by type
		if (_removeBuffType.equals(InstanceRemoveBuffType.ALL))
		{
			for (Playable playable : affected)
			{
				playable.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}
		else
		{
			for (Playable playable : affected)
			{
				for (BuffInfo info : playable.getEffectList().getBuffs())
				{
					if (hasRemoveBuffException(info.getSkill()))
					{
						playable.stopSkillEffects(info.getSkill());
					}
				}
			}
		}
	}
	
	/**
	 * Check if given buff {@code skill} should be removed.
	 * @param skill buff which should be removed
	 * @return {@code true} if buff will be removed, otherwise {@code false}
	 */
	private boolean hasRemoveBuffException(Skill skill)
	{
		final boolean containsSkill = _removeBuffExceptions.contains(skill.getId());
		return (_removeBuffType == InstanceRemoveBuffType.BLACKLIST) ? containsSkill : !containsSkill;
	}
	
	/**
	 * Get type of re-enter data.
	 * @return type of re-enter (see {@link InstanceReenterType} for possible values)
	 */
	public InstanceReenterType getReenterType()
	{
		return _reenterType;
	}
	
	/**
	 * Calculate re-enter time for instance world.
	 * @return re-enter time in milliseconds
	 */
	public long calculateReenterTime()
	{
		long time = -1;
		for (InstanceReenterTimeHolder data : _reenterData)
		{
			if (data.getTime() > 0)
			{
				time = System.currentTimeMillis() + data.getTime();
				break;
			}
			
			final Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR, data.getHour());
			calendar.set(Calendar.MINUTE, data.getMinute());
			calendar.set(Calendar.SECOND, 0);
			
			// If calendar time is lower than current, add one more day
			if (calendar.getTimeInMillis() <= System.currentTimeMillis())
			{
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			// Modify calendar day
			if (data.getDay() != null)
			{
				// DayOfWeek starts with Monday(1) but Calendar starts with Sunday(1)
				int day = data.getDay().getValue() + 1;
				if (day > 7)
				{
					day = 1;
				}
				
				// Set exact day. If modified date is before current, add one more week.
				calendar.set(Calendar.DAY_OF_WEEK, day);
				if (calendar.getTimeInMillis() <= System.currentTimeMillis())
				{
					calendar.add(Calendar.WEEK_OF_MONTH, 1);
				}
			}
			
			if ((time == -1) || (calendar.getTimeInMillis() < time))
			{
				time = calendar.getTimeInMillis();
			}
		}
		return time;
	}
	
	/**
	 * Check if enter group mask contains given group type {@code type}.
	 * @param type type of group
	 * @return {@code true} if mask contains given group, otherwise {@code false}
	 */
	private final boolean groupMaskContains(GroupType type)
	{
		final int flag = type.getMask();
		return (_groupMask & flag) == flag;
	}
	
	/**
	 * Get enter group which can enter into instance world based on player's group.
	 * @param player player who wants to enter
	 * @return group type which can enter if any can enter, otherwise {@code null}
	 */
	private final GroupType getEnterGroupType(PlayerInstance player)
	{
		// If mask doesn't contain any group
		if (_groupMask == 0)
		{
			return null;
		}
		
		// If player can override instance conditions then he can enter alone
		if (player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return GroupType.NONE;
		}
		
		// Check if mask contains player's group
		final GroupType playerGroup = player.getGroupType();
		if (groupMaskContains(playerGroup))
		{
			return playerGroup;
		}
		
		// Check if mask contains only one group
		final GroupType type = GroupType.getByMask(_groupMask);
		if (type != null)
		{
			return type;
		}
		
		// When mask contains more group types but without player's group, choose nearest one
		// player < party < command channel
		for (GroupType t : GroupType.values())
		{
			if (!t.equals(playerGroup) && groupMaskContains(t))
			{
				return t;
			}
		}
		// nothing found? then player cannot enter
		return null;
	}
	
	/**
	 * Get player's group based on result of {@link InstanceTemplate#getEnterGroupType(PlayerInstance)}.
	 * @param player player who wants to enter into instance
	 * @return list of players (first player in list is player who make enter request)
	 */
	public List<PlayerInstance> getEnterGroup(PlayerInstance player)
	{
		final GroupType type = getEnterGroupType(player);
		if (type == null)
		{
			return null;
		}
		
		// Make list of players which can enter into instance world
		final List<PlayerInstance> group = new ArrayList<>();
		group.add(player); // Put player who made request at first position inside list
		
		// Check if player has group in which he can enter
		AbstractPlayerGroup pGroup = null;
		if (type.equals(GroupType.PARTY))
		{
			pGroup = player.getParty();
		}
		else if (type.equals(GroupType.COMMAND_CHANNEL))
		{
			pGroup = player.getCommandChannel();
		}
		
		// If any group found then put them into enter group list
		if (pGroup != null)
		{
			pGroup.getMembers().stream().filter(p -> !p.equals(player)).forEach(group::add);
		}
		return group;
	}
	
	/**
	 * Validate instance conditions for given group.
	 * @param group group of players which want to enter instance world
	 * @param npc instance of NPC used to enter to instance
	 * @param htmlCallback callback function used to display fail HTML when condition validate failed
	 * @return {@code true} when all condition are met, otherwise {@code false}
	 */
	public boolean validateConditions(List<PlayerInstance> group, Npc npc, BiConsumer<PlayerInstance, String> htmlCallback)
	{
		for (Condition cond : _conditions)
		{
			if (!cond.validate(npc, group, htmlCallback))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Apply condition effects for each player from enter group.
	 * @param group players from enter group
	 */
	public void applyConditionEffects(List<PlayerInstance> group)
	{
		_conditions.forEach(c -> c.applyEffect(group));
	}
	
	/**
	 * Get count of created instance worlds.
	 * @return count of created instances
	 */
	public long getWorldCount()
	{
		return InstanceManager.getInstance().getWorldCount(getId());
	}
	
	@Override
	public String toString()
	{
		return "ID: " + getId() + " Name: " + getName();
	}
}