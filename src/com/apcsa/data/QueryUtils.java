package com.apcsa.data;

public class QueryUtils {

    /////// QUERY CONSTANTS ///////////////////////////////////////////////////////////////
    
    /*
     * Determines if the default tables were correctly loaded.
     */
	
    public static final String SETUP_SQL =
        "SELECT COUNT(name) AS names FROM sqlite_master " +
            "WHERE type = 'table' " +
        "AND name NOT LIKE 'sqlite_%'";
    
    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String LOGIN_SQL =
        "SELECT * FROM users " +
            "WHERE username = ?" +
        "AND auth = ?";
    
    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String UPDATE_LAST_LOGIN_SQL =
        "UPDATE users " +
            "SET last_login = ? " +
        "WHERE username = ?";
    
    /*
     * Retrieves an administrator associated with a user account.
     */

    public static final String GET_ADMIN_SQL =
        "SELECT * FROM administrators " +
            "WHERE user_id = ?";
    
    /*
     * Retrieves a teacher associated with a user account.
     */

    public static final String GET_TEACHER_SQL =
        "SELECT * FROM teachers " +
            "WHERE user_id = ?";
    
    /*
     * Retrieves a student associated with a user account.
     */

    public static final String GET_STUDENT_SQL =
        "SELECT * FROM students " +
            "WHERE user_id = ?";
    
    /*
     * Updates password
     */
    
    public static final String UPDATE_PASSWORD_SQL =
            "UPDATE users " +
                "SET auth = ? " +
            "WHERE username = ?";
    
    public static final String GET_COURSES_FOR_TEACHER = 
    		"SELECT * FROM COURSES " +
    			"WHERE TEACHER_ID = ?";
    
    public static final String GET_COURSE_ID = 
    		"SELECT * FROM COURSES " +
    			"WHERE COURSE_NO = ?";
    
    public static final String GET_STUDENT_ID_BY_COURSE =
    		"SELECT * FROM course_grades " + 
    			"WHERE course_id = ?";
    
    public static final String GRADE_BY_COURSE = 
    		"SELECT * FROM COURSE_GRADES " +
    			"WHERE COURSE_ID = ? " +
    			"AND STUDENT_ID = ?";
    
    public static final String GET_STUDENT_FROM_STUDENT_ID =
            "SELECT * FROM students " +
                "WHERE STUDENT_ID = ?";
    
    public static final String GET_GRADE =
    		"SELECT * FROM course_grades " + 
    			"WHERE course_id = ? " +
    			"AND STUDENT_ID = ?";
    
    public static final String ADD_ASSIGNMENT = 
    		"INSERT INTO ASSIGNMENTS " +
    			"(course_id, assignment_id, marking_period, " +
    			"is_midterm, is_final, title, point_value) " +
    			"VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    public static final String GET_ASSIGNMENT_BY_MP =
    		"SELECT * FROM ASSIGNMENTS " +
    			"WHERE COURSE_ID = ? " +
    			"AND MARKING_PERIOD = ?";
    
    public static final String GET_ASSIGNMENT_IDS = 
    		"SELECT * FROM ASSIGNMENTS ";
    
    public static final String GET_ASSIGNMENT_BY_ID =
    		"SELECT * FROM ASSIGNMENTS " +
    			"WHERE ASSIGNMENT_ID = ?";
    
    public static final String DELETE_ASSIGNMENT =
    		"DELETE FROM ASSIGNMENTS " +
    			"WHERE ASSIGNMENT_ID = ?";
    
    public static final String GET_ASSIGNMENT_BY_NAME =
    		"SELECT * FROM ASSIGNMENTS " +
    			"WHERE TITLE = ?";
    
    public static final String GET_ASSIGNMENT_BY_MID =
    		"SELECT * FROM ASSIGNMENTS " +
    			"WHERE COURSE_ID = ? " +
    			"AND IS_MIDTERM = 1";
    
    public static final String GET_ASSIGNMENT_BY_FIN =
    		"SELECT * FROM ASSIGNMENTS " +
    			"WHERE COURSE_ID = ? " +
    			"AND IS_FINAL = 1";
}