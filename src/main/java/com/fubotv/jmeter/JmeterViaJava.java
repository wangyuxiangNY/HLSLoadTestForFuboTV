package com.fubotv.jmeter;


import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileInputStream;



public class JmeterViaJava {


    public static void main(String[] argv) throws Exception {
        // JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        // Initialize Properties, logging, locale, etc.
       JMeterUtils.loadJMeterProperties("C:\\Users\\azurewangyx\\Desktop\\apache-jmeter-3.2\\bin\\jmeter.properties");
       JMeterUtils.setJMeterHome("C:\\Users\\azurewangyx\\Desktop\\apache-jmeter-3.2");
       JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
       JMeterUtils.initLocale();

        // Initialize JMeter SaveService
        SaveService.loadProperties();

        // Load existing .jmx Test Plan
        //FileInputStream in = new FileInputStream("C:\\Users\\azurewangyx\\Desktop\\jmeterExamples\\fuboTV.jmx");
        File in = new File("C:\\Users\\azurewangyx\\Desktop\\jmeterExamples\\fuboTV_NEW.jmx");
        
        HashTree testPlanTree = SaveService.loadTree(in);
        System.out.println("loaded!");
        // Run JMeter Test
        jmeter.configure(testPlanTree);
        jmeter.run();
        System.out.println("done!");
    }
	
}
