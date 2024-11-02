package com.mindex.challenge.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Employee {

    @Id
    private String employeeId;
    private String firstName;
    private String lastName;
    private String position;
    private String department;
    private List<Employee> directReports;

    //I wasn't certain which implementation was the intended target for the second task
    //At this point I've implemented a version that appends data directly to the employee collection
    //As well as one that creates a new collection and there exists a "foreign key" of the employeeId
    //between the 2 collections
    //This original implementation used the EmployeeRepository update method, and the updated Document class
    //to add the new information, and it would return along with the Employee "read" endpoint.
    //Due to the verbiage of the README, I've decided to make a separate collection to *only* return compensation data.
    /*private Compensation compensation;*/

    public Employee() {
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<Employee> getDirectReports() {
        return directReports;
    }

    public void setDirectReports(List<Employee> directReports) {
        this.directReports = directReports;
    }

    /*public Compensation getCompensation() {
        return compensation;
    }

    public void setCompensation(Compensation compensation) {
        this.compensation = compensation;
    }*/
}
