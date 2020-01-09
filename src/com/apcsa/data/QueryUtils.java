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
     * Checks to see if a username is within the database 
     */
    
    public static final String USERNAME_EXISTS_SQL =
    	"SELECT * FROM users " +
    		"WHERE username = ?";
    
    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String UPDATE_LAST_LOGIN_SQL =
        "UPDATE users " +
            "SET last_login = ? " +
        "WHERE username = ?";
    
    /*
     * Updates password to hashed username and last_login to 0 value.
     */
    
    public static final String RESET_PASSWORD_SQL = 
    	"UPDATE users " +
    		"SET auth = ?, " +
    		"last_login = ? " +
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
    
    public static final String GET_POINTS_EARNED_ON_ASSIGNMENT =
    		"SELECT * FROM ASSIGNMENT_GRADES " +
    			"WHERE COURSE_ID = ? " +
    			"AND ASSIGNMENT_ID = ? " +
    			"AND STUDENT_ID = ? ";
    
    public static final String GET_ASSIGN_GRADE = 
		"SELECT * FROM ASSIGNMENT_GRADES " +
			"WHERE ASSIGNMENT_ID = ? " +
			"AND STUDENT_ID = ?";
    
    public static final String ADD_ASSIGNMENT_GRADE = 
		"INSERT INTO ASSIGNMENT_GRADES " +
			"(course_id, assignment_id, STUDENT_ID, " +
			"POINTS_EARNED, POINTS_POSSIBLE, IS_GRADED) " +
			"VALUES (?, ?, ?, ?, ?, ?)";
    
    public static final String UPDATE_ASSIGNMENT_GRADE = 
    	"UPDATE ASSIGNMENT_GRADES " +
			"SET POINTS_EARNED = ?, " +
    		"IS_GRADED = ? " +
    		"WHERE COURSE_ID = ? " +
    		"AND ASSIGNMENT_ID = ? " +
    		"AND STUDENT_ID = ? ";
    
    public static final String DELETE_GRADES_FROM_ASSIGN =
    		"DELETE FROM ASSIGNMENT_GRADES " +
    			"WHERE ASSIGNMENT_ID = ?";
    
    public static final String GET_TEACHER_IDS =
    		"SELECT * FROM TEACHERS";
    
    public static final String GET_TEACHER_WITH_TEACH_ID =
    		"SELECT * FROM TEACHERS " +
    			"WHERE TEACHER_ID = ?";
    
    public static final String GET_DEP_NAME =
    		"SELECT * FROM DEPARTMENTS " +
    			"WHERE DEPARTMENT_ID = ?";
    
    public static final String GET_DEPARTMENT_IDS =
    		"SELECT * FROM DEPARTMENTS";
    
    public static final String GET_TEACHER_BY_DEPT =
    		"SELECT * FROM TEACHERS " +
    			"WHERE DEPARTMENT_ID = ?";
    
    public static final String GET_STUDENT_IDS =
    		"SELECT * FROM STUDENTS";
    
    public static final String GET_STUDENT_IDS_BY_GRADE =
    		"SELECT * FROM STUDENTS " +
    			"WHERE GRADE_LEVEL = ?";
    
    public static final String GET_ALL_COURSE_NO =
    		"SELECT * FROM COURSES";
    
    public static final String GET_COURSES_FOR_STUDENT =
    	    "SELECT courses.title, grade, courses.course_no FROM course_grades " +
    	         "INNER JOIN courses ON course_grades.course_id = courses.course_id " +
    	         "INNER JOIN students ON students.student_id = course_grades.student_id " +
    	         "WHERE students.student_id = ?";
    
    public static final String GET_ASSIGNMENT_ID_BY_COURSE_ID_AND_STUDENT_ID_AND_MARKING_PERIOD =
    		"SELECT * FROM courses " +
    			"INNER JOIN assignments ON assignments.course_id = courses.course_id " +
    			"INNER JOIN assignment_grades ON assignment_grades.course_id = courses.course_id " +
    			"WHERE courses.course_id = ? AND assignment_grades.student_id = ? AND assignments.marking_period = ?";
    
    public static final String GET_COURSE_GRADES = 
    		"SELECT * FROM COURSE_GRADES " +
    			"WHERE COURSE_ID = ? " +
    			"AND STUDENT_ID = ?";
    
    public static final String UPDATE_COURSE_GRADES = 
        	"UPDATE COURSE_GRADES " +
    			"SET MP1 = ?, " +
        		"MP2 = ?, " +
    			"MIDTERM_EXAM = ?, " +
        		"MP3 = ?, " +
    			"MP4 = ?, " +
        		"FINAL_EXAM = ?, " +
    			"GRADE = ? " +
        		"WHERE COURSE_ID = ? " +
        		"AND STUDENT_ID = ? ";
    
    public static final String UPDATE_GPA =
    		"UPDATE STUDENTS " +
    			"SET GPA = ? " +
    			"WHERE STUDENT_ID = ?";
    
    public static final String GET_FINAL_GRADES = 
    		"SELECT * FROM COURSE_GRADES " +
    			"WHERE STUDENT_ID = ?";
  
    public static final String UPDATE_RANKING = 
        	"UPDATE STUDENTS " +
    			"SET CLASS_RANK = ? " +
        		"WHERE STUDENT_ID = ?";
}