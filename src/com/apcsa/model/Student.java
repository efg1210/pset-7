package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.apcsa.model.User;

public class Student extends User {

    private int studentId;
    private int classRank;
    private int gradeLevel;
    private int graduationYear;
    private double gpa;
    private String firstName;
    private String lastName;
    
    /**
     * Creates an instance of the Student class.
     *
     * @param user the User
     * @param rs a ResultSet of Student information
     */
    
    public Student (User user, ResultSet rs) throws SQLException {
    	this(rs.getInt("student_id"),
    		 rs.getInt("class_rank"),
    		 rs.getInt("grade_level"),
    		 rs.getInt("graduation"),
    		 rs.getDouble("gpa"),
    		 rs.getString("first_name"),
    		 rs.getString("last_name"),
    		 user
    	);
    }

    /**
     * Creates an instance of the Teacher class.
     *
     * @param studentId the student's ID
     * @param classRank the student's class rank
     * @param gradeLevel the student's grade level
     * @param graduationYear the year the student will graduate
     * @param gpa the student's gpa
     * @param firstName the student's first name
     * @param lastName the student's last name
     * @param rs a ResultSet of Student information
     */
    
    public Student(int studentId, int classRank, int gradeLevel, int graduationYear, double gpa, String firstName, String lastName,
			User user) throws SQLException {
		super(user.getUserId(), user.getAccountType(), user.getUsername(), user.getPassword(), user.getLastLogin());
		this.studentId = studentId;
		this.classRank = classRank;
		this.gradeLevel = gradeLevel;
		this.graduationYear = graduationYear;
		this.gpa = gpa;
		this.firstName = firstName;
		this.lastName = lastName;
		
	}
    
    /**
     * @return studentId
     */
    
    public int getStudentId() {
        return studentId;
    }
    
    /**
     * @return classRank
     */
    
    public int getClassRank() {
        return classRank;
    }
    
    /**
     * @return gradeLevel
     */
    
    public int getGradeLevel() {
        return gradeLevel;
    }
    
    /**
     * @return graduationYear
     */
    
    public int getGraduationYear() {
        return graduationYear;
    }
    
    /**
     * @return gpa
     */
    
    public double getGPA() {
        return gpa;
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
    
}