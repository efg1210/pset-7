package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.apcsa.model.User;

public class Teacher extends User {

    private int teacherId;
    private int departmentId;
    private String firstName;
    private String lastName;
    
    public Teacher (User user, ResultSet rs) throws SQLException {
    	this(rs.getInt("teacher_id"),
             rs.getInt("department_id"),
             rs.getString("firstName"),
             rs.getString("lastName"),
             rs
        );
    }

	public Teacher(int teacherId, int departmentId, String firstName, String lastName,  ResultSet rs) throws SQLException {
    	super(rs);
		this.teacherId = teacherId;
		this.departmentId = departmentId;
		this.firstName = firstName;
		this.lastName = lastName;
	}

}