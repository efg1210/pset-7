package com.apcsa.controller; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import com.apcsa.data.*;
import com.apcsa.model.*;

public class Application {

    private Scanner in;
    private User activeUser;

    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */

    public Application() {
        this.in = new Scanner(System.in);

        try {
        	PowerSchool.initialize(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the PowerSchool application.
     */

    public void startup() {
        System.out.println("PowerSchool -- now for students, teachers, and school administrators!");

        // continuously prompt for login credentials and attempt to login

        while (true) {
            System.out.print("\nUsername: ");
            String username = in.next();

            System.out.print("Password: ");
            String password = in.next();

            // if login is successful, update generic user to administrator, teacher, or student

            if (login(username, password)) {
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;
                    
                if (isFirstLogin() && !activeUser.isRoot()) {
                	// first-time users need to change their passwords from the default provided
                	
                	System.out.print("\nPlease change your password: ");
                    String newPassword = Utils.getHash(in.next());
                    String oldPassword = activeUser.getPassword();
                    
                    if(!newPassword.equals(oldPassword)) {
                    	System.out.println("\nPassword updated.");
                        activeUser.setPassword(newPassword);
                        PowerSchool.updatePassword(username, activeUser.getPassword());
                    }
                                        
                }
                
                System.out.println("");
                if (activeUser.isAdministrator()) {
                	admin();
                } else if (activeUser.isTeacher()) {
                	teacher();
                } else if (activeUser.isStudent()) {
                	student();
                } else if (activeUser.isRoot()) {
                	root();
                }

                // create and show the user interface
                //
                // remember, the interface will be difference depending on the type
                // of user that is logged in (root, administrator, teacher, student)
            } else {
                System.out.println("\nInvalid username and/or password.");
            }
        }
    }
    
    private void admin() {
    	System.out.println("admin");
    }

    private boolean teacher() {
    	System.out.println("Hello, again, " + activeUser.getFirstName() +"!");
    	
    	while (true) {
    		System.out.println("\n[1] View enrollment by course.");
        	System.out.println("[2] Add assignment.");
        	System.out.println("[3] Delete assignment.");
        	System.out.println("[4] Enter grade.");
        	System.out.println("[5] Change password.");
        	System.out.print("[6] Logout.\n\n::: ");

        	int selection = Utils.getInt(in, 7);
        	
        	switch (selection) {
        		case 1: enrollmentByCourse(); break;
        		case 2: addAssign(); break;
        		case 3: deleteAssignment(); break;
        		case 4: 
        		case 5: 
        		case 6: return false;
        		default: System.out.println("Invalid selection. Please do it again.");
        	}
    	}
    }
    
    private int assignments() {
    	ArrayList<String> courses = viewCourse();
    	System.out.print("\n::: ");
    	
    	int selection = Utils.getInt(in, courses.size());
    	if (selection <= courses.size()) {
    		int courseID = PowerSchool.courseID(courses.get(selection - 1));
    		
    		System.out.println("\nChoose a marking period or exam status.\n");
    		System.out.println("[1] MP1 assignment.");
    		System.out.println("[2] MP2 assignment.");
    		System.out.println("[3] MP3 assignment.");
    		System.out.println("[4] MP4 assignment.");
    		System.out.println("[5] Midterm exam.");
    		System.out.println("[6] Final exam.");
        	System.out.print("\n::: ");
        	return courseID;
    	} else {
    		System.out.println("Invalid selection. Try again.");
    	}
    	return 0;
    }
    
    private void deleteAssignment() {
    	int courseID = assignments();
    	int markingPeriod = Utils.getInt(in, 7);
    	
    	if (markingPeriod < 6) {
    		System.out.println("Choose an assignment.\n");
        	ArrayList<String> assignments = new ArrayList<String>();
        	ArrayList<Integer> values = new ArrayList<Integer>();
        	if (markingPeriod <= 4) {
        		assignments = PowerSchool.assignmentNameByMP(courseID, markingPeriod);
        		values = PowerSchool.assignmentValuesByMP(courseID, markingPeriod);
        		for (int i = 0; i < assignments.size(); i++) {
        			System.out.print("[" + (i + 1) + "] " + assignments.get(i));
        			
        			System.out.println(" (" + values.get(i) + ")");
        		}
            	
        		System.out.print("\n::: ");
        		
        		int assignmentNumber = Utils.getInt(in, assignments.size());
        		
        		int assignmentID = PowerSchool.assignmentIDByName(assignments.get(assignmentNumber - 1));
        		if (assignmentID < assignments.size()) {
        			PowerSchool.deleteAssignment(assignmentID);
        		}
        	} else if (markingPeriod == 5) {
        		System.out.println("\nMIDTERM");
        	} else if (markingPeriod == 6) {
        		System.out.println("\nFINAL");
        	} else {
        		System.out.println("\nInvalid selection. Try again.");
        	}
    	} else {
    		System.out.println("\nInvalid selection. Try again.");
    	}
    }
    
    private void addAssign() {
    	int courseID = assignments();
    	int selection = Utils.getInt(in, 7);
    	if (selection > 6) {
    		System.out.println("\nInvalid selection. Try again.");
    	} else {
	    	System.out.print("\nAssignment Title: ");
			String title = in.nextLine();
			
			System.out.print("Point Value: ");
			
			int pointValue = Utils.getInt(in, 100);
			while (pointValue < 1 || pointValue > 100) {
				System.out.print("\nPoint values must be between 1 and 100.\n\nPoint Value: ");
				pointValue = Utils.getInt(in, 100);
			}
			
			System.out.print("\nAre you sure you want to create this assignment? (y/n) ");
			String agreement = in.nextLine().toLowerCase();
			
			int assignmentID = PowerSchool.getLastAssignID() + 1;
			
			if (agreement.equals("y")) {
				if (selection <= 4) {
					PowerSchool.addAssignment(courseID, assignmentID, (int) selection, 0, 0, title, pointValue);
				} else if (selection == 5) {
	    			PowerSchool.addAssignment(courseID, assignmentID, 0, 1, 0, title, pointValue);
				} else if (selection == 6) {
	    			PowerSchool.addAssignment(courseID, assignmentID, 0, 0, 1, title, pointValue);
				}
				
				System.out.println("\nSuccessfully created assignment.");
			}
	    	
    	}
    }
    
    private void enrollmentByCourse() {
    	ArrayList<String> courses = viewCourse();
    	System.out.print("\n::: ");
    	
    	int selection = Utils.getInt(in, courses.size());
    	if (selection <= courses.size()) {
    		int courseID = PowerSchool.courseID(courses.get(selection - 1));    		
    		
    		ArrayList<Integer> studentIDs = PowerSchool.studentIDByCourse(courseID);
    		System.out.println("");
    		
    		ArrayList<String> studentFirstName = new ArrayList<String>();
    		ArrayList<String> studentLastName = new ArrayList<String>();
    		
    		for (int i = 0; i < studentIDs.size(); i++) {
    			//System.out.println("id: " + studentIDs.get(i));
    			
    			studentFirstName.add(PowerSchool.studentFirstName(studentIDs.get(i)));
    			studentLastName.add(PowerSchool.studentLastName(studentIDs.get(i)));
    			
    			System.out.print((i + 1) + ". " + studentLastName.get(i) + ", ");
    			System.out.print(studentFirstName.get(i) + " / ");
    			
    			//doesn't work because there are no grades so far?
    			if (activeUser.isAdministrator()) {
    				
    			} else if (activeUser.isTeacher()) {
    				double grade = PowerSchool.courseGrade(courseID, studentIDs.get(i));
    				System.out.println(grade);
    			}
    		}
    		
    	} else {
    		System.out.println("Invalid selection. Try again.");
    	}
    	
    	
    }
    
    private ArrayList<String> viewCourse() {
    	if (activeUser.isTeacher()) {
    		System.out.println("\nChoose a course.");
    		ArrayList<String> courses = PowerSchool.teacherCourses(activeUser);
    		System.out.println("");
    		for (int i = 0; i < courses.size(); i++) {
        		System.out.println("[" + (i + 1) + "] " + courses.get(i));
    		}
    		return courses;
    	} else if (activeUser.isAdministrator()) {
    		return null;
    	} else {
    		System.out.println("A student or root user is viewing enrollment by course, which is bad. Fix it.");
    		return null;
    	}
    }
    
    
    
    private void student() {
    	System.out.println("student");
    }
    
    private void root() {
    	System.out.println("root");
    }

    /**
     * Logs in with the provided credentials.
     *
     * @param username the username for the requested account
     * @param password the password for the requested account
     * @return true if the credentials were valid; false otherwise
     */

    public boolean login(String username, String password) {
        activeUser = PowerSchool.login(username, password);

        return activeUser != null;
    }

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
        return activeUser.getLastLogin().equals("0000-00-00 00:00:00.000");
    }

    /////// MAIN METHOD ///////////////////////////////////////////////////////////////////

    /*
     * Starts the PowerSchool application.
     *
     * @param args unused command line argument list
     */

    public static void main(String[] args) {
        Application app = new Application();

        app.startup();
    }
}