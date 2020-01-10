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
import java.util.Collections;

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
            	//calcRanking();
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
                    
                    if (!newPassword.equals(oldPassword)) {
                    	System.out.println("\nSuccessfully changed password.");
                        activeUser.setPassword(newPassword);
                        PowerSchool.updatePassword(username, activeUser.getPassword());
                    } else {
                    	System.out.println("\nOld password and new password cannot match.");
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
    
    private void calcRanking() {
    	for (int gradeLevel = 9; gradeLevel < 13; gradeLevel++) {
    		ArrayList<Integer> studentIDs = PowerSchool.studentsByGrade(gradeLevel);
    		
    		for (int i = 0; i < studentIDs.size(); i++) {
    			int counter = 1;
    			double currentGPA = PowerSchool.studentGPA(studentIDs.get(i));
    			
    			for (int j = 0; j < studentIDs.size(); j++) {
    				double compareGPA = PowerSchool.studentGPA(studentIDs.get(j));
    				
    				if (studentIDs.get(i) != studentIDs.get(j)) {
    					
    					if (compareGPA > currentGPA) {
    						counter++;
    					}
    				}
    			}
    			PowerSchool.updateClassRank(studentIDs.get(i), counter);
    		}
    	}
    }
    
    private boolean logout(boolean factoryReset) {
    	if (factoryReset) {
    		return true;
    	} else {
    		System.out.println("");
    		return Utils.confirm(in, "Are you sure you want to logout? (y/n) ");
    	}
    }
    
    private void changePassword() {
    	System.out.print("\nEnter current password: ");
        String oldPassword = Utils.getHash(in.next());
        System.out.print("Enter new password: ");
        String newPassword = Utils.getHash(in.next());
        
        if (oldPassword.equals(activeUser.getPassword())) {
        	if (!newPassword.equals(oldPassword)) {
            	System.out.println("\nSuccessfully changed password.");
                activeUser.setPassword(newPassword);
                PowerSchool.updatePassword(activeUser.getUsername(), activeUser.getPassword());
            } else {
            	System.out.println("\nOld password and new password cannot match.");
            }
        } else {
        	System.out.println("\nInvalid current password.");
        }
    }
    
	private boolean admin() {
		/* Needs:
		 * Change Password (shared with teacher / student)
		 * Logout of Account (shared with all)
		 * View enrollment
		 * View enrollment by grade
		 * View enrollment by course (shared with teacher)
		 * View faculty
		 * View faculty by department
		 */
		System.out.println("Hello, again, " + activeUser.getFirstName() +"!");
		
		while (true) {
			System.out.println("\n[1] View faculty.");
			System.out.println("[2] View faculty by department.");
			System.out.println("[3] View student enrollment.");
			System.out.println("[4] View student enrollment by grade.");
			System.out.println("[5] View student enrollment by course.");
			System.out.println("[6] Change password.");
			System.out.print("[7] Logout.\n\n::: ");
			
			int selection = Utils.getInt(in, 8);
			
			switch (selection) {
				case 1: viewFaculty(); break;
				case 2: facultyByDept(); break;
				case 3: enrollment(); break;
				case 4: classEnrollment(); break;
				case 5: enrollmentByCourse(); break;
				case 6: changePassword(); break;
				case 7: 
					if (logout(false)) {
                		return false;
					}
                	break;
				default: System.out.println("\nInvalid selection.");
			}
		}
	}
	
	private void classEnrollment() {
		System.out.println("\nChoose a grade level.\n");
		System.out.println("[1] Freshman.");
		System.out.println("[2] Sophomore.");
		System.out.println("[3] Junior.");
		System.out.println("[4] Senior.");
		
		System.out.print("\n::: ");
		
		int gradeLevel = Utils.getInt(in, 5);
		while (gradeLevel >= 5) {
    		System.out.print("::: ");
    		gradeLevel = Utils.getInt(in, 5);
    	}
		
		gradeLevel += 8;
		
		ArrayList<Integer> studentIDs = PowerSchool.studentsByGrade(gradeLevel);
		ArrayList<String> studentMessage = new ArrayList<String>();

		System.out.println("");
		
		for (int i = 0; i < studentIDs.size(); i++) {
			String tempMessage = "";
			tempMessage += (PowerSchool.studentLastName(studentIDs.get(i)) + ", ");
			tempMessage += (PowerSchool.studentFirstName(studentIDs.get(i)) + " / #");
			tempMessage += PowerSchool.studentRank(studentIDs.get(i));
			studentMessage.add(tempMessage);
		}
		
		if (studentIDs.size() > 0) {
			Collections.sort(studentMessage);
			for (int i = 0; i < studentMessage.size(); i++) {
				System.out.print((i + 1) + ". ");
				System.out.println(studentMessage.get(i));
			}
		} else {
			System.out.println("There are no students from this grade.");
		}
		
	}
	
	private void enrollment() {
		ArrayList<Integer> studentIDs = PowerSchool.studentIDs();
		ArrayList<String> studentMessage = new ArrayList<String>();

		System.out.println("");
		for (int i = 0; i < studentIDs.size(); i++) {
			String tempMessage = "";
			tempMessage += (PowerSchool.studentLastName(studentIDs.get(i)) + ", ");
			tempMessage += (PowerSchool.studentFirstName(studentIDs.get(i)) + " / ");
			tempMessage += PowerSchool.studentGradYear(studentIDs.get(i));
			studentMessage.add(tempMessage);
		}
		Collections.sort(studentMessage);
		for (int i = 0; i < studentMessage.size(); i++) {
			System.out.print((i + 1) + ". ");
			System.out.println(studentMessage.get(i));
		}
	}
 
	private void facultyByDept() {
		ArrayList<Integer> deptIDs = PowerSchool.deptIDs();
		System.out.println("\nChoose a department.\n");
		for (int i = 0; i < deptIDs.size(); i++) {
			System.out.print("[" + (i + 1) + "] ");
			System.out.println(PowerSchool.depByID(deptIDs.get(i)));
		}
		
		System.out.print("\n::: ");
		int deptIdIndex = Utils.getInt(in, deptIDs.size()) - 1;
		while (deptIdIndex >= deptIDs.size()) {
    		System.out.print("::: ");
    		deptIdIndex = Utils.getInt(in, deptIDs.size()) - 1;
    	}
		
		int deptID = deptIDs.get(deptIdIndex);
		
		ArrayList<Integer> teacherIDs = PowerSchool.teachersByDept(deptID);
		for (int i = 0; i < teacherIDs.size(); i++) {
			System.out.print("\n" + (i + 1) + ". ");
			System.out.print(PowerSchool.teacherLastName(teacherIDs.get(i)) + ", ");
			System.out.print(PowerSchool.teacherFirstName(teacherIDs.get(i)) + " / ");
			System.out.println(PowerSchool.depByID(deptID));
		}
	}
	
	private void viewFaculty() {
		ArrayList<Integer> teacherIDs = PowerSchool.teacherIDs();
		ArrayList<String> teacherMessage = new ArrayList<String>();

		System.out.println("");
		for (int i = 0; i < teacherIDs.size(); i++) {
			String tempMessage = "";
			tempMessage += (PowerSchool.teacherLastName(teacherIDs.get(i)) + ", ");
			tempMessage += (PowerSchool.teacherFirstName(teacherIDs.get(i)) + " / ");
			int depID = PowerSchool.teacherDepartmentID(teacherIDs.get(i));
			tempMessage += (PowerSchool.depByID(depID));
			teacherMessage.add(tempMessage);
		}
		Collections.sort(teacherMessage);
		for (int i = 0; i < teacherMessage.size(); i++) {
			System.out.print((i + 1) + ". ");
			System.out.println(teacherMessage.get(i));
		}
	}

    private boolean teacher() {
      /* Needs:
    	 * Change password (shared with student / admin)
    	 * Logout of account (shared with all)
    	 * View enrollment by course (shared with admin)
    	 * Add assignment
    	 * Delete assignment
    	 * Enter grade for assignment
    	 */
  
    	System.out.println("Hello, again, " + activeUser.getFirstName() +"!");
    	
    	while (true) {
    		System.out.println("\n[1] View enrollment by course.");
        	System.out.println("[2] Add assignment.");
        	System.out.println("[3] Delete assignment.");
        	System.out.println("[4] Enter grade.");
        	System.out.println("[5] Change password.");
        	System.out.println("[6] Easter egg.");
        	System.out.print("[7] Logout.\n\n::: ");
        
        	int selection = Utils.getInt(in, 8);
        	
        	switch (selection) {
        		case 1: enrollmentByCourse(); break;
        		case 2: addAssign(); break;
        		case 3: deleteAssignment(); break;
        		case 4: enterGrade(); break;
        		case 5: changePassword(); break;
        		case 6: easterEgg(((Teacher) activeUser).getTeacherId()); break;
        		case 7:
        			if (logout(false)) {
                		return false;
					}
        			break;
        		default: System.out.println("\nInvalid selection.");
        	}
    	}
    }
    
    private void enterGrade() {
    	int courseID = assignments();
    	int markingPeriod = Utils.getInt(in, 7);
    	
    	while (markingPeriod > 6) {
    		System.out.print("::: ");
    		markingPeriod = Utils.getInt(in, 7);
    	}
    	
    	ArrayList<Integer> values = new ArrayList<Integer>();
    	ArrayList<Integer> assignmentIDs = new ArrayList<Integer>();
    	ArrayList<Integer> studentIDs = PowerSchool.studentIDByCourse(courseID);
    	
    	if (markingPeriod <= 4) {
    		assignmentIDs = PowerSchool.assignmentIDByMP(courseID, markingPeriod);
    	} else if (markingPeriod == 5) {
    		assignmentIDs = PowerSchool.assignmentIDsByMid(courseID);
    	} else if (markingPeriod == 6) {
    		assignmentIDs = PowerSchool.assignmentIDsByFin(courseID);
    	}
    	
    	for (int i = 0; i < assignmentIDs.size(); i++) {
    		values.add(PowerSchool.assignmentValueByID(assignmentIDs.get(i)));
    	}
    	
    	if (assignmentIDs.size() > 0  && studentIDs.size() > 0) {
			System.out.println("\nChoose an assignment.\n");
			for (int i = 0; i < assignmentIDs.size(); i++) {
				System.out.print("[" + (i + 1) + "] " + PowerSchool.assignmentNameByID(assignmentIDs.get(i)));
				System.out.println(" (" + values.get(i) + ")");
			}
			
			System.out.print("\n::: ");
			int assignmentIndex = Utils.getInt(in, assignmentIDs.size()) - 1;
			while (assignmentIndex >= assignmentIDs.size()) {
				System.out.print("::: ");
				assignmentIndex = Utils.getInt(in, assignmentIDs.size()) - 1;
			}
			
			int chosenAssignmentID = assignmentIDs.get(assignmentIndex);
			String chosenAssignmentTitle = PowerSchool.assignmentNameByID(chosenAssignmentID);
			int chosenAssignmentValue = PowerSchool.assignmentValueByID(chosenAssignmentID);
			
			System.out.println("");
			
			ArrayList<String> studentFirstName = new ArrayList<String>();
			ArrayList<String> studentLastName = new ArrayList<String>();
			
			for (int i = 0; i < studentIDs.size(); i++) {
				
				studentFirstName.add(PowerSchool.studentFirstName(studentIDs.get(i)));
				studentLastName.add(PowerSchool.studentLastName(studentIDs.get(i)));
				
				System.out.print((i + 1) + ". " + studentLastName.get(i) + ", ");
				System.out.println(studentFirstName.get(i));
			}
			
			System.out.print("\n::: ");
			int studentIndex = Utils.getInt(in, studentIDs.size()) - 1;
			while (studentIndex >= studentIDs.size()) {
				System.out.print("::: ");
				studentIndex = Utils.getInt(in, studentIDs.size()) - 1;
			}
			
			System.out.println("\nAssignment: " + chosenAssignmentTitle);
			System.out.print("Student: " + studentLastName.get(studentIndex));
			System.out.println(", " + studentFirstName.get(studentIndex));
			double grade = PowerSchool.assignmentGrade(chosenAssignmentID, studentIDs.get(studentIndex));
			if (grade == -1) {
				System.out.println("Current Grade: --");
			} else {
				System.out.println("Current Grade: " + grade);
			}
			System.out.print("\nNew Grade: ");
			
			int newGrade = Utils.getInt(in, chosenAssignmentValue + 1);
			while (newGrade > chosenAssignmentValue) {
				System.out.print("New Grade: ");
				newGrade = Utils.getInt(in, chosenAssignmentValue + 1);
			}
			
			boolean confirm = Utils.confirm(in, "\nAre you sure you want to enter this grade? (y/n) ");
			
			if (confirm && grade == -1) {
				PowerSchool.addAssignmentGrade(courseID, chosenAssignmentID, studentIDs.get(studentIndex), 
				newGrade, chosenAssignmentValue, 1);
				System.out.println("\nSuccessfully entered grade.");
				updateGrades(studentIDs.get(studentIndex));
				calcRanking();
			} else if (confirm && grade != -1) {
				PowerSchool.updateAssignmentGrade(courseID, chosenAssignmentID, studentIDs.get(studentIndex), 
				  newGrade, 1);
				System.out.println("\nSuccessfully entered grade.");
				updateGrades(studentIDs.get(studentIndex));
				calcRanking();
			} else {
				System.out.println("\nGrade not entered.");
			}
    	} else if (assignmentIDs.size() == 0) {
    		System.out.println("\nThere are no assignments.");
    	} else if (studentIDs.size() == 0) {
    		System.out.println("\nThere are no students for this course.");
    	}
    }
    
    private int assignments() {
    	ArrayList<String> courses = viewCourse();
    	System.out.print("\n::: ");
    	
    	int selection = Utils.getInt(in, courses.size());
    	
    	while (selection > courses.size()) {
    		System.out.print("::: ");
    		selection = Utils.getInt(in, courses.size());
    	}
    	
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
    }
    
    private void deleteAssignment() {
    	int courseID = assignments();
    	int markingPeriod = Utils.getInt(in, 7);
    	
    	while (markingPeriod > 6) {
    		System.out.print("::: ");
    		markingPeriod = Utils.getInt(in, 7);
    	}
    	
    	ArrayList<Integer> values = new ArrayList<Integer>();
    	ArrayList<Integer> assignmentIDs = new ArrayList<Integer>();
    	
    	if (markingPeriod <= 4) {
    		assignmentIDs = PowerSchool.assignmentIDByMP(courseID, markingPeriod);
    	} else if (markingPeriod == 5) {
    		assignmentIDs = PowerSchool.assignmentIDsByMid(courseID);
    	} else if (markingPeriod == 6) {
    		assignmentIDs = PowerSchool.assignmentIDsByFin(courseID);
    	}
    	
    	if (assignmentIDs.size() > 0) {
    		System.out.println("\nChoose an assignment.\n");
    		for (int i = 0; i < assignmentIDs.size(); i++) {
        		values.add(PowerSchool.assignmentValueByID(assignmentIDs.get(i)));
        	}
        	
        	for (int i = 0; i < assignmentIDs.size(); i++) {
    			System.out.print("[" + (i + 1) + "] " + PowerSchool.assignmentNameByID(assignmentIDs.get(i)));
    			System.out.println(" (" + values.get(i) + ")");
    		}
        	    	
        	System.out.print("\n::: ");
    		int assignmentIndex = Utils.getInt(in, assignmentIDs.size()) - 1;
    		while (assignmentIndex >= assignmentIDs.size()) {
    			System.out.print("::: ");
    			assignmentIndex = Utils.getInt(in, assignmentIDs.size()) - 1;
    		}
    		
    		int chosenAssignmentID = assignmentIDs.get(assignmentIndex);
    		String chosenAssignmentTitle = PowerSchool.assignmentNameByID(chosenAssignmentID);
    				
    		System.out.print("\nAre you sure you want to delete this assignment? (y/n) ");
    		String agreement = in.nextLine().toLowerCase();
    		while (!agreement.equals("y") && !agreement.equals("n")) {
    			System.out.print("Are you sure you want to delete this assignment? (y/n) ");
    			agreement = in.nextLine().toLowerCase();
    		}
    		
    		if (agreement.equals("y")) {
    			PowerSchool.deleteGrades(chosenAssignmentID);
    			PowerSchool.deleteAssignment(chosenAssignmentID);
    			System.out.println("\nSuccessfully deleted " + chosenAssignmentTitle + ".");
    		} else {
    			System.out.println("\nAssignment not deleted.");
    		}
    	} else {
    		System.out.println("\nThere are no assignments here.");
    	}
    }
    
    private void addAssign() {
    	int courseID = assignments();
    	int selection = Utils.getInt(in, 7);
    	
    	while (selection > 6) {
    		System.out.print("::: ");
    		selection = Utils.getInt(in, 7);
    	}
    	
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
		while (!agreement.equals("y") && !agreement.equals("n")) {
			System.out.print("Are you sure you want to delete this assignment? (y/n) ");
			agreement = in.nextLine().toLowerCase();
		}
		
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
		} else {
			System.out.println("\nAssignment not created.");
		}
    }
    
    private void easterEgg(int teacherID) {
    	if (teacherID == 1) {
    		System.out.println("");
    		
    		System.out.println("   +--------------+");
    		System.out.println("   |.------------.|");
    		System.out.println("   ||            ||");
    		System.out.println("   ||            ||");
    		System.out.println("   ||            ||");
    		System.out.println("   ||            ||");
    		System.out.println("   |+------------+|");
    		System.out.println("   +-..--------..-+");
    		System.out.println("   .--------------.");
    		System.out.println("  / /============\\ \\");
    		System.out.println(" / /==============\\ \\");
    		System.out.println("/____________________\\");
    		System.out.println("\\____________________/");
    		
    		System.out.println("\nThis easter egg has been inspired by Nicole.");
    	} else if (teacherID == 2) {
    		System.out.println("");
    		
    		System.out.println("    __________________   __________________");
    		System.out.println(".-/|                  \\ /                  |\\-.");
    		System.out.println("||||                   |                   ||||");
    		System.out.println("||||                   |       ~~*~~       ||||");
    		System.out.println("||||    --==*==--      |                   ||||");
    		System.out.println("||||                   |                   ||||");
    		System.out.println("||||                   |                   ||||");
    		System.out.println("||||                   |     --==*==--     ||||");
    		System.out.println("||||                   |                   ||||");
    		System.out.println("||||                   |                   ||||");
    		System.out.println("||||                   |                   ||||");
    		System.out.println("||||                   |                   ||||");
    		System.out.println("||||__________________ | __________________||||");
    		System.out.println("||/===================\\|/===================\\||");
    		System.out.println("`--------------------~___~-------------------''");
    		
    		System.out.println("\nThis easter egg has been inspired by Nicole.");
    	} else if (teacherID == 3) {
    		System.out.println("");

    		System.out.println(" (");
    		System.out.println("(_)");
    		System.out.println("###       .");
    		System.out.println("(#c    __\\|/__");
    		System.out.println(" #\\     wWWWw");
    		System.out.println(" \\ \\-. (/. .\\)");
    		System.out.println(" /\\ /`\\/\\   /\\");
    		System.out.println(" |\\/   \\_) (_|");
    		System.out.println(" `\\.' ; ; `' ;`\\");
    		System.out.println("   `\\;  ;    .  ;/\\");
    		System.out.println("     `\\;    ;  ;|  \\");
    		System.out.println("      ;   .'  ' ;  /");
    		System.out.println("      |_.'   ;  | /)");
    		System.out.println("      (     ''._;/`");
    		System.out.println("      |    ' . ;");
    		System.out.println("      |.-'   .:)");
    		System.out.println("      |        |");
    		System.out.println("      (  .'  : |");
    		System.out.println("      |,-  .:: |");
    		System.out.println("      | ,-'  .;|");
    		System.out.println("     _/___,_.:_\\_");
    		System.out.println("    [I_I_I_I_I_I_]");
    		System.out.println("    | __________ |");
    		System.out.println("    | || |  | || |");
    		System.out.println("   _| ||_|__|_|| |_");
    		System.out.println("  /=--------------=\\");
    		System.out.println(" /                  \\");
    		System.out.println("|                    |");
    		
    		System.out.println("\nThis easter egg has been inspired by Nicole.");
     	} else if (teacherID == 6) {
     		System.out.println("");
     		
     		System.out.println("(==(     )==)");
     		System.out.println(" `-.`. ,',-'");
     		System.out.println("    _,-'\"");
     		System.out.println(" ,-',' `.`-.");
     		System.out.println("(==(     )==)");
     		System.out.println(" `-.`. ,',-'");
     		System.out.println("    _,-'\"");
     		System.out.println(" ,-',' `.`-.");
     		System.out.println("(==(     )==)");
     		System.out.println(" `-.`. ,',-'");
     		System.out.println("    _,-'\"");
     		System.out.println(" ,-',' `.`-.");
     		System.out.println("(==(     )==)");
     		
     		System.out.println("\nThis easter egg has been inspired by Nicole.");
     	} else if (teacherID == 4) {
     		System.out.println("");
     		
            System.out.println("   +------+.");
            System.out.println("   |`.    | `.");
            System.out.println("   |  `+--+---+");
            System.out.println("   |   |  |   |");
            System.out.println("   +---+--+   |");
            System.out.println("    `. |   `. |");
            System.out.println("      `+------+");
     		
     		System.out.println("\nThis easter egg has been inspired by Nicole.");
     	} else if (teacherID == 5) {
     		System.out.println("");
     		
     		System.out.println("          ___");
     		System.out.println("      .:::---:::.");
     		System.out.println("    .'--:     :--'.");
     		System.out.println("   /.'   \\   /   `.\\");
     		System.out.println("  | /'._ /:::\\ _.'\\ |");
     		System.out.println("  |/    |:::::|    \\|");
     		System.out.println("  |:\\ .''-:::-''. /:|");
     		System.out.println("   \\:|    `|`    |:/");
     		System.out.println("    '.'._.:::._.'.'");
     		System.out.println("      '-:::::::-'");
     		
     		System.out.println("\nThis easter egg has been inspired by Nicole.");
     	} else {
    		System.out.println("\nNo easter egg for you. :( Sorry.");
    	}
    }
    
    private boolean isValidCourseNo(String courseNo) {
    	ArrayList<String> courseNums = PowerSchool.courseNumbers();
    	for (int i = 0; i < courseNums.size(); i++) {
    		if (courseNums.get(i).equals(courseNo)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private void enrollmentByCourse() {
    	int courseID = 0;
    	
    	if (activeUser.isTeacher()) {
    		ArrayList<String> courses = viewCourse();
    		
    		System.out.print("\n::: ");
        	
           	int selection = Utils.getInt(in, courses.size());
        	
        	while (selection > courses.size()) {
        		System.out.print("::: ");
            	selection = Utils.getInt(in, courses.size());
        	}
        	courseID = PowerSchool.courseID(courses.get(selection - 1));    		
    	} else if (activeUser.isAdministrator()) {
    		System.out.print("\nCourse No.: ");
    		String courseNo = in.next();
    		
    		while (!isValidCourseNo(courseNo)) {
    			System.out.println("\nCourse not found.");
    			System.out.print("\nCourse No.: ");
    			courseNo = in.next();
    		}
    		
    		courseID = PowerSchool.courseID(courseNo);
    	} else {
    		System.out.println("\nHow did you get into view enrollment by course?");
    	}
    	
    	ArrayList<Integer> studentIDs = PowerSchool.studentIDByCourse(courseID);
		
    	if (studentIDs.size() > 0) {
    		ArrayList<String> studentMessage = new ArrayList<String>();

    		System.out.println("");
    		
    		for (int i = 0; i < studentIDs.size(); i++) {
    			String tempMessage = "";
    			tempMessage += (PowerSchool.studentLastName(studentIDs.get(i)) + ", ");
    			tempMessage += (PowerSchool.studentFirstName(studentIDs.get(i)) + " / ");
    			if (activeUser.isAdministrator()) {
    				double gpa = PowerSchool.studentGPA(studentIDs.get(i));
    				if (gpa < 0) {
    					tempMessage += "--";
    				} else {
    					tempMessage += gpa;
    				}
    			} else if (activeUser.isTeacher()) {
    				tempMessage += PowerSchool.courseGrade(courseID, studentIDs.get(i));
    			}
    			studentMessage.add(tempMessage);
    		}
    		Collections.sort(studentMessage);
    		for (int i = 0; i < studentMessage.size(); i++) {
    			System.out.print((i + 1) + ". ");
    			System.out.println(studentMessage.get(i));
    		}
		} else {
			System.out.println("\nThere are no students for this course.");
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
    	} else if (activeUser.isStudent()) {
    		System.out.println("\nChoose a course.");
    		ArrayList<String> courses = PowerSchool.studentCourses(activeUser);
    		System.out.println("");
    		for (int i = 0; i < courses.size(); i++) {
        		System.out.println("[" + (i + 1) + "] " + courses.get(i));
    		}
    		return courses;
    	} else if (activeUser.isAdministrator()) {
    		return null;
    	} else {
    		System.out.println("Root user is viewing enrollment by course, which is bad. Fix it.");
    		return null;
    	}
    }
    
    
    
    private void student() {
    	System.out.println("Hello, again, " + activeUser.getFirstName() + "!");
    	
    	//updateGrades();
    	/* Needs:
    	 * View course grades
    	 * View assignment grades by course
    	 * Change password (shared with teacher / admin)
    	 * Logout of account (shared with all)
    	 */
    	
        boolean validLogin = true;
        while (validLogin) {
            final int VIEW_COURSE_GRADES = 1;
            final int VIEW_ASSIGNMENT_GRADES = 2;
            final int CHANGE_PASSWORD = 3;
            final int LOGOUT = 4;
        	
            switch (getSelectionStudent()) {
                case VIEW_COURSE_GRADES: viewCourseGrades(); break;
                case VIEW_ASSIGNMENT_GRADES: viewAssignmentGrades(); break;
                case CHANGE_PASSWORD: changePassword(); break;
                case LOGOUT: 
                	if (logout(false)) {
                		validLogin = false;
					}
                	break;
                default: System.out.println("\nInvalid selection.\n"); break;
            }
        }
    	
    }
    
    public int getSelectionStudent() {
        System.out.println("\n[1] View course grades.");
        System.out.println("[2] View assignment grades by course.");
        System.out.println("[3] Change password.");
        System.out.print("[4] Logout.\n\n::: ");
        
        return in.nextInt();
    }
    
    public void updateGrades(int studentID) {
    	/* Get MP Grades
    	 * 	1. Find all assignments attributed to student. If none, mark as -1.
    	 *	2. Take points earned. Add.
    	 *	3. Take points possible. Add.
    	 *	4. Divide earned/possible. Multiple by 100. Round to 2 decimals.
    	 * Get Midterm / Final Exam Grades
    	 * Get Course Grade: Utils.getGrade(grades[]);
    	 */
    	    	
    	ArrayList<String> courses = PowerSchool.studentCoursesByStudentID(studentID);
    	ArrayList<Integer> courseIDs = PowerSchool.getCourseIDsFromCourseNo(courses);
    	
    	ArrayList<Integer> assignmentIDs = new ArrayList<Integer>();
    	ArrayList<Integer> pointsEarned = new ArrayList<Integer>();
    	ArrayList<Integer> pointsPossible = new ArrayList<Integer>();
    	ArrayList<Double> grades = new ArrayList<Double>();
    	
    	for (int i = 0; i < 7; i++) { //fills grades correctly so it doesn't catch fire
    		grades.add(i, null);
    	}
    	
    	double pointsPossibleSum = 0;
    	double pointsEarnedSum = 0;
    	double tempMPGrade = -1.00;
    	int examPointsEarned = 0;
    	int examPointsPossible = 0;
    	double examGrade = 0;
    	double midtermGrade = 0;
    	double finalExamGrade = 0;
    	int assignmentID = 0;
    	
    	for (int i = 0; i < courses.size(); i++) { //iterates through all courses
    		for (int j = 1; j <= 4; j++) { //iterates through all marking periods
    			assignmentIDs = PowerSchool.assignmentIDByMP(courseIDs.get(i), j);
            	
        		pointsPossible = PowerSchool.assignmentValuesByMP(courseIDs.get(i), j);
            	pointsEarned = PowerSchool.pointsEarnedByStudent(courseIDs.get(i), PowerSchool.getAssignmentIDByCourseIDAndStudentIDAndMarkingPeriod(courseIDs.get(i), studentID, j), studentID);
            	
            	if (assignmentIDs != null) {
	            	for (int k = 0; k < assignmentIDs.size(); k++) { //iterates through all assignments
	            		if (pointsEarned.get(k) != null) { //adds non-null pointsEarned + corresponding pointsPossible to total sum for calculation
		            		pointsPossibleSum += pointsPossible.get(k);
		            		pointsEarnedSum += pointsEarned.get(k);
	            		}
	            	}
            	}
            	            	
            	if (assignmentIDs != null && pointsPossibleSum != 0) { //if anything was in assignments
            		tempMPGrade = Utils.round((pointsEarnedSum/ pointsPossibleSum) * 100, 2);
            	}
            	
            	if (tempMPGrade != -1.00) { 
			         if (j < 3) {
			            grades.set(j - 1, tempMPGrade);
			    	} else { //makes space for midterm
			        	grades.set(j, tempMPGrade);
			        }
            	}
            	
        		pointsPossibleSum = 0;
        		pointsEarnedSum = 0;
            	tempMPGrade = -1.00;
    		}
    		
    		for (int l = 1; l <= 2; l++) { // midterm / final exam grade. in for loop for convienence. 
    			if (l == 1) {
    				assignmentID = PowerSchool.assignmentIDByMid(courseIDs.get(i));
    			} else if (l == 2) {
    				assignmentID = PowerSchool.assignmentIDByFin(courseIDs.get(i));
    			}
    			
            	examPointsEarned = PowerSchool.getExamPointsEarned(courseIDs.get(i), assignmentID, studentID);
            	examPointsPossible = PowerSchool.getExamPointsPossible(courseIDs.get(i), assignmentID, studentID);
            	
            	if (assignmentID != -1) {
        			examGrade = Utils.round(examPointsEarned/examPointsPossible, 2);
    	    		if (l == 1) {
    	        		if(examGrade != -1) {
    	        			grades.set(2, midtermGrade);
    	        		} else {
    	        			grades.set(2, null);
    	        		}
    	    			examGrade = -1;
    	    		} else if (l == 2) {
    	        		if(examGrade != -1) {
    	        			grades.set(5, finalExamGrade);
    	        		} else {
    	        			grades.set(5, null);
    	        		}
    	    		} else { 
    	    			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    	    		}
            	}
    		}
    		
    		grades.set(6, Utils.getGrade(grades));
    		
    		PowerSchool.updateCourseGrades(courseIDs.get(i), studentID, grades);
    		
        	for (int a = 0; a < 7; a++) {
        		grades.set(a, null);
        	}
    	} 	
		updateGPA(studentID);
    }
    
    public void updateGPA(int studentID) {
    	
    	ArrayList<Double> courseGrades = PowerSchool.getFinalCourseGrades(studentID);
    	
    	double courseGradesSum = -1.0;
    	int count = 0;
    	double GPA = -1;
    	
    	for (int i = 0; i < courseGrades.size(); i++) {
    		if (courseGrades.get(i) != -1.0) {
    			courseGradesSum += courseGrades.get(i);
    			count++;
    		}
    	}
    	
    	if (count > 0) {
    		courseGradesSum += 1.0;
    	}
    	
    	if (courseGradesSum > -1.0) {
    		GPA = Utils.round(((courseGradesSum/count)/25), 2);
    	}
    	
    	PowerSchool.updateGPA(GPA, studentID);
    }
    
    public void viewCourseGrades() {
    	int studentID = PowerSchool.getStudentIDbyUserID(activeUser.getUserId());
    	
    	ArrayList<String> courses = PowerSchool.studentCourses(activeUser);
    	ArrayList<String> courseTitle = PowerSchool.getCourseTitlesFromCourseNo(courses);
    	ArrayList<Double> grade = PowerSchool.getCourseGrades(PowerSchool.getCourseIDsFromCourseNo(courses), studentID);

    	System.out.println("");
    	for (int i = 0; i < courseTitle.size(); i++) {
    		if (grade.get(i) != null) {
        		System.out.println(i + 1 + ". " + courseTitle.get(i) + " / " + (grade.get(i) == -1 ? "--" : grade.get(i)));	
    		}
    	}	
    }
    
    public void viewAssignmentGrades() {
    	int studentID = PowerSchool.getStudentIDbyUserID(activeUser.getUserId());
    	int courseID = assignments();
    	int markingPeriod = Utils.getInt(in, 7);
    	
    	while (markingPeriod > 6) {
    		System.out.print("::: ");
    		markingPeriod = Utils.getInt(in, 7);
    	}
    	
    	ArrayList<String> assignments = new ArrayList<String>();
    	ArrayList<Integer> pointsEarned = new ArrayList<Integer>();
    	ArrayList<Integer> pointsPossible = new ArrayList<Integer>();
    	
    	if (markingPeriod <= 4) {
    		assignments = PowerSchool.assignmentNameByMP(courseID, markingPeriod);
        	pointsPossible = PowerSchool.assignmentValuesByMP(courseID, markingPeriod);
        	pointsEarned = PowerSchool.pointsEarnedByStudent(courseID, PowerSchool.getAssignmentIDByCourseIDAndStudentIDAndMarkingPeriod(courseID, studentID, markingPeriod), studentID);
    	} else if (markingPeriod == 5) {
    		assignments = PowerSchool.assignmentNameByMid(courseID);
    	} else if (markingPeriod == 6) {
    		assignments = PowerSchool.assignmentNameByFin(courseID);
    		pointsPossible = PowerSchool.assignmentValuesByFin(courseID);
    	}
    	
    	System.out.println("");
    	
    	if (assignments.size() > 0) {
    		for (int i = 0; i < assignments.size(); i++) {
    			System.out.print("[" + (i + 1) + "] " + assignments.get(i));
    			System.out.print(" / " + pointsEarned.get(i));
    			System.out.println(" (out of " + pointsPossible.get(i) + " pts)");
    		}
    	} else {
    		System.out.println("There are no assignments.");
    	}
    }
    
    private void root() {
    	System.out.println("Hello, again, Root!\n");
        boolean validLogin = true;
        while (validLogin) {
            final int RESET_PASSWORD = 1;
            final int RESET_DATABASE = 2;
            final int LOGOUT = 3;
            final int SHUTDOWN = 4;
        	
            boolean factoryReset = false;
            
            switch (getSelectionRoot()) {
                case RESET_PASSWORD: resetPassword(); break;
                case RESET_DATABASE: 
                	factoryReset = resetDatabase();
                	if (!factoryReset) {
                		break;
                	}
                case LOGOUT: 
                	if (logout(factoryReset)) {
                		validLogin = false;
					}
                	break;
                case SHUTDOWN: shutdown(); break;
                default: System.out.println("\nInvalid selection.\n");
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
        return (activeUser.getLastLogin().equals("0000-00-00 00:00:00.000") || activeUser.getLastLogin().equals("1111-11-11 11:11:11.11"));
    }
    
    
    /**
     * Shutdowns application.
     */
    
    public int getSelectionRoot() {
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.print("[4] Shutdown.\n\n::: ");
        
        return in.nextInt();
    }
    
    public void resetPassword() {
    	in.nextLine();
    	System.out.print("\nUsername: ");
    	String userID = in.nextLine();
    	Timestamp ts = Timestamp.valueOf("1111-11-11 11:11:11.11");
    	if (Utils.confirm(in, "\nAre you sure you want to reset the password for " + userID + "? (y/n) ")) {
	    	try {
	    		int successfulChange = PowerSchool.resetPassword(userID, ts);
		    	switch(successfulChange) {
		    		case 1: System.out.println("\nSuccessfully reset password for " + userID + "."); break;
			    	case -1: System.out.println("\n" + userID + " is not a valid username."); break;
			    	case -2: System.out.println("Issue with updating last login"); break;
			    	case -3: System.out.println("Issue with PowerSchool statement"); break;
		    	}
	    	} catch(Exception e) {
	            System.out.println("\nInvalid username.\n");
	            return;
	    	}
	    	System.out.println("");
    	} else {
    		System.out.println("");
    	}
    }
    
    public boolean resetDatabase() {
    	in.nextLine();
    	if (Utils.confirm(in, "\nAre you sure you want to reset all settings and data? (y/n) ")) {
	    	try {
	    		PowerSchool.initialize(true);
    			System.out.println("\nSuccessfully reset database. Please log in again to continue.");
    			return true;
	    	} catch(Exception e) {
	            return false;
	    	}
    	} else {
    		System.out.println("");
    	}
    	return false;
    }
    
     
    public void shutdown() {
    	in.nextLine();
        
    	if (Utils.confirm(in, "\nAre you sure? (y/n) ")) {
	    	try {
	        	if (in != null) {
	                in.close();
	            }
	            
	            System.out.println("\nGoodbye!");
	            System.exit(0);
	    	} catch(Exception e) {
	            return;
	    	}
    	} else {
    		System.out.println("");
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