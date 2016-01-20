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
package handlers.targethandlers.affectscope;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.l2junity.gameserver.GeoData;
import org.l2junity.gameserver.handler.AffectObjectHandler;
import org.l2junity.gameserver.handler.IAffectObjectHandler;
import org.l2junity.gameserver.handler.IAffectScopeHandler;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.skills.targets.AffectScope;
import org.l2junity.gameserver.util.Util;

/**
 * Square affect scope implementation (actually more like a rectangle).
 * @author Nik
 */
public class Square implements IAffectScopeHandler
{
	@Override
	public void forEachAffected(Creature activeChar, WorldObject target, Skill skill, Consumer<? super WorldObject> action)
	{
		final IAffectObjectHandler affectObject = AffectObjectHandler.getInstance().getHandler(skill.getAffectObject());
		final int squareStartAngle = skill.getFanRange()[1];
		final int squareLength = skill.getFanRange()[2];
		final int squareWidth = skill.getFanRange()[3];
		final int radius = (int) Math.sqrt((squareLength * squareLength) + (squareWidth * squareWidth));
		final int affectLimit = skill.getAffectLimit();
		
		final int rectX = activeChar.getX();
		final int rectY = activeChar.getY() - (squareWidth / 2);
		final double heading = Math.toRadians(squareStartAngle + Util.convertHeadingToDegree(activeChar.getHeading()));
		final double cos = Math.cos(-heading);
		final double sin = Math.sin(-heading);
		
		// Target checks.
		final AtomicInteger affected = new AtomicInteger(0);
		final Predicate<Creature> filter = c ->
		{
			if ((affectLimit > 0) && (affected.get() >= affectLimit))
			{
				return false;
			}
			if (c.isDead())
			{
				return false;
			}
			
			// Check if inside square.
			int xp = c.getX() - activeChar.getX();
			int yp = c.getY() - activeChar.getY();
			int xr = (int) ((activeChar.getX() + (xp * cos)) - (yp * sin));
			int yr = (int) (activeChar.getY() + (xp * sin) + (yp * cos));
			if ((xr > rectX) && (xr < (rectX + squareLength)) && (yr > rectY) && (yr < (rectY + squareWidth)))
			{
				if ((affectObject != null) && !affectObject.checkAffectedObject(activeChar, c))
				{
					return false;
				}
				if (!GeoData.getInstance().canSeeTarget(activeChar, c))
				{
					return false;
				}
				
				affected.incrementAndGet();
				return true;
			}
			
			return false;
		};
		
		// Add object of origin since its skipped in the forEachVisibleObjectInRange method.
		if (target.isCreature() && filter.test((Creature) target))
		{
			action.accept(target);
		}
		
		// Check and add targets.
		World.getInstance().forEachVisibleObjectInRange(target, Creature.class, radius, c ->
		{
			if (filter.test(c))
			{
				action.accept(c);
			}
		});
	}
	
	@Override
	public Enum<AffectScope> getAffectScopeType()
	{
		return AffectScope.SQUARE;
	}
}
