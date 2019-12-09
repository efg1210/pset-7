package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.apcsa.model.User;

public class Teacher extends User {

    private int teacherId;
    private int departmentId;
    private String firstName;
    private String lastName;
    
    /**
     * Creates an instance of the Teacher class.
     *
     * @param user the User
     * @param rs a ResultSet of Teacher information
     */
    
    public Teacher (User user, ResultSet rs) throws SQLException {
    	this(rs.getInt("teacher_id"),
             rs.getInt("department_id"),
             rs.getString("first_name"),
             rs.getString("last_name"),
             user
        );
    }

    /**
     * Creates an instance of the Teacher class.
     *
     * @param teacherId the teacher's ID
     * @param departmentId the teacher's department
     * @param firstName the teacher's first name
     * @param lastName the teacher's last name
     * @param rs a ResultSet of Teacher information
     */
    
    public Teacher(int teacherId, int departmentId, String firstName, String lastName,  User user) {
    	super(user.getUserId(), user.getAccountType(), user.getUsername(), user.getPassword(), user.getLastLogin());
		this.teacherId = teacherId;
		this.departmentId = departmentId;
		this.firstName = firstName;
		this.lastName = lastName;
	}
    
    /**
     * @return teacherId
     */
    
    public int getTeacherId() {
        return teacherId;
    }
    
    /**
     * @return departmentId
     */
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    /**
     * @return firstName
     */
    
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * @return lastName
     */
    
    public String getLastName() {
        return lastName;
    }

}