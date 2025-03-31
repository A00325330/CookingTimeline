package com.tus.group_project.test_helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseManager {

    @Autowired
    private DataSource dataSource;

    @Transactional
    public void executeSetupScripts() {
        runScript("setup_scripts/test_data.sql");
    }

    @Transactional
    public void clearDatabase() {
        runScript("setup_scripts/clear_data.sql");
    }

    private void runScript(String path) {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(path));
            System.out.println("Executed script: " + path);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute script: " + path, e);
        }
    }
}
