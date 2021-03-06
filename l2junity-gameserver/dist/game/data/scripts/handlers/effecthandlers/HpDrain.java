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

import org.l2junity.gameserver.enums.ShotType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.effects.L2EffectType;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.Formulas;

/**
 * HP Drain effect implementation.
 * @author Adry_85
 */
public final class HpDrain extends AbstractEffect
{
	private final double _power;
	private final double _percentage;
	
	public HpDrain(StatsSet params)
	{
		_power = params.getDouble("power", 0);
		_percentage = params.getDouble("percentage", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HP_DRAIN;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		// TODO: Unhardcode Cubic Skill to avoid double damage
		if (effector.isAlikeDead() || (skill.getId() == 4050))
		{
			return;
		}
		
		boolean sps = skill.useSpiritShot() && effector.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = skill.useSpiritShot() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		boolean mcrit = Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill);
		double damage = Formulas.calcMagicDam(effector, effected, skill, effector.getMAtk(), _power, effected.getMDef(), sps, bss, mcrit);
		
		double drain = 0;
		int cp = (int) effected.getCurrentCp();
		int hp = (int) effected.getCurrentHp();
		
		if (cp > 0)
		{
			drain = (damage < cp) ? 0 : (damage - cp);
		}
		else if (damage > hp)
		{
			drain = hp;
		}
		else
		{
			drain = damage;
		}
		
		final double hpAdd = ((_percentage / 100) * drain);
		final double hpFinal = ((effector.getCurrentHp() + hpAdd) > effector.getMaxHp() ? effector.getMaxHp() : (effector.getCurrentHp() + hpAdd));
		effector.setCurrentHp(hpFinal);
		
		if (damage > 0)
		{
			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!effected.isRaid() && Formulas.calcAtkBreak(effected, damage))
			{
				effected.breakAttack();
				effected.breakCast();
			}
			effected.reduceCurrentHp(damage, effector, skill, false, false, mcrit, false);
			effector.sendDamageMessage(effected, skill, (int) damage, mcrit, false);
		}
	}
}