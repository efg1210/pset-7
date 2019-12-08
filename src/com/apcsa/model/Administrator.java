package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.apcsa.model.User;

public class Administrator extends User {

    private int administratorId;
    private String firstName;
    private String lastName;
    private String jobTitle;
    
    /**
     * Creates an instance of the Administrator class.
     *
     * @param rs a ResultSet of Administrator information
     */
    
    public Administrator (User user, ResultSet rs) throws SQLException {
    	this(rs.getInt("administrator_id"),
    		 rs.getString("first_name"),
    		 rs.getString("last_name"),
    		 rs.getString("job_title"),
    		 rs
    	);
    }

    /**
     * Creates an instance of the Teacher class.
     *
     * @param administratorId the administrator's ID
     * @param firstName the administrator's first name
     * @param lastName the administrator's last name
     * @param jobTitle the administrator's job title
     * @param rs a ResultSet of Administrator information
     */
    
    public Administrator(int administratorId, String firstName, String lastName, String jobTitle, ResultSet rs) throws SQLException {
		super(rs);
		this.administratorId = administratorId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.jobTitle = jobTitle;
	}
 
}