package com.apcsa.controller; 

import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.User;

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
                
                if (activeUser.isAdministrator()) {
                	admin(activeUser);
                } else if (activeUser.isTeacher()) {
                	teacher(activeUser);
                } else if (activeUser.isStudent()) {
                	student(activeUser);
                } else if (activeUser.isRoot()) {
                	root(activeUser);
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
    
    private void admin(User activeUser) {
    	System.out.println("admin");
    	/* Needs:
    	 * Change Password (shared with teacher / student)
    	 * Logout of Account (shared with all)
    	 * View enrollment
    	 * View enrollment by grade
    	 * View enrollment by course (shared with teacher)
    	 * View faculty
    	 * View faculty by department
    	 */
    }

    private void teacher(User activeUser) {
    	System.out.println("teacher");
    	/* Needs:
    	 * Change password (shared with student / admin)
    	 * Logout of account (shared with all)
    	 * View enrollment by course (shared with admin)
    	 * Add assignment
    	 * Delete assignment
    	 * Enter grade for assignment
    	 */
    }

    
    private void student(User activeUser) {
    	System.out.println("student");
    	/* Needs:
    	 * View course grades
    	 * View assignment grades by course
    	 * Change password (shared with teacher / admin)
    	 * Logout of account (shared with all)
    	 */
    }
    
    private void root(User activeUser) {
    	System.out.println("\nHello, again, Root!\n");
        boolean validLogin = true;
        while (validLogin) {
            final int RESET_PASSWORD = 1;
            final int RESET_DATABASE = 2;
            final int LOGOUT = 3;
            final int SHUTDOWN = 4;
        	
            switch (getSelectionRoot()) {
                case RESET_PASSWORD: resetPassword(); break;
                case RESET_DATABASE: resetDatabase(); break;
                case LOGOUT: validLogin = false; break;
                case SHUTDOWN: shutdown();
                default: System.out.println("\nInvalid selection.\n"); break;
            }
        }
    	/* Needs:
    	 * Logout of account (shared with all)
    	 * Reset password FOR ANOTHER ACCOUNT
    	 * Factory reset database
    	 * Invalid selection needs to disappear for aborted shutdown
    	 */
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
    
    
    /**
     * Shutdowns application.
     */
    
    public int getSelectionRoot() {
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");
        
        return in.nextInt();
    }
    
    public void resetPassword() {
    	System.out.println("Reset password");
    	
    	System.out.print("Username:");
    	String userID = in.nextLine();
    	in.nextLine();
    	String confirmation = "";
    	if ("userID == a userID within database") {
    		do {
        		System.out.println("\n Are you sure you want to reset the password for " + userID + "? (y/n)");
        		confirmation = in.nextLine();
        		in.nextLine();
	    		if (confirmation.equals("y") || confirmation.equals("Y")) {
	    			// PowerSchool.resetPassword(, userID, "0000-00-00 00:00:00.000")
	    			System.out.println("Successfully reset password for " + userID + ".");
	            } else if (confirmation.equals("n") || confirmation.equals("N")) {
	            	System.out.println("Password reset aborted.");
	            }
    		} while (!confirmation.equals("y") && !confirmation.equals("n") && !confirmation.equals("Y") && !confirmation.equals("N"));
    	}
    }
    
    public void resetDatabase() {
    	System.out.println("Reset database");
    }
     
    public void shutdown() {
    	in.nextLine();
        System.out.println("Are you sure? (y/n)");
        String confirmation = in.nextLine();
        
        if (confirmation.equals("y")) {
        	if (in != null) {
                in.close();
            }
            
            System.out.println("\nGoodbye!");
            System.exit(0);
        } else if (confirmation.equals("n")) {
        	System.out.println("Shutdown aborted.");
        } else {
        	System.out.println("Invalid input. Shutdown aborted.");
        }
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