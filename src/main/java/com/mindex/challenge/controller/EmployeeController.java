package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("")
    public Employee create(@RequestBody Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        return employeeService.create(employee);
    }

    @GetMapping("/{id}")
    public Employee read(@PathVariable String id) {
        LOG.debug("Received employee search request for id [{}]", id);

        return employeeService.read(id);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        LOG.debug("Received employee update request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        return employeeService.update(employee);
    }

    @GetMapping("reportingStructure/{id}")
    public ReportingStructure readReportingStructure(@PathVariable String id) throws ExecutionException, InterruptedException {
        LOG.debug("Received employee reporting structure request for id [{}]", id);
        return employeeService.getReportingStructure(id);
    }

    //Example request body
    /*{
        "employeeId": "n4o5p6q7-r8s9-0t1u-2v3w-4x5y6z7a8b9c",
            "salary": 1100.10,
            "effectiveDate": "2024-11-01"
    }*/
    //I'd like to install swagger or springdocs, but I think that's beyond the scope of this exercise.
    @PostMapping("/compensation/{id}")
    public Compensation addCompensation(@PathVariable String id, @RequestBody Compensation compensation) {
        LOG.debug("Received employee add compensation request for id [{}]", id);
        Employee employee = employeeService.read(id);

        return employeeService.createCompensation(compensation);
    }

    @GetMapping("/compensation/{id}")
    public Compensation readCompensation(@PathVariable String id) {
        LOG.debug("Received employee compensation request for id [{}]", id);
        return employeeService.readCompensation(id);
    }

    @PatchMapping("/compensation/{id}")
    public Compensation updateCompensation(@PathVariable String id, @RequestBody Compensation compensation) {
        LOG.debug("Received employee update (it's really no different) compensation request for id [{}]", id);
        Employee employee = employeeService.read(id);

        return employeeService.updateCompensation(compensation);
    }
}
