package com.epam.rd.autocode;

import com.epam.rd.autocode.domain.Employee;
import com.epam.rd.autocode.domain.FullName;
import com.epam.rd.autocode.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetMapperImpl implements SetMapper{
    @Override
    public Object mapSet(ResultSet resultSet) {
        Map<BigInteger, Employee> employees = new HashMap<>();
        Map<BigInteger, BigInteger> employeeToManagerMap = new HashMap<>();
        try {
            while (resultSet.next()) {
                BigInteger id = resultSet.getBigDecimal("id").toBigInteger();
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                String middleName = resultSet.getString("middleName");
                FullName fullName = new FullName(firstName, lastName, middleName);
                Position position = Position.valueOf(resultSet.getString("position"));
                LocalDate hired = resultSet.getDate("hiredate").toLocalDate();
                BigDecimal salary = resultSet.getBigDecimal("salary");
                BigInteger managerId = resultSet.getBigDecimal("manager") != null ? resultSet.getBigDecimal("manager").toBigInteger() : null;
                Employee manager = null;
                if (managerId != null) {
                    employeeToManagerMap.put(id, managerId);
                }
                Employee employee = new Employee(id, fullName, position, hired, salary, null);
                employees.put(id, employee);
            }
            employeeToManagerMap.forEach((key, value) -> {
                employees.put(key, findManager(key, employees, employeeToManagerMap));
            });
            return new HashSet<>(employees.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Employee findManager(BigInteger id, Map<BigInteger, Employee> employees, Map<BigInteger, BigInteger> employeeToManagerMap) {
        Employee employee = employees.get(id);
        Employee manager = employees.get(employeeToManagerMap.get(id));
        Employee employeeWithManager;
        if (manager != null) {
            employeeWithManager = new Employee(employee.getId(), employee.getFullName(), employee.getPosition(), employee.getHired(), employee.getSalary(),
                    findManager(manager.getId(), employees, employeeToManagerMap));
        } else {
            employeeWithManager = employee;
        }
        return employeeWithManager;
    }
}
