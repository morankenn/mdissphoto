package org.mdissjava.mdisscore.model.dao;

import org.bson.types.ObjectId;
import org.mdissjava.mdisscore.model.pojo.Login;
import org.mdissjava.mdisscore.model.pojo.User;



public interface LoginDao {

	public User login();
	public void addLoginDetails(Login login);
	void modifyLoginDetails(ObjectId id, String email, String pass);
	void removeLoginDetails(ObjectId id);
}