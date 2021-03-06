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
package quests.Q10328_RequestToSealTheEvilFragment;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

import quests.Q10327_IntruderWhoWantsTheBookOfGiants.Q10327_IntruderWhoWantsTheBookOfGiants;

/**
 * Request to Seal the Evil Fragment (10328)
 * @author Gladicek
 */
public final class Q10328_RequestToSealTheEvilFragment extends Quest
{
	// NPCs
	private static final int PANTHEON = 32972;
	private static final int KAKAI = 30565;
	// Items
	private static final int EVIL_FRAGMENT = 17577;
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10328_RequestToSealTheEvilFragment()
	{
		super(10328);
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON, KAKAI);
		addCondMaxLevel(MAX_LEVEL, "32972-06.htm");
		registerQuestItems(EVIL_FRAGMENT);
		addCondCompletedQuest(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName(), "32972-06.htm");
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
			case "32972-04.htm":
			{
				qs.startQuest();
				giveItems(player, EVIL_FRAGMENT, 1);
				htmltext = event;
				break;
			}
			case "32972-02.htm":
			case "32972-03.htm":
			case "30565-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30565-03.htm":
			{
				if (qs.isStarted())
				{
					giveAdena(player, 20000, true);
					addExpAndSp(player, 13000, 5);
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
				if (npc.getId() == PANTHEON)
				{
					htmltext = "32972-01.htm";
					break;
				}
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == PANTHEON ? "32972-05.htm" : "30565-01.htm";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == PANTHEON ? "32972-07.htm" : "30565-04.htm";
				break;
			}
		}
		return htmltext;
	}
}