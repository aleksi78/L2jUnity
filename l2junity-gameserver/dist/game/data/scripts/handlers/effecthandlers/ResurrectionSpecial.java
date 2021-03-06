/*
 * Copyright (C) 2004-2015 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.effects.EffectFlag;
import org.l2junity.gameserver.model.effects.L2EffectType;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * Resurrection Special effect implementation.
 * @author Zealar
 */
public final class ResurrectionSpecial extends AbstractEffect
{
	private final int _power;
	private final Set<Integer> _instanceId;
	
	public ResurrectionSpecial(StatsSet params)
	{
		_power = params.getInt("power", 0);
		
		final String instanceIds = params.getString("instanceId", null);
		if ((instanceIds != null) && !instanceIds.isEmpty())
		{
			_instanceId = new HashSet<>();
			for (String id : instanceIds.split(";"))
			{
				_instanceId.add(Integer.parseInt(id));
			}
		}
		else
		{
			_instanceId = Collections.<Integer> emptySet();
		}
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RESURRECTION_SPECIAL;
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.RESURRECTION_SPECIAL.getMask();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (!info.getEffected().isPlayer() && !info.getEffected().isPet())
		{
			return;
		}
		
		final PlayerInstance caster = info.getEffector().getActingPlayer();
		final Instance instance = caster.getInstanceWorld();
		if (!_instanceId.isEmpty() && ((instance == null) || !_instanceId.contains(instance.getTemplateId())))
		{
			return;
		}
		
		final Skill skill = info.getSkill();
		
		if (info.getEffected().isPlayer())
		{
			info.getEffected().getActingPlayer().reviveRequest(caster, skill, false, _power);
		}
		else if (info.getEffected().isPet())
		{
			final L2PetInstance pet = (L2PetInstance) info.getEffected();
			info.getEffected().getActingPlayer().reviveRequest(pet.getActingPlayer(), skill, true, _power);
		}
	}
}