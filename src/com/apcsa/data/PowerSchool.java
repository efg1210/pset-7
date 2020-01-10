package com.apcsa.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.apcsa.controller.Utils;
import com.apcsa.model.Administrator;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;

public class PowerSchool {

    private final static String PROTOCOL = "jdbc:sqlite:";
    private final static String DATABASE_URL = "data/powerschool.db";

    /**
     * Initializes the database if needed (or if requested).
     *
     * @param force whether or not to force-reset the database
     * @throws Exception
     */

    public static void initialize(boolean force) {
        if (force) {
            reset();    // force reset
        } else {
            boolean required = false;

            // check if all tables have been created and loaded in database

            try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(QueryUtils.SETUP_SQL)) {

                while (rs.next()) {
                    if (rs.getInt("names") != 9) {
                        required = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // build database if needed

            if (required) {
                reset();
            }
        }
    }

    /**
     * Retrieves the User object associated with the requested login.
     *
     * @param username the username of the requested User
     * @param password the password of the requested User
     * @return the User object for valid logins; null for invalid logins
     */

    public static User login(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.LOGIN_SQL)) {

            stmt.setString(1, username);
            stmt.setString(2, Utils.getHash(password));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = new Timestamp(new Date().getTime());
                    int affected = PowerSchool.updateLastLogin(conn, username, ts);

                    if (affected != 1) {
                        System.err.println("Unable to update last login (affected rows: " + affected + ").");
                    }

                    return new User(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static boolean passwordTest(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.LOGIN_SQL)) {

            stmt.setString(1, username);
            stmt.setString(2, Utils.getHash(password));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                	return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Returns the administrator account associated with the user.
     *
     * @param user the user
     * @return the administrator account if it exists
     */

    public static User getAdministrator(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ADMIN_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Administrator(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Returns the teacher account associated with the user.
     *
     * @param user the user
     * @return the teacher account if it exists
     */

    public static User getTeacher(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Teacher(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Returns the student account associated with the user.
     *
     * @param user the user
     * @return the student account if it exists
     */

    public static User getStudent(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }
    
    public static int resetPassword(String username, Timestamp ts) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.RESET_PASSWORD_SQL)) {
        	
        	conn.setAutoCommit(false);
            stmt.setString(1, Utils.getHash(username));
            stmt.setString(2, ts.toString());
            stmt.setString(3, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            
            return -2;
        }
    }

    /*
     * Establishes a connection to the database.
     *
     * @return a database Connection object
     * @throws SQLException
     */

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PROTOCOL + DATABASE_URL);
    }

    /*
     * Updates the last login time for the user.
     *
     * @param conn the current database connection
     * @param username the user's username
     * @param ts the current timestamp
     * @return the number of affected rows
     */

    private static int updateLastLogin(Connection conn, String username, Timestamp ts) {
        try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_LAST_LOGIN_SQL)) {
        	
            conn.setAutoCommit(false);
            stmt.setString(1, ts.toString());
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return -1;
        }
    }
    
    /*
     * Updates password
     */
    
    public static int updatePassword(String username, String password) {

    	try (Connection conn = getConnection();
    		 PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_PASSWORD_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, password);
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return -1;
        }
    }

    /*
     * Builds the database. Executes a SQL script from a configuration file to
     * create the tables, setup the primary and foreign keys, and load sample data.
     */

