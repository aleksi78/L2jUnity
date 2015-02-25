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
package quests.Q10330_ToTheRuinsOfYeSagira;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.NpcStringId;
import org.l2junity.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10329_BackupSeekers.Q10329_BackupSeekers;

/**
 * To the Ruins of Ye Sagira (10330)
 * @author Gladicek
 */
public final class Q10330_ToTheRuinsOfYeSagira extends Quest
{
	// NPCs
	private static final int ATRAN = 33448;
	private static final int LAKCIS = 32977;
	// Items
	private static final int LEATHER_SHIRT = 22;
	private static final int LEATHER_PANTS = 29;
	// Misc
	private static final int MIN_LEVEL = 8;
	private static final int MAX_LEVEL = 20;
	
	public Q10330_ToTheRuinsOfYeSagira()
	{
		super(10330, Q10330_ToTheRuinsOfYeSagira.class.getSimpleName(), "To the Ruins of Ye Sagira");
		addStartNpc(ATRAN);
		addTalkId(ATRAN, LAKCIS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33448-05.htm");
		addCondCompletedQuest(Q10329_BackupSeekers.class.getSimpleName(), "33448-05.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33448-02.htm":
			case "32977-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33448-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32977-03.htm":
			{
				if (qs.isStarted())
				{
					showOnScreenMsg(player, NpcStringId.ARMOR_HAS_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
					giveAdena(player, 620, true);
					giveItems(player, LEATHER_SHIRT, 1);
					giveItems(player, LEATHER_PANTS, 1);
					addExpAndSp(player, 23000, 5);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == ATRAN ? "33448-01.htm" : "32977-04.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == ATRAN ? "33448-04.htm" : "32977-01.htm";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == ATRAN ? "33448-06.htm" : "32977-05.htm";
				break;
			}
		}
		return htmltext;
	}
}