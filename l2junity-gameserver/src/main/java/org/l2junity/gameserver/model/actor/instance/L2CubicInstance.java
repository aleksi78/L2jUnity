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
package org.l2junity.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.l2junity.Config;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.ai.CtrlEvent;
import org.l2junity.gameserver.datatables.SkillData;
import org.l2junity.gameserver.instancemanager.DuelManager;
import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.tasks.cubics.CubicAction;
import org.l2junity.gameserver.model.actor.tasks.cubics.CubicBuff;
import org.l2junity.gameserver.model.actor.tasks.cubics.CubicDisappear;
import org.l2junity.gameserver.model.actor.tasks.cubics.CubicHeal;
import org.l2junity.gameserver.model.effects.L2EffectType;
import org.l2junity.gameserver.model.interfaces.IIdentifiable;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.Formulas;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class L2CubicInstance implements IIdentifiable
{
	private static final Logger _log = LoggerFactory.getLogger(L2CubicInstance.class);
	
	// Type of Cubics
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
	public static final int SMART_CUBIC_ARCANALORD = 12;
	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;
	public static final int CUBIC_AVENGE = 15;
	public static final int CUBIC_KNIGHT = 16;
	public static final int CUBIC_HEALER = 17;
	public static final int CUBIC_POEM = 18;
	public static final int CUBIC_MENTAL = 19;
	public static final int CUBIC_SPIRIT = 20;
	public static final int CUBIC_WHAMMY = 21;
	
	// Max range of cubic skills
	// TODO: Check/fix the max range
	public static final int MAX_MAGIC_RANGE = 900;
	
	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	public static final int SKILL_CUBIC_KNIGHT = 10056;
	public static final int SKILL_CUBIC_HEALER = 11807;
	public static final int SKILL_POEM_CUBIC_HEAL = 10083;
	public static final int SKILL_MENTAL_CUBIC_RECHARGE = 10084;
	public static final int SKILL_POEM_CUBIC_GREAT_HEAL = 10082;
	public static final int SKILL_MENTAL_CUBIC_GREAT_RECHARGE = 10089;
	
	private final PlayerInstance _owner;
	private Creature _target;
	
	private final int _cubicId;
	private final int _cubicPower;
	private final int _cubicDelay;
	private final int _cubicSkillChance;
	private final int _cubicMaxCount;
	private boolean _active;
	private final boolean _givenByOther;
	
	private final List<Skill> _skills = new ArrayList<>();
	
	private Future<?> _disappearTask;
	private Future<?> _actionTask;
	
	public L2CubicInstance(PlayerInstance owner, int cubicId, int level, int cubicPower, int cubicDelay, int cubicSkillChance, int cubicMaxCount, int cubicDuration, boolean givenByOther)
	{
		_owner = owner;
		_cubicId = cubicId;
		_cubicPower = cubicPower;
		_cubicDelay = cubicDelay * 1000;
		_cubicSkillChance = cubicSkillChance;
		_cubicMaxCount = cubicMaxCount;
		_active = false;
		_givenByOther = givenByOther;
		
		switch (_cubicId)
		{
			case STORM_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4050, level));
				break;
			case LIFE_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4051, level));
				doAction();
				break;
			case VIPER_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4052, level));
				break;
			case POLTERGEIST_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4053, level));
				_skills.add(SkillData.getInstance().getSkill(4054, level));
				_skills.add(SkillData.getInstance().getSkill(4055, level));
				break;
			case BINDING_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4164, level));
				break;
			case AQUA_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4165, level));
				break;
			case SPARK_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4166, level));
				break;
			case ATTRACT_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(5115, level));
				_skills.add(SkillData.getInstance().getSkill(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				_skills.add(SkillData.getInstance().getSkill(4051, 7));
				_skills.add(SkillData.getInstance().getSkill(4165, 9));
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				_skills.add(SkillData.getInstance().getSkill(4049, 8));
				_skills.add(SkillData.getInstance().getSkill(4166, 9));
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				_skills.add(SkillData.getInstance().getSkill(4049, 8));
				_skills.add(SkillData.getInstance().getSkill(4052, 6));
				break;
			case SMART_CUBIC_EVATEMPLAR:
				_skills.add(SkillData.getInstance().getSkill(4053, 8));
				_skills.add(SkillData.getInstance().getSkill(4165, 9));
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_skills.add(SkillData.getInstance().getSkill(4049, 8));
				_skills.add(SkillData.getInstance().getSkill(5115, 4));
				break;
			case CUBIC_AVENGE:
				_skills.add(SkillData.getInstance().getSkill(11292, level));
				_skills.add(SkillData.getInstance().getSkill(11293, level));
				_skills.add(SkillData.getInstance().getSkill(11294, level));
				break;
			case CUBIC_KNIGHT:
				_skills.add(SkillData.getInstance().getSkill(10056, level));
				doAction();
				break;
			case CUBIC_HEALER:
				_skills.add(SkillData.getInstance().getSkill(11807, level));
				doAction();
				break;
			case CUBIC_POEM:
				_skills.add(SkillData.getInstance().getSkill(10083, level));
				_skills.add(SkillData.getInstance().getSkill(10082, level));
				doAction();
				break;
			case CUBIC_MENTAL:
				_skills.add(SkillData.getInstance().getSkill(10084, level));
				_skills.add(SkillData.getInstance().getSkill(10089, level));
				doAction();
				break;
			case CUBIC_SPIRIT:
				_skills.add(SkillData.getInstance().getSkill(10085, level));
				break;
			case CUBIC_WHAMMY:
				_skills.add(SkillData.getInstance().getSkill(10086, level));
				break;
		}
		_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubicDisappear(this), cubicDuration * 1000); // disappear
	}
	
	public synchronized void doAction()
	{
		if (_active)
		{
			return;
		}
		_active = true;
		
		switch (_cubicId)
		{
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
			case SMART_CUBIC_ARCANALORD:
			case SMART_CUBIC_ELEMENTALMASTER:
			case SMART_CUBIC_SPECTRALMASTER:
			case SMART_CUBIC_EVATEMPLAR:
			case SMART_CUBIC_SHILLIENTEMPLAR:
			case CUBIC_AVENGE:
			case CUBIC_SPIRIT:
			case CUBIC_WHAMMY:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CubicAction(this, _cubicSkillChance), 0, _cubicDelay);
				break;
			case LIFE_CUBIC:
			case CUBIC_HEALER:
			case CUBIC_POEM:
			case CUBIC_MENTAL:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CubicHeal(this), 0, _cubicDelay);
				break;
			case CUBIC_KNIGHT:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CubicBuff(this, 10), 0, _cubicDelay); // TODO: Custom chance here
				break;
		}
	}
	
	@Override
	public int getId()
	{
		return _cubicId;
	}
	
	public PlayerInstance getOwner()
	{
		return _owner;
	}
	
	public int getCubicPower()
	{
		return _cubicPower;
	}
	
	public Creature getTarget()
	{
		return _target;
	}
	
	public void setTarget(Creature target)
	{
		_target = target;
	}
	
	public List<Skill> getSkills()
	{
		return _skills;
	}
	
	public int getCubicMaxCount()
	{
		return _cubicMaxCount;
	}
	
	public void stopAction()
	{
		_target = null;
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}
		_active = false;
	}
	
	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}
	
	/** this sets the enemy target for a cubic */
	public void getCubicTarget()
	{
		try
		{
			_target = null;
			WorldObject ownerTarget = _owner.getTarget();
			if (ownerTarget == null)
			{
				return;
			}
			
			// Duel targeting
			if (_owner.isInDuel())
			{
				PlayerInstance PlayerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
				PlayerInstance PlayerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();
				
				if (DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
				{
					Party partyA = PlayerA.getParty();
					Party partyB = PlayerB.getParty();
					Party partyEnemy = null;
					
					if (partyA != null)
					{
						if (partyA.getMembers().contains(_owner))
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							partyEnemy = partyA;
						}
					}
					else
					{
						if (PlayerA == _owner)
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							_target = PlayerA;
						}
					}
					if ((_target == PlayerA) || (_target == PlayerB))
					{
						if (_target == ownerTarget)
						{
							return;
						}
					}
					if (partyEnemy != null)
					{
						if (partyEnemy.getMembers().contains(ownerTarget))
						{
							_target = (Creature) ownerTarget;
						}
						return;
					}
				}
				if ((PlayerA != _owner) && (ownerTarget == PlayerA))
				{
					_target = PlayerA;
					return;
				}
				if ((PlayerB != _owner) && (ownerTarget == PlayerB))
				{
					_target = PlayerB;
					return;
				}
				_target = null;
				return;
			}
			// Olympiad targeting
			if (_owner.isInOlympiadMode())
			{
				if (_owner.isOlympiadStart())
				{
					if (ownerTarget.isPlayable())
					{
						final PlayerInstance targetPlayer = ownerTarget.getActingPlayer();
						if ((targetPlayer != null) && (targetPlayer.getOlympiadGameId() == _owner.getOlympiadGameId()) && (targetPlayer.getOlympiadSide() != _owner.getOlympiadSide()))
						{
							_target = (Creature) ownerTarget;
						}
					}
				}
				return;
			}
			// test owners target if it is valid then use it
			final Summon pet = _owner.getPet();
			if (ownerTarget.isCreature() && (ownerTarget != pet) && !_owner.hasServitor(ownerTarget.getObjectId()) && (ownerTarget != _owner))
			{
				// target mob which has aggro on you or your summon
				if (ownerTarget.isAttackable())
				{
					final Attackable attackable = (Attackable) ownerTarget;
					if ((attackable.getAggroList().get(_owner) != null) && !attackable.isDead())
					{
						_target = (Creature) ownerTarget;
						return;
					}
					if (_owner.hasSummon())
					{
						if ((attackable.getAggroList().get(pet) != null) && !attackable.isDead())
						{
							_target = (Creature) ownerTarget;
							return;
						}
						for (Summon servitor : _owner.getServitors().values())
						{
							if ((attackable.getAggroList().get(servitor) != null) && !attackable.isDead())
							{
								_target = (Creature) ownerTarget;
								return;
							}
						}
					}
				}
				
				// get target in pvp or in siege
				PlayerInstance enemy = null;
				
				if (((_owner.getPvpFlag() > 0) && !_owner.isInsideZone(ZoneId.PEACE)) || _owner.isInsideZone(ZoneId.PVP))
				{
					if (!((Creature) ownerTarget).isDead())
					{
						enemy = ownerTarget.getActingPlayer();
					}
					
					if (enemy != null)
					{
						boolean targetIt = true;
						
						if (_owner.getParty() != null)
						{
							if (_owner.getParty().getMembers().contains(enemy))
							{
								targetIt = false;
							}
							else if (_owner.getParty().getCommandChannel() != null)
							{
								if (_owner.getParty().getCommandChannel().getMembers().contains(enemy))
								{
									targetIt = false;
								}
							}
						}
						if ((_owner.getClan() != null) && !_owner.isInsideZone(ZoneId.PVP))
						{
							if (_owner.getClan().isMember(enemy.getObjectId()))
							{
								targetIt = false;
							}
							if ((_owner.getAllyId() > 0) && (enemy.getAllyId() > 0))
							{
								if (_owner.getAllyId() == enemy.getAllyId())
								{
									targetIt = false;
								}
							}
						}
						if ((enemy.getPvpFlag() == 0) && !enemy.isInsideZone(ZoneId.PVP))
						{
							targetIt = false;
						}
						if (enemy.isInsideZone(ZoneId.PEACE))
						{
							targetIt = false;
						}
						if ((_owner.getSiegeState() > 0) && (_owner.getSiegeState() == enemy.getSiegeState()))
						{
							targetIt = false;
						}
						if (!enemy.isVisible())
						{
							targetIt = false;
						}
						
						if (targetIt)
						{
							_target = enemy;
							return;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
	}
	
	public void useCubicContinuous(Skill skill, WorldObject[] targets)
	{
		for (Creature target : (Creature[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			if (skill.isBad())
			{
				byte shld = Formulas.calcShldUse(_owner, target, skill);
				boolean acted = Formulas.calcCubicSkillSuccess(this, target, skill, shld);
				if (!acted)
				{
					_owner.sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
					continue;
				}
				
			}
			
			// Apply effects
			skill.applyEffects(_owner, target, false, false, true, 0);
			
			// If this is a bad skill notify the duel manager, so it can be removed after the duel (player & target must be in the same duel).
			if (target.isPlayer() && target.getActingPlayer().isInDuel() && skill.isBad() && (_owner.getDuelId() == target.getActingPlayer().getDuelId()))
			{
				DuelManager.getInstance().onBuff(target.getActingPlayer(), skill);
			}
		}
	}
	
	/**
	 * @param activeCubic
	 * @param skill
	 * @param targets
	 */
	public void useCubicMdam(L2CubicInstance activeCubic, Skill skill, WorldObject[] targets)
	{
		for (Creature target : (Creature[]) targets)
		{
			if (target == null)
			{
				continue;
			}
			
			if (target.isAlikeDead())
			{
				if (target.isPlayer())
				{
					target.stopFakeDeath(true);
				}
				else
				{
					continue;
				}
			}
			
			skill.applyEffects(_owner, target);
		}
	}
	
	public void useCubicDrain(L2CubicInstance activeCubic, Skill skill, WorldObject[] targets)
	{
		if (Config.DEBUG)
		{
			_log.info("L2SkillDrain: useCubicSkill()");
		}
		
		for (Creature target : (Creature[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}
			
			skill.applyEffects(_owner, target);
		}
	}
	
	public void useCubicDisabler(Skill skill, WorldObject[] targets)
	{
		if (Config.DEBUG)
		{
			_log.info("Disablers: useCubicSkill()");
		}
		
		for (Creature target : (Creature[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			byte shld = Formulas.calcShldUse(_owner, target, skill);
			
			if (skill.hasEffectType(L2EffectType.STUN, L2EffectType.PARALYZE, L2EffectType.ROOT))
			{
				if (Formulas.calcCubicSkillSuccess(this, target, skill, shld))
				{
					// Apply effects
					skill.applyEffects(_owner, target, false, false, true, 0);
					
					// If this is a bad skill notify the duel manager, so it can be removed after the duel (player & target must be in the same duel).
					if (target.isPlayer() && target.getActingPlayer().isInDuel() && skill.isBad() && (_owner.getDuelId() == target.getActingPlayer().getDuelId()))
					{
						DuelManager.getInstance().onBuff(target.getActingPlayer(), skill);
					}
					
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> success");
					}
				}
				else
				{
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> failed");
					}
				}
			}
			
			if (skill.hasEffectType(L2EffectType.AGGRESSION))
			{
				if (Formulas.calcCubicSkillSuccess(this, target, skill, shld))
				{
					if (target.isAttackable())
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _owner, ((150 * -skill.getEffectPoint()) / (target.getLevel() + 7)));
					}
					
					// Apply effects
					skill.applyEffects(_owner, target, false, false, true, 0);
					
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> success");
					}
				}
				else
				{
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> failed");
					}
				}
			}
		}
	}
	
	/**
	 * @param owner
	 * @param target
	 * @return true if the target is inside of the owner's max Cubic range
	 */
	public static boolean isInCubicRange(Creature owner, Creature target)
	{
		if ((owner == null) || (target == null))
		{
			return false;
		}
		
		int x, y, z;
		// temporary range check until real behavior of cubics is known/coded
		int range = MAX_MAGIC_RANGE;
		
		x = (owner.getX() - target.getX());
		y = (owner.getY() - target.getY());
		z = (owner.getZ() - target.getZ());
		
		return (((x * x) + (y * y) + (z * z)) <= (range * range));
	}
	
	/** this sets the friendly target for a cubic */
	public void cubicTargetForHeal()
	{
		Creature target = null;
		double percentleft = 100.0;
		Party party = _owner.getParty();
		
		// if owner is in a duel but not in a party duel, then it is the same as he does not have a party
		if (_owner.isInDuel())
		{
			if (!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
			{
				party = null;
			}
		}
		
		if ((party != null) && !_owner.isInOlympiadMode())
		{
			// Get all visible objects in a spheric area near the L2Character
			// Get a list of Party Members
			for (Creature partyMember : party.getMembers())
			{
				if (!partyMember.isDead())
				{
					// if party member not dead, check if he is in cast range of heal cubic
					if (isInCubicRange(_owner, partyMember))
					{
						// member is in cubic casting range, check if he need heal and if he have
						// the lowest HP
						if (partyMember.getCurrentHp() < partyMember.getMaxHp())
						{
							if (percentleft > (partyMember.getCurrentHp() / partyMember.getMaxHp()))
							{
								percentleft = (partyMember.getCurrentHp() / partyMember.getMaxHp());
								target = partyMember;
							}
						}
					}
				}
				final Summon pet = partyMember.getPet();
				if (pet != null)
				{
					if (pet.isDead() || !isInCubicRange(_owner, pet))
					{
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (pet.getCurrentHp() < pet.getMaxHp())
					{
						if (percentleft > (pet.getCurrentHp() / pet.getMaxHp()))
						{
							percentleft = (pet.getCurrentHp() / pet.getMaxHp());
							target = pet;
						}
					}
				}
				for (Summon s : partyMember.getServitors().values())
				{
					if (s.isDead() || !isInCubicRange(_owner, s))
					{
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (s.getCurrentHp() < s.getMaxHp())
					{
						if (percentleft > (s.getCurrentHp() / s.getMaxHp()))
						{
							percentleft = (s.getCurrentHp() / s.getMaxHp());
							target = s;
						}
					}
				}
			}
		}
		else
		{
			if (_owner.getCurrentHp() < _owner.getMaxHp())
			{
				percentleft = (_owner.getCurrentHp() / _owner.getMaxHp());
				target = _owner;
			}
			for (Summon summon : _owner.getServitors().values())
			{
				if (!summon.isDead() && (summon.getCurrentHp() < summon.getMaxHp()) && (percentleft > (summon.getCurrentHp() / summon.getMaxHp())) && isInCubicRange(_owner, summon))
				{
					target = summon;
				}
			}
			final Summon pet = _owner.getPet();
			if (pet != null)
			{
				if (!pet.isDead() && (pet.getCurrentHp() < pet.getMaxHp()) && (percentleft > (pet.getCurrentHp() / pet.getMaxHp())) && isInCubicRange(_owner, pet))
				{
					target = _owner.getPet();
				}
			}
		}
		
		_target = target;
	}
	
	public boolean givenByOther()
	{
		return _givenByOther;
	}
}
