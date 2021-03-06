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
package quests.Q00511_AwlUnderFoot;

import org.l2junity.gameserver.enums.QuestType;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Fort;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;

/**
 * Awl Under Foot (511)
 * @author malyelfik
 */
public final class Q00511_AwlUnderFoot extends Quest
{
	// NPCs
	private static final int[] NPCS =
	{
		35666, // Shanty
		35698, // Southern
		35735, // Hive
		35767, // Valley
		35804, // Ivory
		35835, // Narsell
		35867, // Bayou
		35904, // White Sands
		35936, // Borderland
		35974, // Swamp
		36011, // Archaic
		36043, // Floran
		36081, // Cloud Mountain
		36118, // Tanor
		36149, // Dragonspine
		36181, // Antharas
		36219, // Western
		36257, // Hunter
		36294, // Aaru
		36326, // Demon
		36364, // Monastic
	};
	// Items
	private static final int MARK = 9797;
	private static final int KNIGHT_EPALUETTE = 9912;
	// Misc
	private static final int MIN_LEVEL = 85;
	
	public Q00511_AwlUnderFoot()
	{
		super(511);
		addStartNpc(NPCS);
		addTalkId(NPCS);
		
		addCondMinLevel(MIN_LEVEL, "Warden-00a.htm");
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
			case "Warden-03.html":
			case "Warden-04.html":
			case "Warden-05.html":
			case "Warden-06.html":
			case "Warden-09.html":
				break;
			case "Warden-02.htm":
				qs.startQuest();
				break;
			case "Warden-10.html":
				qs.exitQuest(QuestType.REPEATABLE);
				break;
			default:
				htmltext = null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player, boolean isSimulated)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		if (qs.isCreated())
		{
			final Fort fort = npc.getFort();
			final L2Clan clan = player.getClan();
			htmltext = ((fort != null) && (clan != null) && (clan.getFortId() == fort.getResidenceId())) ? "Warden-01.htm" : "Warden-00b.htm";
		}
		else
		{
			final long itemCount = getQuestItemsCount(player, MARK);
			if (itemCount == 0)
			{
				htmltext = "Warden-07.html";
			}
			else
			{
				if (!isSimulated)
				{
					takeItems(player, MARK, itemCount);
					giveItems(player, KNIGHT_EPALUETTE, itemCount * 2);
				}
				htmltext = "Warden-08.html";
			}
		}
		return htmltext;
	}
}