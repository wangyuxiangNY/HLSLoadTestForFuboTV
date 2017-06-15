package com.fubotv.jmeter;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.extractor.DebugPostProcessor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.PostWriter;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import com.sun.jna.Structure;

public class loadTestUsingJmeterLib {
      private static final String AUTH_URL ="fubo.auth0.com/oauth/ro";
      private static final String  AUTH_USER = "yuxiang.wang.ny@gmail.com";
      private static final String AUTH_PASS ="football123";
      
      private static final String SAMPLE_STREAM = "api.fubo.tv/v3/kgraph/v1/networks/88199/stream/";
      
	public static void main(String[] argv) throws Exception {

        //JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine("localhost");
	
        //JMeter initialization (properties, log levels, locale, etc)
        JMeterUtils.loadJMeterProperties("C:\\Users\\azurewangyx\\Desktop\\apache-jmeter-3.2\\bin\\jmeter.properties");
         JMeterUtils.initLocale();
        JMeterUtils.setJMeterHome("C:\\Users\\azurewangyx\\Desktop\\apache-jmeter-3.2");


        // JMeter Test Plan, basic all u JOrphan HashTree
        HashTree testPlanTree = new HashTree();

        // Auth request
        String  authBodyRaw = "scope=openid+user+offline_access&response_type=token&sso=false&connection=Username-Password-Authentication&username=yuxiang.wang.ny%40gmail.com&password=football123&client_id=d6YiOzgcOnC305cKkBZoydAu62K1z7Ly&grant_type=password&device=Browser";
        HTTPSampler authSampler = new HTTPSampler();
       // authSampler.setDomain("fubo.auth0.com/oauth/ro");
        authSampler.setDomain("automeditator.com");
         authSampler.setPort(80);
        authSampler.setPath("/");
        authSampler.setMethod("GET");
    
  
        
        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.addTestElement(authSampler);
        loopController.setFirst(true);
        loopController.initialize();

        // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopController);

        // Test Plan
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");

        // Construct Test Plan from previously initialized elements
        testPlanTree.add("testPlan", testPlan);
        testPlanTree.add("loopController", loopController);
        testPlanTree.add("threadGroup", threadGroup);
        testPlanTree.add("httpSampler", authSampler);

 
        
     // Result collector
        
        ResultCollector resultCollector = new ResultCollector();
        resultCollector.setErrorLogging(false);
        resultCollector.setFilename("C:\\Users\\azurewangyx\\Desktop\\jmeterExamples\\apiLOG.txt");
        SampleSaveConfiguration saveConfiguration = new SampleSaveConfiguration();
        saveConfiguration.setTime(true);
        saveConfiguration.setLatency(true);
        saveConfiguration.setTimestamp(true);

        saveConfiguration.setSuccess(true);
        saveConfiguration.setLabel(true);
        saveConfiguration.setCode(true);
        saveConfiguration.setMessage(true);
        saveConfiguration.setThreadName(true);
        saveConfiguration.setDataType(true);
        saveConfiguration.setEncoding(true);
        saveConfiguration.setAsXml(false);
        saveConfiguration.setUrl(true);
        saveConfiguration.setResponseData(true);
        
        resultCollector.setSaveConfig(saveConfiguration);
        
        
       // testPlanTree.add(resultCollector);
       
        // Run Test Plan
       // jmeter.configure(testPlanTree);
        
        
        SaveService
        .saveTree(
          testPlanTree,
          new FileOutputStream(
            "C:\\Users\\azurewangyx\\Desktop\\jmeterExamples\\jmeter_api_saample.jmx"));

        
        
        
      //  jmeter.run();
      //  System.out.println("Done!");

    }
	
	
	
	  
}
