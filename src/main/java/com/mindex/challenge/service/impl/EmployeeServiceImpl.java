package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final CompensationRepository compensationRepository;

    @Value("${max.worker.threads}")
    private int maxWorkerThreads;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, CompensationRepository compensationRepository) {
        this.employeeRepository = employeeRepository;
        this.compensationRepository = compensationRepository;
    }

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Retrieving employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public Compensation createCompensation(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);
        compensationRepository.save(compensation);
        return compensation;
    }

    @Override
    public Compensation readCompensation(String employeeId) {
        LOG.debug("Retrieving compensation with id [{}]", employeeId);
        Compensation compensation = compensationRepository.findByEmployeeId(employeeId);
        if (compensation == null) {
            throw new RuntimeException("Invalid compensationId: " + employeeId);
        }
        return compensation;
    }

    @Override
    public Compensation updateCompensation(Compensation compensation) {
        LOG.debug("Updating compensation [{}]", compensation);
        return compensationRepository.save(compensation);
    }

    /**
     * Retrieves the reporting structure for a given employee.
     *
     * @param employeeId the ID of the employee whose reporting structure is to be retrieved
     * @return the reporting structure of the specified employee
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws RuntimeException if the employeeId is invalid (i.e., no employee with the given ID exists)
     */
    @Override
    public ReportingStructure getReportingStructure(String employeeId) throws ExecutionException, InterruptedException {
        Employee currentEmployee = read(employeeId);
        if (currentEmployee == null) {
            throw new RuntimeException("Invalid employeeId: " + employeeId);
        }

        ForkJoinPool customPool = new ForkJoinPool(maxWorkerThreads);

        LOG.info("Starting async Calc");
        long asyncStartTime = System.nanoTime();
        ReportingStructure returnVal = calculateReportStructure(new ReportingStructure.ReportingStructureBuilder()
                .Employee(currentEmployee)
                .NumberOfReports(0) // setting a default value of 0, calculateReportStructure will either correct this or it's already correct.
                .build(), customPool);
        long asyncEndTime = System.nanoTime();

        LOG.info("Ending async Calc");

        //I got on this whole thing wondering if there was actually any performance improvement over a dataset this small.
        LOG.info("Starting sync Calc");
        long startTime = System.nanoTime();
        ReportingStructure testVal = calculateReportStructureSlow(new ReportingStructure.ReportingStructureBuilder()
                .Employee(currentEmployee)
                .NumberOfReports(0)
                .build());
        long endTime = System.nanoTime();
        LOG.info("Ending sync Calc");

        long asyncElapsedTime = asyncEndTime - asyncStartTime;
        long asyncElapsedTimeInMillis = asyncElapsedTime / 1_000_000;
        long elapsedTime = endTime - startTime;
        long elapsedTimeInMillis = elapsedTime / 1_000_000;
        LOG.info("Async Elapsed Time: {}", asyncElapsedTimeInMillis);
        LOG.info("Sync Elapsed Time: {}", elapsedTimeInMillis);

        return returnVal;
    }


    /**
     * Calculates the reporting structure for a given employee.
     * Calculations parallelized with a configurable worker pool to increase response time at scale
     *
     * @param employeeReportingStructure the initial reporting structure of the employee
     * @param customPool the ForkJoinPool used to parallelize the computation
     * @return the complete reporting structure of the employee, including all direct and indirect reports
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    private ReportingStructure calculateReportStructure(ReportingStructure employeeReportingStructure, ForkJoinPool customPool) throws ExecutionException, InterruptedException {

        Employee employee = employeeReportingStructure.getEmployee();
        LOG.debug("*Async* Constructing Report Structure for employee [{} {}]", employee.getFirstName(), employee.getLastName());

        if (employee.getDirectReports() == null) {
            return employeeReportingStructure;
        }

        int directReportCount = employee.getDirectReports().size();
        if (directReportCount > 0) {
            //db reads can be expensive so each is given to a worker thread to increase throughput
            List<Employee> directReports = customPool.submit(() ->
                employee.getDirectReports().parallelStream()
                        .map(Employee::getEmployeeId)
                        .map(this::read)
                        .toList()
            ).get();

            //each of the employees gathered may have their own direct reports
            //depending on the connections between employees, the custom pool size will being to bottleneck to prevent
            //consuming too many resources
            List<ReportingStructure> directReportStructures = customPool.submit(() ->directReports.parallelStream()
                    .map(leafEmployee -> new ReportingStructure.ReportingStructureBuilder()
                            .Employee(leafEmployee)
                            .NumberOfReports(0)
                            .build())
                    .map(rs -> {
                        try {
                            return calculateReportStructure(rs, customPool);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList()
            ).get();

            employeeReportingStructure.getEmployee()
                    .setDirectReports(directReportStructures.stream()
                            .map(ReportingStructure::getEmployee)
                            .toList());
            directReportCount += directReportStructures.stream()
                    .map(ReportingStructure::getNumberOfReports)
                    .mapToInt(Integer::intValue).sum();
        }
        employeeReportingStructure.setNumberOfReports(directReportCount);

        return employeeReportingStructure;
    }

    /**
     * Calculates the reporting structure for a given employee.
     * Sequential stream solution for comparison's sake
     *
     * @param employeeReportingStructure the initial reporting structure of the employee
     * @return the complete reporting structure of the employee, including all direct and indirect reports
     */
    private ReportingStructure calculateReportStructureSlow(ReportingStructure employeeReportingStructure) {

        Employee employee = employeeReportingStructure.getEmployee();
        LOG.debug("*Sync* Constructing Report Structure for employee [{} {}]", employee.getFirstName(), employee.getLastName());

        if (employee.getDirectReports() == null) {
            return employeeReportingStructure;
        }

        int directReportCount = employee.getDirectReports().size();
        if (directReportCount > 0) {
            List<ReportingStructure> directReportStructures = employee.getDirectReports().stream()
                        .map(Employee::getEmployeeId)
                        .map(this::read)
                    .map(leafEmployee -> new ReportingStructure.ReportingStructureBuilder()
                            .Employee(leafEmployee)
                            .NumberOfReports(0)
                            .build())
                    .map(this::calculateReportStructureSlow)
                    .toList();

            employeeReportingStructure.getEmployee()
                    .setDirectReports(directReportStructures.stream()
                            .map(ReportingStructure::getEmployee)
                            .toList());
            directReportCount += directReportStructures.stream()
                    .map(ReportingStructure::getNumberOfReports)
                    .mapToInt(Integer::intValue).sum();
        }
        employeeReportingStructure.setNumberOfReports(directReportCount);

        return employeeReportingStructure;
    }
}
