package com.flowstate.controller;

import com.flowstate.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfoControllerAdvancedTest {

    private InfoControllerAdvanced controller;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();

        appProperties.setName("Flowstate");
        appProperties.setVersion("1.0.0");
        appProperties.setMaxTasksPerPage(100);

        controller = new InfoControllerAdvanced(appProperties);
    }

    @Test
    void shouldReturnApplicationInformation() {

        Map<String, Object> result = controller.getAppInfo();

        assertEquals("Flowstate", result.get("appName"));
        assertEquals("1.0.0", result.get("appVersion"));
        assertEquals(100, result.get("maxTasksPerPage"));
    }
}