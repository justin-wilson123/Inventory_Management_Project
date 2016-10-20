package utilities;

import java.util.Random;

import models.User;

public class UserSession {

	private boolean add;
	private boolean edit;
	private	boolean delete;
	private String userID;
	
	public UserSession() {
		add = edit = delete = false;
		userID = null;
	}
	
	public UserSession(User user){
		
		Random rand = new Random();
		int randomNumber = rand.nextInt(100000); // 0-100000.
		
		this.userID = user.getUser() + randomNumber;
		
		if ((user.getAdd().equals("yes")) || (user.getAdd().equals("Yes")))
			this.add = true;
		else
			this.add = false;
		
		if ((user.getEdit().equals("yes")) || (user.getEdit().equals("Yes")))
			this.edit = true;
		else
			this.edit = false;
		
		if ((user.getDelete().equals("yes")) || (user.getDelete().equals("Yes")))
			this.delete = true;
		else
			this.delete = false;
	}
	
	// check attribute
	public boolean checkAdd(){
		return this.add;
	}
	
	public boolean checkEdit(){
		return this.edit;
	}
	
	public boolean checkDelete(){
		return this.delete;
	}
	
	public String getUser(){
		return this.userID;
	}
	
}
