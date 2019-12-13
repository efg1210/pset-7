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
                    	System.out.println("Password updated.");
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
        		case 1: viewEnrollmentCourse(); break;
        		case 2: 
        		case 3: 
        		case 4: 
        		case 5: 
        		case 6: return false;
        		default: System.out.println("Invalid selection. Please do it again.");
        	}
    	}
    }
    
    private void viewEnrollmentCourse() {
    	if (activeUser.isTeacher()) {
    		System.out.println("\nChoose a course.\n");
    		ArrayList<String> courses = PowerSchool.teacherCourses(activeUser);
    		for (int i = 0; i < courses.size(); i++) {
        		System.out.println(courses.get(i));
    		}
    	} else if (activeUser.isAdministrator()) {
    		
    	} else {
    		System.out.println("A student or root user is viewing enrollment by course, which is bad. Fix it.");
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