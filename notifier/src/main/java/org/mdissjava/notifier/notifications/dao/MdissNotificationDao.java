package org.mdissjava.notifier.notifications.dao;

import java.util.List;

import org.mdissjava.notifier.notifications.pojo.MdissNotification;

public interface MdissNotificationDao {
	
	void insertMdissNotification(MdissNotification mdissNotification);
	List<MdissNotification> findMdissNotification(MdissNotification mdissNotification);
	void updateMdissNotification(MdissNotification mdissNotification);
	void deleteMdissNotification(MdissNotification mdissNotification);
	public List<MdissNotification> findUsersMdissNotifications(String userName, int limit) throws IllegalStateException;

}