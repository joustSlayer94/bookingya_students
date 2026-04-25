package com.project.bookingya.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.project.bookingya",
        plugin = {"pretty"},
        tags = "@smoke",
        objectFactory = io.cucumber.spring.SpringFactory.class
)
public class CucumberTestRunnerIT {}