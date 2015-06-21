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
package org.l2junity.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.enums.MailType;
import org.l2junity.gameserver.idfactory.IdFactory;
import org.l2junity.gameserver.instancemanager.tasks.MessageDeletionTask;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Message;
import org.l2junity.gameserver.network.client.send.ExNoticePostArrived;
import org.l2junity.gameserver.network.client.send.ExUnReadMailCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Migi, DS
 */
public final class MailManager
{
	private static final Logger _log = LoggerFactory.getLogger(MailManager.class);
	
	private final Map<Integer, Message> _messages = new ConcurrentHashMap<>();
	
	protected MailManager()
	{
		load();
	}
	
	private void load()
	{
		int count = 0;
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement ps = con.createStatement();
			ResultSet rs = ps.executeQuery("SELECT * FROM messages ORDER BY expiration"))
		{
			while (rs.next())
			{
				
				final Message msg = new Message(rs);
				
				int msgId = msg.getId();
				_messages.put(msgId, msg);
				
				count++;
				
				long expiration = msg.getExpiration();
				
				if (expiration < System.currentTimeMillis())
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), 10000);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), expiration - System.currentTimeMillis());
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Error loading from database:" + e.getMessage(), e);
		}
		_log.info(getClass().getSimpleName() + ": Successfully loaded " + count + " messages.");
	}
	
	public final Message getMessage(int msgId)
	{
		return _messages.get(msgId);
	}
	
	public final Collection<Message> getMessages()
	{
		return _messages.values();
	}
	
	public final boolean hasUnreadPost(PlayerInstance player)
	{
		final int objectId = player.getObjectId();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && msg.isUnread())
			{
				return true;
			}
		}
		return false;
	}
	
	public final int getInboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver())
			{
				size++;
			}
		}
		return size;
	}
	
	public final int getOutboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender())
			{
				size++;
			}
		}
		return size;
	}
	
	public final List<Message> getInbox(int objectId)
	{
		final List<Message> inbox = new LinkedList<>();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver())
			{
				inbox.add(msg);
			}
		}
		return inbox;
	}
	
	public final long getUnreadCount(PlayerInstance player)
	{
		return getInbox(player.getObjectId()).stream().filter(Message::isUnread).count();
	}
	
	public int getMailsInProgress(int objectId)
	{
		int count = 0;
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getMailType() == MailType.REGULAR))
			{
				if ((msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver() && !msg.isReturned() && msg.hasAttachments())
				{
					count++;
				}
				else if ((msg.getSenderId() == objectId) && !msg.isDeletedBySender() && !msg.isReturned() && msg.hasAttachments())
				{
					count++;
				}
			}
		}
		return count;
	}
	
	public final List<Message> getOutbox(int objectId)
	{
		final List<Message> outbox = new LinkedList<>();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender())
			{
				outbox.add(msg);
			}
		}
		return outbox;
	}
	
	public void sendMessage(Message msg)
	{
		_messages.put(msg.getId(), msg);
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = Message.getStatement(msg, con))
		{
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Error saving message:" + e.getMessage(), e);
		}
		
		final PlayerInstance receiver = World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null)
		{
			receiver.sendPacket(ExNoticePostArrived.valueOf(true));
			receiver.sendPacket(new ExUnReadMailCount(receiver));
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msg.getId()), msg.getExpiration() - System.currentTimeMillis());
	}
	
	public final void markAsReadInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET isUnread = 'false' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Error marking as read message:" + e.getMessage(), e);
		}
	}
	
	public final void markAsDeletedBySenderInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET isDeletedBySender = 'true' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Error marking as deleted by sender message:" + e.getMessage(), e);
		}
	}
	
	public final void markAsDeletedByReceiverInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET isDeletedByReceiver = 'true' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Error marking as deleted by receiver message:" + e.getMessage(), e);
		}
	}
	
	public final void removeAttachmentsInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE messages SET hasAttachments = 'false' WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Error removing attachments in message:" + e.getMessage(), e);
		}
	}
	
	public final void deleteMessageInDb(int msgId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM messages WHERE messageId = ?"))
		{
			ps.setInt(1, msgId);
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Error deleting message:" + e.getMessage(), e);
		}
		
		_messages.remove(msgId);
		IdFactory.getInstance().releaseId(msgId);
	}
	
	/**
	 * Gets the single instance of {@code MailManager}.
	 * @return single instance of {@code MailManager}
	 */
	public static MailManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MailManager _instance = new MailManager();
	}
}
