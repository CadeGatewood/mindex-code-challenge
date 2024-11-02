package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;

import java.util.concurrent.ExecutionException;

public interface EmployeeService {
    Employee create(Employee employee);
    Employee read(String id);
    Employee update(Employee employee);
    ReportingStructure getReportingStructure(String employeeId) throws ExecutionException, InterruptedException;
    Compensation createCompensation(Compensation compensation);
    Compensation readCompensation(String employeeId);
    Compensation updateCompensation(Compensation compensation);

}
