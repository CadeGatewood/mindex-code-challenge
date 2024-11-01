package com.mindex.challenge.data;

public class ReportingStructure {
    private final Employee employee;
    private int numberOfReports;

    public Employee getEmployee() {
        return employee;
    }
    public int getNumberOfReports() {
        return numberOfReports;
    }
    public void setNumberOfReports(int numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

    private ReportingStructure(ReportingStructureBuilder builder) {
        this.employee = builder.employee;
        this.numberOfReports = builder.numberOfReports;
    }

    public static class ReportingStructureBuilder {
        private Employee employee;
        private int numberOfReports;

        public ReportingStructureBuilder() {}
        public ReportingStructureBuilder Employee(Employee employee) {
            this.employee = employee;
            return this;
        }
        public ReportingStructureBuilder NumberOfReports(int numberOfReports) {
            this.numberOfReports = numberOfReports;
            return this;
        }
        public ReportingStructure build() {
            return new ReportingStructure(this);
        }
    }
}
