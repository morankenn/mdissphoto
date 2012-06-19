package org.mdissjava.mdisscore.controller.bll.impl;

import java.io.IOException;
import java.util.List;

import org.mdissjava.commonutils.properties.PropertiesFacade;
import org.mdissjava.mdisscore.controller.bll.AlbumManager;
import org.mdissjava.mdisscore.controller.bll.UserManager;
import org.mdissjava.mdisscore.model.dao.UserDao;
import org.mdissjava.mdisscore.model.dao.impl.UserDaoImpl;
import org.mdissjava.mdisscore.model.pojo.User;
import org.mdissjava.notifier.event.manager.NotificationManager;
import org.mdissjava.notifier.event.observable.NewFollowerObservable;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class UserManagerImpl implements UserManager {

	private final String GLOBAL_PROPS_KEY = "globals";
	private final String DEFAULT_ALBUM_NAME = "default_album_name.var";
	private UserDao userDao = new UserDaoImpl();
	private AlbumManager albumbll = new AlbumManagerImpl();

	@Override
	public void saveUser(User user) {
		if (user.getId() == 0) {
			user.setPass(PEncoder(user.getPass()));
			userDao.addUser(user);
			String albumName = "Master";
			try {
				albumName = new PropertiesFacade().getProperties(GLOBAL_PROPS_KEY).getProperty(DEFAULT_ALBUM_NAME);
				albumbll.insertAlbum(albumName, user.getNick());
			} 
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			userDao.updateUser(user);
		}
	}

	@Override
	public void deleteUser(User user) {
		userDao.deleteUser(user);
	}

	@Override
	public User getUserById(int id) {
		return userDao.getUserById(id);
	}

	@Override
	public List<User> findFollows(String user, int pageNumber, int maxResults) {
		return userDao.findFollows(user, pageNumber, maxResults);
	}

	@Override
	public List<User> findFollowers(String user, int pageNumber, int maxResults) {
		return userDao.findFollowers(user, pageNumber, maxResults);
	}


	@Override
	public boolean changePassword(User user, String oldPassword, String newPassword) {
		if (user.getPass().equals(PEncoder(oldPassword))) {
			user.setPass(PEncoder(newPassword));
			userDao.updateUser(user);
			return true;
		} else
			return false;
	}

	/**
	 * Password hashing function sha-256
	 * 
	 * @param password
	 *            the orignal password
	 * @return string the hash code
	 */

	private String PEncoder(String password) {
		PasswordEncoder sha256Encoder = new ShaPasswordEncoder(256);
		return sha256Encoder.encodePassword(password, null);
	}

	@Override
	public boolean emailAlreadyExists(String email) {
		return userDao.emailAlreadyExists(email);
	}

	@Override
	public boolean nickAlreadyExists(String nick) {
		return userDao.nickAlreadyExists(nick);
	}

	public User getUserByNick(String nick) {
		return userDao.getUserByNick(nick);
	}

	@Override
	public void addPrivateFollow(String userNickname, User follow) {
		
		if (!this.followsUser(userNickname, follow))
			userDao.addFollow(userNickname, follow);
		else
			throw new IllegalStateException("You already follow this user");
	}
	
	@Override
	public void addFollow(String userNickname, User follow) {
		
		NotificationManager notifier = NotificationManager.getInstance();
		NewFollowerObservable nfo = notifier.getNewFollowerObservable();
		//if it is not private then add the follow
		if (!follow.getConfiguration().isIsPrivate()){
			this.addPrivateFollow(userNickname, follow);
			nfo.newFollower(follow.getNick(), userNickname);
		}
		else
			nfo.newPrivateFollower(follow.getNick(), userNickname);
	}

	@Override
	public void addFollower(String userNickname, User follower) {
		userDao.addFollower(userNickname, follower);
	}

	@Override
	public void activateUser(int idUser) {
		userDao.activateUser(idUser);
	}

	@Override
	public boolean followsUser(String userNickname, User follow) {
		return userDao.followsUser(userNickname, follow);
	}

	@Override
	public void deleteFollow(String userNickname, User follow) {
		userDao.deleteFollow(userNickname, follow);

	}

	@Override
	public void deleteFollower(String userNickname, User follower) {
		userDao.deleteFollower(userNickname, follower);

	}

	@Override
	public int getTotalUsers() {		
		return userDao.getTotalUsers();
	}

	@Override
	public String getAvatar(String userId) {
		return userDao.getAvatar(userId);
	}

	@Override
	public List<User> findAllUsers(int pageNumber, int maxResults) {
		return userDao.findAllUsers(pageNumber, maxResults);
	}

	@Override
	public void deactivateUser(int userId) {
		userDao.deactivateUser(userId);	
	}

}
