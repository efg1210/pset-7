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
     * @param user the User
     * @param rs a ResultSet of Administrator information
     */
    
    public Administrator (User user, ResultSet rs) throws SQLException {
    	this(rs.getInt("administrator_id"),
    		 rs.getString("first_name"),
    		 rs.getString("last_name"),
    		 rs.getString("job_title"),
    		 user
    	);
    }

    /**
     * Creates an instance of the Administrator class.
     *
     * @param administratorId the administrator's ID
     * @param firstName the administrator's first name
     * @param lastName the administrator's last name
     * @param jobTitle the administrator's job title
     * @param rs a ResultSet of Administrator information
     */
    
    public Administrator(int administratorId, String firstName, String lastName, String jobTitle, User user) {
    	super(user.getUserId(), user.getAccountType(), user.getUsername(), user.getPassword(), user.getLastLogin());
		this.administratorId = administratorId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.jobTitle = jobTitle;
	}
    
    /**
     * @return administratorId
     */
    
    public int getAdministratorId() {
        return administratorId;
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
    
    /**
     * @return jobTitle
     */
    
    public String getJobTitle() {
        return jobTitle;
    }
    
 
}