    private static void reset() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader br = new BufferedReader(new FileReader(new File("config/setup.sql")))) { // TODO FIX THIS

            String line;
            StringBuffer sql = new StringBuffer();

            // read the configuration file line-by-line to get SQL commands

            while ((line = br.readLine()) != null) {
                sql.append(line);
            }

            // execute SQL commands one-by-one

            for (String command : sql.toString().split(";")) {
                if (!command.strip().isEmpty()) {
                    stmt.executeUpdate(command);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to load SQL configuration file.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: Unable to open and/or read SQL configuration file.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute SQL script from configuration file.");
            e.printStackTrace();
        }
    }
    
    public static ArrayList<String> teacherCourses(User teacher) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSES_FOR_TEACHER)) {

			stmt.setInt(1, ((Teacher) teacher).getTeacherId());
			
			ArrayList<String> courses = new ArrayList<String>();
			   
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					courses.add(rs.getString("course_no"));
				}
			}
			return courses;
        } catch (SQLException e) {
        	e.printStackTrace();
        }

        return null;
    }
    
    public static int courseID(String courseName) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID)) {

			stmt.setString(1, courseName);
			   
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("course_id");
				}
			}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}

        return -1;
    }
    
    public static ArrayList<Integer> studentIDByCourse(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_ID_BY_COURSE)) {

			stmt.setInt(1, courseID);
			   
			ArrayList<Integer> studentIDs = new ArrayList<Integer>();
			   
			try (ResultSet rs = stmt.executeQuery()) {            	   
				while (rs.next()) {
					studentIDs.add(rs.getInt("student_id"));
				}
			}
			return studentIDs;
        } catch (SQLException e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    public static String studentFirstName(int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_FROM_STUDENT_ID)) {

           stmt.setInt(1, studentID);

           String firstName;
           
           try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
            	   firstName = rs.getString("first_name");
                   return firstName;
               }
           }
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return null;
    }
    
    public static String studentLastName(int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_FROM_STUDENT_ID)) {

			stmt.setInt(1, studentID);
			
			String lastName;
			       
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					lastName = rs.getString("last_name");
					return lastName;
				}
			}
        } catch (SQLException e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    public static double courseGrade(int courseID, int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_GRADE)) {

		   stmt.setInt(1, courseID);   
		   stmt.setInt(2, studentID);
	
	       double grade = 0;
	       
	       try (ResultSet rs = stmt.executeQuery()) {
	           if (rs.next()) {
	        	   grade = rs.getDouble("grade");
	           }
	       }
	       return grade;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return -1;
    }
    
    public static void addAssignment(int courseID, int assignmentID, int mp, int isMid,
      int isFinal, String title, int pointValue) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.ADD_ASSIGNMENT)) {

    		stmt.setInt(1, courseID);
    		stmt.setInt(2, assignmentID);
    		stmt.setInt(3, mp);
    		stmt.setInt(4, isMid);
    		stmt.setInt(5, isFinal);
    		stmt.setString(6, title);
    		stmt.setInt(7, pointValue);
               
    		stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<String> assignmentNameByMP(int courseID, int mp) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MP)) {

		        stmt.setInt(1, courseID);
		        stmt.setInt(2, mp);
		        
		        ArrayList<String> assignments = new ArrayList<String>();
		        
		        try (ResultSet rs = stmt.executeQuery()) {            	   
		     	   while (rs.next()) {
		     		  assignments.add(rs.getString("title"));
		            }
		        }
		        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static int getLastAssignID() {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_IDS)) {
    		
	       int assignmentID = 0;
	       
	       try (ResultSet rs = stmt.executeQuery()) {
	           while (rs.next()) {
	        	   assignmentID = rs.getInt("assignment_ID");
	           }
	       }
	       return assignmentID;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return -1;
    }
    
    public static ArrayList<Integer> assignmentValuesByMP(int courseID, int mp) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MP)) {

	        stmt.setInt(1, courseID);
	        stmt.setInt(2, mp);
	        
	        ArrayList<Integer> assignments = new ArrayList<Integer>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  assignments.add(rs.getInt("point_value"));
	           }
	        }
	        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    public static ArrayList<Integer> pointsEarnedByStudent(int courseID, int assignmentID, int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_POINTS_EARNED_ON_ASSIGNMENT)) {
    		
    		
	        stmt.setInt(1, courseID);
	        stmt.setInt(2, assignmentID);
	        stmt.setInt(3, studentID);
	        
	        ArrayList<Integer> assignments = new ArrayList<Integer>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	     	   while (rs.next()) {
	     		  assignments.add(rs.getInt("points_earned"));
	           }
	        }
	        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    public static int getAssignmentIDByCourseIDAndStudentIDAndMarkingPeriod(int courseID, int studentID, int markingPeriod) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_ID_BY_COURSE_ID_AND_STUDENT_ID_AND_MARKING_PERIOD)) {

	        stmt.setInt(1, courseID);
	        stmt.setInt(2, studentID);
	        stmt.setInt(3, markingPeriod);
	        
	        int assignment_id = -1;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   if (rs.next()) {
	     		  assignment_id = rs.getInt("assignment_id");
	           }
	        }
	        return assignment_id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return -1;
    }
    
    public static void deleteAssignment(int assignmentID) {
		try (Connection conn = getConnection();
		  PreparedStatement stmt = conn.prepareStatement(QueryUtils.DELETE_ASSIGNMENT)) {
			
			stmt.setInt(1, assignmentID);
			stmt.executeUpdate();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
    }
    
    public static int assignmentIDByName(String name) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_NAME)) {

	        stmt.setString(1, name);
	        
	        int assignmentID = 0;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   if (rs.next()) {
	     		  assignmentID = rs.getInt("assignment_id");
	           }
	        }
	        return assignmentID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return -1;
    }
    
    public static ArrayList<String> assignmentNameByMid(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MID)) {

	        stmt.setInt(1, courseID);
	        
	        ArrayList<String> assignments = new ArrayList<String>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  assignments.add(rs.getString("title"));
	           }
	        }
	        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    public static int assignmentIDByMid(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MID)) {

	        stmt.setInt(1, courseID);
	        
	        int assignmentID = -1;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   if (rs.next()) {
	     		  assignmentID = Integer.parseInt((rs.getString("assignment_ID")));
	           }
	        }
	        return assignmentID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return -1;
    }
    
    public static int getExamPointsEarned(int courseID, int assignmentID, int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_POINTS_EARNED_ON_ASSIGNMENT)) {
    		
	        stmt.setInt(1, courseID);
	        stmt.setInt(2, assignmentID);
	        stmt.setInt(3, studentID);
	        
	        int pointsEarned = 0;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
		     	   if (rs.next()) {
		     		  pointsEarned = Integer.parseInt((rs.getString("points_earned")));
		           }
		        }
	        return pointsEarned;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return -1;
    }
    
    public static int getStudentIDbyUserID(int userID) {
    	try (Connection conn = getConnection();
    	          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_SQL)) {
    	    		
    		        stmt.setInt(1, userID);
    		        
    		        int studentID = -1;
    		        
    		        try (ResultSet rs = stmt.executeQuery()) {            	   
    			     	   if (rs.next()) {
    			     		  studentID = rs.getInt("student_ID");
    			           }
    			        }
    		        return studentID;
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	        }
    	    	return -1;
    }
    
    public static int getExamPointsPossible(int courseID, int assignmentID, int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_POINTS_EARNED_ON_ASSIGNMENT)) {
    		
	        stmt.setInt(1, courseID);
	        stmt.setInt(2, assignmentID);
	        stmt.setInt(3, studentID);
	        
	        int pointsEarned = 0;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
		     	   if (rs.next()) {
		     		  pointsEarned = Integer.parseInt((rs.getString("points_possible")));
		           }
		        }
	        return pointsEarned;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return -1;
    }
    
    public static ArrayList<String> assignmentNameByFin(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_FIN)) {

	        stmt.setInt(1, courseID);
	        
	        ArrayList<String> assignments = new ArrayList<String>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  assignments.add(rs.getString("title"));
	           }
	        }
	        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    public static int assignmentIDByFin(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_FIN)) {

	        stmt.setInt(1, courseID);
	        
	        int assignmentID = -1;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   if (rs.next()) {
	     		  assignmentID = Integer.parseInt((rs.getString("assignment_ID")));
	           }
	        }
	        return assignmentID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return -1;
    }
    
    public static ArrayList<Integer> assignmentValuesByMid(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MID)) {

		    stmt.setInt(1, courseID);
		    
		    ArrayList<Integer> assignments = new ArrayList<Integer>();
		    
		    try (ResultSet rs = stmt.executeQuery()) {            	   
		 	   while (rs.next()) {
		 		  assignments.add(rs.getInt("point_value"));
		       }
		    }
		    return assignments;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
    	return null;
    }
    
    public static ArrayList<Integer> assignmentValuesByFin(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_FIN)) {

	        stmt.setInt(1, courseID);
	        
	        ArrayList<Integer> assignments = new ArrayList<Integer>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  assignments.add(rs.getInt("point_value"));
	           }
	        }
	        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    public static double assignmentGrade(int assignmentID, int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGN_GRADE)) {

			stmt.setInt(1, assignmentID);   
			stmt.setInt(2, studentID);
			
			double grade = -1;
			   
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					grade = rs.getDouble("points_earned");
				}
			}
			return grade;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return -1;
    }
    
    public static void addAssignmentGrade(int courseID, int assignmentID, int studentID, 
      double pointsEarned, double pointsPoss, int isGraded) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.ADD_ASSIGNMENT_GRADE)) {

     		stmt.setInt(1, courseID);
     		stmt.setInt(2, assignmentID);
     		stmt.setInt(3, studentID);
     		stmt.setDouble(4, pointsEarned);
     		stmt.setDouble(5, pointsPoss);
     		stmt.setInt(6, isGraded);
                
     		stmt.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
         }
    }
    
	public static void updateAssignmentGrade(int courseID, int assignmentID, int studentID, 
	  double pointsEarned, int isGraded) {
		try (Connection conn = getConnection();
	      PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_ASSIGNMENT_GRADE)) {
	
			stmt.setDouble(1, pointsEarned);
	 		stmt.setInt(2, isGraded);
			stmt.setInt(3, courseID);
	 		stmt.setInt(4, assignmentID);
	 		stmt.setInt(5, studentID);
	
	 		stmt.executeUpdate();
	     } catch (SQLException e) {
	         e.printStackTrace();
	     }
	}
	
	public static void deleteGrades(int assignmentID) {
		try (Connection conn = getConnection();
		  PreparedStatement stmt = conn.prepareStatement(QueryUtils.DELETE_GRADES_FROM_ASSIGN)) {
			
			stmt.setInt(1, assignmentID);
			stmt.executeUpdate();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
    }
	
	public static ArrayList<Integer> teacherIDs() {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_IDS)) {
                
            ArrayList<Integer> teacherIDs = new ArrayList<Integer>();
            
            try (ResultSet rs = stmt.executeQuery()) {            	   
         	   while (rs.next()) {
         		  teacherIDs.add(rs.getInt("teacher_id"));
               }
            }
            return teacherIDs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
	
	public static String teacherFirstName(int teacherID) {
		try (Connection conn = getConnection();
	      PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_WITH_TEACH_ID)) {

			stmt.setInt(1, teacherID);
			
			String firstName;
			       
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					firstName = rs.getString("first_name");
					return firstName;
				}
			}
        } catch (SQLException e) {
        	e.printStackTrace();
        }
		return null;
	}
	
	public static String teacherLastName(int teacherID) {
		try (Connection conn = getConnection();
	      PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_WITH_TEACH_ID)) {

			stmt.setInt(1, teacherID);
			
			String lastName;
			       
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					lastName = rs.getString("last_name");
					return lastName;
				}
			}
        } catch (SQLException e) {
        	e.printStackTrace();
        }
		return null;
	}
	
	public static int teacherDepartmentID(int teacherID) {
		try (Connection conn = getConnection();
	      PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_WITH_TEACH_ID)) {

			stmt.setInt(1, teacherID);
			
			int depID;
			       
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					depID = rs.getInt("department_id");
					return depID;
				}
			}
        } catch (SQLException e) {
        	e.printStackTrace();
        }
		return -1;
	}
	
	public static String depByID(int depID) {
		try (Connection conn = getConnection();
		  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_DEP_NAME)) {

			stmt.setInt(1, depID);
			
			String depName;
			       
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					depName = rs.getString("title");
					return depName;
				}
			}
        } catch (SQLException e) {
        	e.printStackTrace();
        }
		return null;
	}
	
	public static ArrayList<Integer> deptIDs() {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_DEPARTMENT_IDS)) {
                
	        ArrayList<Integer> deptIDs = new ArrayList<Integer>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  deptIDs.add(rs.getInt("department_id"));
	           }
	        }
	        return deptIDs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
	
	public static ArrayList<Integer> teachersByDept(int deptID) {
    	try (Connection conn = getConnection();
    	  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_BY_DEPT)) {
                
    		stmt.setInt(1, deptID);
    		
    		ArrayList<Integer> teacherIDs = new ArrayList<Integer>();
                
            try (ResultSet rs = stmt.executeQuery()) {            	   
         	   while (rs.next()) {
         		  teacherIDs.add(rs.getInt("teacher_id"));
               }
            }
            return teacherIDs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
	
	public static ArrayList<Integer> studentIDs() {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_IDS)) {
                
	        ArrayList<Integer> studentIDs = new ArrayList<Integer>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  studentIDs.add(rs.getInt("student_id"));
	           }
	        }
	        return studentIDs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
	
	public static int studentGradYear(int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_FROM_STUDENT_ID)) {

           stmt.setInt(1, studentID);

           int gradYear;
           
           try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
            	   gradYear = rs.getInt("graduation");
                   return gradYear;
               }
           }
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return -1;
    }
	
	public static ArrayList<Integer> studentsByGrade(int gradeLevel) {
		try (Connection conn = getConnection();
    	  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_IDS_BY_GRADE)) {
                
    		stmt.setInt(1, gradeLevel);
    		
    		ArrayList<Integer> studentIDs = new ArrayList<Integer>();
                
            try (ResultSet rs = stmt.executeQuery()) {            	   
         	   while (rs.next()) {
         		  studentIDs.add(rs.getInt("student_id"));
               }
            }
            return studentIDs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
	
	public static int studentRank(int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_FROM_STUDENT_ID)) {

           stmt.setInt(1, studentID);

           int classRank;
           
           try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
            	   classRank = rs.getInt("class_rank");
                   return classRank;
               }
           }
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return -1;
    }
	
	public static double studentGPA(int studentID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_FROM_STUDENT_ID)) {

           stmt.setInt(1, studentID);

           double gpa;
           
           try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
            	   gpa = rs.getDouble("gpa");
                   return gpa;
               }
           }
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return -1;
    }
	
	public static ArrayList<String> courseNumbers() {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ALL_COURSE_NO)) {

	        ArrayList<String> courseNums = new ArrayList<String>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  courseNums.add(rs.getString("course_no"));
	           }
	        }
	        return courseNums;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
	
	public static ArrayList<String> studentCourses(User student) {
		try (Connection conn = getConnection();
	      PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSES_FOR_STUDENT)) {

			stmt.setInt(1, ((Student) student).getStudentId());
			
			ArrayList<String> courses = new ArrayList<String>();
			   
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					courses.add(rs.getString("course_no"));
				}
			}
			return courses;
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }

	    return null;
	}
	
	public static ArrayList<String> studentCoursesByStudentID(int studentID) {
		try (Connection conn = getConnection();
	      PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSES_FOR_STUDENT)) {

			stmt.setInt(1, studentID);
			
			ArrayList<String> courses = new ArrayList<String>();
			   
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					courses.add(rs.getString("course_no"));
				}
			}
			return courses;
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }

	    return null;
	}
	
	public static ArrayList<String> getCourseTitlesFromCourseNo(ArrayList<String> courseNo) {
	    try (Connection conn = getConnection();
	        PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID)) {
	    	
	    	ArrayList<String> courseTitles = new ArrayList<String>();
	    	
	    	for (int i = 0; i < courseNo.size(); i++) {
	    		String tempCourse = courseNo.get(i);
	    		stmt.setString(1, tempCourse);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						courseTitles.add(rs.getString("title"));
					}
				}
	    	}
			return courseTitles;
	    } catch (SQLException e) {
	   		e.printStackTrace();
	    }

	    return null;
	    
	}
	
	public static ArrayList<Integer> getCourseIDsFromCourseNo(ArrayList<String> courseNo) {
	    try (Connection conn = getConnection();
	        PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID)) {
	    	
	    	ArrayList<Integer> courseIDs = new ArrayList<Integer>();
	    	
	    	for (int i = 0; i < courseNo.size(); i++) {
	    		String tempCourse = courseNo.get(i);
	    		stmt.setString(1, tempCourse);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						courseIDs.add(Integer.parseInt(rs.getString("course_ID")));
					}
				}
	    	}
			return courseIDs;
	    } catch (SQLException e) {
	   		e.printStackTrace();
	    }
	    
	    return null;   
	}
	
	public static ArrayList<Double> getCourseGrades(ArrayList<Integer> courseID, int studentID) {
	    try (Connection conn = getConnection();
		        PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_GRADES)) {
		    	
		    	ArrayList<Double> courseGrades = new ArrayList<Double>();
		    	
		    	for (int i = 0; i < courseID.size(); i++) {
		    		int tempID = courseID.get(i);
		    		stmt.setInt(1, tempID);
		    		stmt.setInt(2, studentID);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {	
							courseGrades.add(Double.parseDouble(rs.getString("grade")));
						}
					}
		    	}
				return courseGrades;
		    } catch (SQLException e) {
		   		e.printStackTrace();
		    }
		return null;
	}
	
	public static void updateCourseGrades(int courseID, int studentID, ArrayList<Double> grades) {
		try (Connection conn = getConnection();
				PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_COURSE_GRADES)) {
				
				for (int i = 0; i < grades.size(); i++) {
					if (grades.get(i) == null) {
						grades.set(i, -1.00);
					}
					stmt.setDouble(i + 1, grades.get(i));
				}
				
				stmt.setInt(grades.size() + 1, courseID);
				stmt.setInt(grades.size() + 2, studentID);
				stmt.executeUpdate();
				
	    } catch (SQLException e) {
	   		e.printStackTrace();
	    }
	}
	
	public static ArrayList<Double> getFinalCourseGrades(int studentID) {
		try (Connection conn = getConnection();
				PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_FINAL_GRADES)) {
			
			ArrayList<Double> finalGrades = new ArrayList<Double>();
			
			stmt.setInt(1, studentID);
			
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {	
					finalGrades.add(rs.getDouble("grade"));
				}
				return finalGrades;
			}
			
	    } catch (SQLException e) {
	   		e.printStackTrace();
	    }
		return null;
	}
	
	public static void updateGPA(double GPA, int studentID) {
		try (Connection conn = getConnection();
				PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_GPA)) {
				
				stmt.setDouble(1, GPA);
				stmt.setInt(2, studentID);
				stmt.executeUpdate();
				
	    } catch (SQLException e) {
	   		e.printStackTrace();
	    }
	}
	public static String assignmentNameByID(int assignment_id) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_ID)) {

	        stmt.setInt(1, assignment_id);
	        
	        String name = null;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   if (rs.next()) {
	     		  name = rs.getString("title");
	           }
	        }
	        return name;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
	
	public static int assignmentValueByID(int assignment_id) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_ID)) {

	        stmt.setInt(1, assignment_id);
	        
	        int value = -1;
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   if (rs.next()) {
	     		  value = rs.getInt("point_value");
	           }
	        }
	        return value;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return -1;
    }
	
	public static ArrayList<Integer> assignmentIDByMP(int courseID, int mp) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MP)) {

		        stmt.setInt(1, courseID);
		        stmt.setInt(2, mp);
		        
		        ArrayList<Integer> assignments = new ArrayList<Integer>();
		        
		        try (ResultSet rs = stmt.executeQuery()) {            	   
		     	   while (rs.next()) {
		     		  assignments.add(rs.getInt("assignment_id"));
		            }
		        }
		        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public static ArrayList<Integer> assignmentIDsByMid(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MID)) {

	        stmt.setInt(1, courseID);
	        
	        ArrayList<Integer> assignments = new ArrayList<Integer>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  assignments.add(rs.getInt("assignment_id"));
	           }
	        }
	        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    public static ArrayList<Integer> assignmentIDsByFin(int courseID) {
    	try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_FIN)) {

	        stmt.setInt(1, courseID);
	        
	        ArrayList<Integer> assignments = new ArrayList<Integer>();
	        
	        try (ResultSet rs = stmt.executeQuery()) {            	   
	     	   while (rs.next()) {
	     		  assignments.add(rs.getInt("assignment_id"));
	           }
	        }
	        return assignments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    public static void updateClassRank(int studentID, int rank) {
    			try (Connection conn = getConnection();
    		      PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_RANKING)) {
    		
    				stmt.setDouble(1, rank);
    		 		stmt.setInt(2, studentID);
    		
    		 		stmt.executeUpdate();
    		     } catch (SQLException e) {
    		         e.printStackTrace();
    		     }
    }
}