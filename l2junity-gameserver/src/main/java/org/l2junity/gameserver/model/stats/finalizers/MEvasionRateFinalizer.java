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
package org.l2junity.gameserver.model.stats.finalizers;

import java.util.Optional;

import org.l2junity.Config;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.items.type.CrystalType;
import org.l2junity.gameserver.model.stats.IStatsFunction;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class MEvasionRateFinalizer implements IStatsFunction
{
	@Override
	public double calc(Creature creature, Optional<Double> base, Stats stat)
	{
		throwIfPresent(base);
		
		double baseValue = calcWeaponPlusBaseValue(creature, stat);
		final int level = creature.getLevel();
		if (creature.isPlayer())
		{
			// [Square(WIT)] * 3 + lvl;
			baseValue += (Math.sqrt(creature.getWIT()) * 3) + (level * 2);
			
			// Enchanted helm bonus
			final ItemInstance helmet = creature.getInventory().getPaperdollItemByL2ItemId(L2Item.SLOT_HEAD);
			if ((helmet != null) && helmet.isEnchanted() && (helmet.getItem().getCrystalTypePlus() == CrystalType.R))
			{
				baseValue += (0.2 * Math.max(helmet.getEnchantLevel() - 3, 0)) + (0.2 * Math.max(helmet.getEnchantLevel() - 6, 0));
			}
		}
		else
		{
			// [Square(DEX)] * 6 + lvl;
			baseValue += (Math.sqrt(creature.getWIT()) * 3) + (level * 2);
			if (level > 69)
			{
				baseValue += (level - 69) + 2;
			}
		}
		return validateValue(creature, Stats.defaultValue(creature, stat, baseValue), Config.MAX_EVASION);
	}
}
