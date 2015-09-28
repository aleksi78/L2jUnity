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
package ai.npc.Lykus;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.itemcontainer.Inventory;

import ai.npc.AbstractNpcAI;

/**
 * Lykus AI.
 * @author St3eT
 */
public final class Lykus extends AbstractNpcAI
{
	// NPCs
	private static final int LYKUS = 33521;
	// Items
	private static final int POLISHED_SHIELD = 17723; // Polished Ancient Hero's Shield
	private static final int OLD_SHIELD = 17724; // Orbis Ancient Hero's Shield
	
	public Lykus()
	{
		super(Lykus.class.getSimpleName(), "ai/npc");
		addFirstTalkId(LYKUS);
		addTalkId(LYKUS);
		addStartNpc(LYKUS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		
		switch (event)
		{
			case "33521-01.html":
			case "33521-02.html":
			case "33521-03.html":
			case "33521-04.html":
			case "33521-05.html":
			case "33521-07.html":
			case "33521-08.html":
			case "33521-12.html":
			{
				htmltext = event;
				break;
			}
			default:
			{
				if (event.startsWith("trade"))
				{
					final int count = (int) (event.equals("trade1") ? 1 : getQuestItemsCount(player, OLD_SHIELD));
					
					if (!hasAtLeastOneQuestItem(player, OLD_SHIELD))
					{
						htmltext = "33521-11.html";
					}
					else if (player.getAdena() < (5000 * count))
					{
						htmltext = "33521-10.html";
					}
					else
					{
						takeItems(player, Inventory.ADENA_ID, 5000 * count);
						takeItems(player, OLD_SHIELD, count);
						giveItems(player, POLISHED_SHIELD, count);
						htmltext = "33521-09.html";
					}
				}
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Lykus();
	}
}
