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
package quests.Q10782_LettersFromTheQueenForsakenPlains;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import quests.LetterQuest;

/**
 * Letters from the Queen: Forsaken Plains (10782)
 * @author malyelfik
 */
public final class Q10782_LettersFromTheQueenForsakenPlains extends LetterQuest
{
	// NPCs
	private static final int ORVEN = 30857;
	private static final int NOVAIN = 33866;
	// Items
	private static final int SOE_ADEN = 39576;
	private static final int SOE_FORSAKEN_PLAINS = 39577;
	private static final int ENCHANT_ARMOR_B = 948;
	// Location
	private static final Location TELEPORT_LOC = new Location(147446, 22761, -1984);
	// Misc
	private static final int MIN_LEVEL = 58;
	private static final int MAX_LEVEL = 60;
	
	public Q10782_LettersFromTheQueenForsakenPlains()
	{
		super(10782);
		addTalkId(ORVEN, NOVAIN);
		
		setIsErtheiaQuest(true);
		setLevel(MIN_LEVEL, MAX_LEVEL);
		setStartLocation(SOE_ADEN, TELEPORT_LOC);
		setStartQuestSound("Npcdialog1.serenia_quest_6");
		registerQuestItems(SOE_ADEN, SOE_FORSAKEN_PLAINS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "30857-02.html":
			case "33866-02.html":
				break;
			case "30857-03.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					giveItems(player, SOE_FORSAKEN_PLAINS, 1);
				}
				break;
			}
			case "33866-03.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, ENCHANT_ARMOR_B, 10);
					giveStoryQuestReward(player, 60);
					addExpAndSp(player, 731010, 175);
					showOnScreenMsg(player, NpcStringId.GROW_STRONGER_HERE_UNTIL_YOU_RECEIVE_THE_NEXT_LETTER_FROM_QUEEN_NAVARI_AT_LV_61, ExShowScreenMessage.TOP_CENTER, 8000);
					qs.exitQuest(false, true);
				}
				break;
			}
			default:
				htmltext = null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		String htmltext = getNoQuestMsg(player);
		if (qs == null)
		{
			return htmltext;
		}
		
		if (qs.isStarted())
		{
			if (npc.getId() == ORVEN)
			{
				htmltext = (qs.isCond(1)) ? "30857-01.html" : "30857-04.html";
			}
			else if (qs.isCond(2))
			{
				htmltext = "33866-01.html";
			}
		}
		return htmltext;
	}
}