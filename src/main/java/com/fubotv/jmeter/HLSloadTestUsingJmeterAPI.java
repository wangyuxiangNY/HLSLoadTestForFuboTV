package com.fubotv.jmeter;

import java.io.FileOutputStream;

import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.ForeachControlPanel;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.extractor.json.jsonpath.gui.JSONPostProcessorGui;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jmeter.timers.gui.ConstantTimerGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Class to create HLS_Test.jmx to handle HLS via jmeter API.  
 * @auther yuxiang wang
 */

public class HLSloadTestUsingJmeterAPI {
	
	  private static  final String USER_NAME = "yuxiang.wang.ny@gmail.com";
	  private static  final String PASSWORD = "football123";
		
	  protected static Logger logger = LoggerFactory.getLogger(HLSloadTestUsingJmeterAPI.class);

	
	 public static void main(String[] argv) throws Exception {
	        
		  
		 //JMeter Engine
	        StandardJMeterEngine jmeter = new StandardJMeterEngine("localhost");
		
	    	 JMeterUtils.loadJMeterProperties("C:\\Users\\azurewangyx\\Desktop\\apache-jmeter-3.2\\bin\\jmeter.properties");
	         JMeterUtils.initLocale();
	        JMeterUtils.setJMeterHome("C:\\Users\\azurewangyx\\Desktop\\apache-jmeter-3.2");
	   
	        logger.info("Creating HTTP Samplers...");
	        ListedHashTree  oauthTree = oauth(USER_NAME, PASSWORD);
	        
	        ListedHashTree  getStream = getStreamURL();
			
		    //GET PLAYLIST
		    ListedHashTree playList = getPlayList();
		   
		    //Get Chunk LIST
		    ListedHashTree chunkList = getTSChunkList();
		    
		    ListedHashTree getChunks = getChunks();
		    
	        // TestPlan
	        TestPlan testPlan = new TestPlan();
	        testPlan.setName("Test Plan");
	        testPlan.setEnabled(true);
	        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
	        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());

	        // ThreadGroup controller
	        LoopController loopController = new LoopController();
	        loopController.setEnabled(true);
	        loopController.setLoops(1);
	        loopController.setProperty(TestElement.TEST_CLASS,
	                LoopController.class.getName());
	        loopController.setProperty(TestElement.GUI_CLASS,
	                LoopControlPanel.class.getName());

	        loopController.setFirst(true);
	        
	        // ThreadGroup
	        ThreadGroup threadGroup = new ThreadGroup();
	        threadGroup.setName("Thread Group");
	        threadGroup.setEnabled(true);
	        threadGroup.setProperty(TestElement.TEST_CLASS,
	                ThreadGroup.class.getName());
	        threadGroup.setProperty(TestElement.GUI_CLASS,
	                ThreadGroupGui.class.getName());
	        threadGroup.setSamplerController(loopController);
	        threadGroup.setNumThreads(1);
	        threadGroup.setRampUp(2);
	        
	       
	        // Create TestPlan hash tree
	       
	        ListedHashTree jmeterTestPlan = new ListedHashTree();
	         jmeterTestPlan.add(testPlan);
	         
	         jmeterTestPlan.add(new ListedHashTree(threadGroup));
	         jmeterTestPlan.add(threadGroup, setDelayBetweenRequests("50"));
	         
	         jmeterTestPlan.add(threadGroup,oauthTree);
	         jmeterTestPlan.add(threadGroup, getStream);
	         jmeterTestPlan.add(threadGroup, playList);

	         jmeterTestPlan.add(threadGroup, chunkList);
	         jmeterTestPlan.add(threadGroup, getChunks);
	         
	         jmeterTestPlan.add( threadGroup, createResultCollector());
	        
	         // Save test plan  into jmx file
		     SaveService.saveTree(jmeterTestPlan, new FileOutputStream(
		                "C:\\Users\\azurewangyx\\Desktop\\jmeterExamples\\HLS_Test.jmx"));
	      //  jmeter.configure(jmeterTestPlan);
	     //   jmeter.run();
	        logger.info("HLS_Test.jmx is generated.");
	       
	    }
	 
	/*
	 * Add delay between requests
	 *  
	 *  @param delayInMillisecond
	 *  
	 */
	private static ListedHashTree setDelayBetweenRequests(String delayInMillisecond)
	{
		ConstantTimer  timer = new ConstantTimer();
		timer.setName("Delay between Requests");
		timer.setEnabled(true);
		timer.setProperty(TestElement.TEST_CLASS,ConstantTimer.class.getName());
		timer.setProperty(TestElement.GUI_CLASS,ConstantTimerGui.class.getName());
		
		timer.setDelay(delayInMillisecond);
		
	    ListedHashTree delayTree = new ListedHashTree(timer);
	    
	    return delayTree;

	}
	 
	/* Create HTTPSampler for oauth
	 * 
	 * @param userName 
	 * @param password
	 * @return  ListedHashTree for oauth
	 */
	public static ListedHashTree oauth(String userName, String password)
	{
		//AUTH REQUEST
        String  authBodyRaw = "scope=openid+user+offline_access&response_type=token&sso=false&connection=Username-Password-Authentication&username=yuxiang.wang.ny%40gmail.com&password=football123&client_id=d6YiOzgcOnC305cKkBZoydAu62K1z7Ly&grant_type=password&device=Browser";
        HTTPSampler sampler = createRequest("post oauth", "POST", "fubo.auth0.com", "/oauth/ro", authBodyRaw);
       
        JSONPostProcessor jsonProcessor = new JSONPostProcessor();
        jsonProcessor.setName("Extract ID_TOKEN");
        jsonProcessor.setEnabled(true);
        jsonProcessor.setProperty(TestElement.TEST_CLASS, JSONPostProcessor.class.getName());
        jsonProcessor.setProperty(TestElement.GUI_CLASS, JSONPostProcessorGui.class.getName());
        jsonProcessor.setRefNames("id_token");
        jsonProcessor.setMatchNumbers("1");
        jsonProcessor.setJsonPathExpressions("$.id_token");
        
        
        ListedHashTree authTree = new ListedHashTree(sampler);
        authTree.add(sampler, jsonProcessor);
	    return authTree;
	}
	
	/* Create HTTPSampler to get streamURL 
	 * 
	 * @param userName 
	 * @param password
	 * @return  ListedHashTree for streamURL
	 */
	public static ListedHashTree getStreamURL()
	{
		HTTPSampler sampler = createRequest("Get Stream", "GET", "api.fubo.tv", "/v3/kgraph/v1/networks/88199/stream", null);
        //add header for authentication
        HeaderManager headManager = new HeaderManager();
		headManager.setName("header for Autho");
		headManager.setEnabled(true);
		headManager.setProperty(TestElement.TEST_CLASS,HeaderManager.class.getName());
		headManager.setProperty(TestElement.GUI_CLASS,HeaderPanel.class.getName());
		Header header = new Header("Authorization","Bearer ${id_token}");
	    headManager.add(header);
		
		//Exract Stream Domain
	    RegexExtractor domainExtractor = new RegexExtractor();
	    domainExtractor.setName("Exract Stream Domain");
	    domainExtractor.setEnabled(true);
	    domainExtractor.setProperty(TestElement.TEST_CLASS,RegexExtractor.class.getName());
	    domainExtractor.setProperty(TestElement.GUI_CLASS,RegexExtractorGui.class.getName());
	    domainExtractor.setRegex("{\"streamUrl\":\"https://(.*)/(.*)\",\"sessionTrack");
	    domainExtractor.setMatchNumber(1);
	    domainExtractor.setRefName("streamDomain");
	    domainExtractor.setTemplate("$1$");
		
		//Extract Stream PATH
	    RegexExtractor pathExtractor= new RegexExtractor();
	    pathExtractor.setName("Exract Stream Path");
	    pathExtractor.setEnabled(true);
	    pathExtractor.setProperty(TestElement.TEST_CLASS,RegexExtractor.class.getName());
	    pathExtractor.setProperty(TestElement.GUI_CLASS,RegexExtractorGui.class.getName());
	    pathExtractor.setRegex( "{\"streamUrl\":\"https://playlist.fubo.tv(.*)\",\"sessionTrack");
	    pathExtractor.setMatchNumber(1);
	    pathExtractor.setRefName("streamPath");
	    pathExtractor.setTemplate("$1$");
	    
	    ListedHashTree streamTree = new ListedHashTree(sampler);
	    streamTree.add(sampler, headManager);
	    streamTree.add(sampler, domainExtractor);
	    streamTree.add(sampler, pathExtractor);
	    return streamTree;
	}
	 
	/* Create HTTPSampler to get play list 
	 * 
	 * @param userName 
	 * @param password
	 * @return  ListedHashTree for play list
	 */
	public static ListedHashTree getPlayList()
	{
		HTTPSampler sampler =  createEmptySampler("Get Play list");
		sampler.setPostBodyRaw(true);
		sampler.setDomain("${streamDomain}");
        sampler.setProtocol("https");	
        sampler.setPath("${streamPath}");
        sampler.setMethod("GET");
        
      
		
		//Extract media.m3u8 Domain
	    RegexExtractor domainExtractor = new RegexExtractor();
	    domainExtractor.setName("Extract media.m3u8 Domain");
	    domainExtractor.setEnabled(true);
	    domainExtractor.setProperty(TestElement.TEST_CLASS,RegexExtractor.class.getName());
	    domainExtractor.setProperty(TestElement.GUI_CLASS,RegexExtractorGui.class.getName());

	    domainExtractor.setRefName("mediaDomain");
	    domainExtractor.setRegex("(?m)^(https://)(.*)/(.*)");
	    domainExtractor.setMatchNumber(1);
	    domainExtractor.setTemplate("$2$");
		
		//Extract media.m3u path
	    RegexExtractor pathExtractor= new RegexExtractor();
	    pathExtractor.setName("Extract media.m3u path");
	    pathExtractor.setEnabled(true);
	    pathExtractor.setProperty(TestElement.TEST_CLASS,RegexExtractor.class.getName());
	    pathExtractor.setProperty(TestElement.GUI_CLASS,RegexExtractorGui.class.getName());

	    pathExtractor.setRefName("playList");
	    //(?m)(https://playlist.fubo.tv/)([^"]+)
	    pathExtractor.setRegex( "(?m)(https://playlist.fubo.tv/)([^#\"]+)");
	    pathExtractor.setMatchNumber(0);
	    pathExtractor.setTemplate("$2$");
	    
	    
	    ListedHashTree streamTree = new ListedHashTree(sampler);
	    streamTree.add(sampler, domainExtractor);
	    streamTree.add(sampler, pathExtractor);
	    return streamTree;
	}
	 
	/* Create HTTPSampler to get chunk list 
	 * 
	 * @param userName 
	 * @param password
	 * @return  ListedHashTree for getting chunk list
	 */
	public static ListedHashTree getTSChunkList()
	{
		HTTPSampler sampler = createEmptySampler("Get .TS list");
		sampler.setDomain("${mediaDomain}");
        sampler.setProtocol("https");	
        sampler.setPath("${playList}");
        sampler.setMethod("GET");
		
		//Extract media.m3u8 Domain
	    RegexExtractor domainExtractor = new RegexExtractor();
	    domainExtractor.setName("Extract chunk Domain");
	    domainExtractor.setEnabled(true);
	    domainExtractor.setProperty(TestElement.TEST_CLASS,RegexExtractor.class.getName());
	    domainExtractor.setProperty(TestElement.GUI_CLASS,RegexExtractorGui.class.getName());

	    domainExtractor.setRefName("chunkDomain");
	    domainExtractor.setRegex("(?m)^(https://)(.*?)/");
	    domainExtractor.setMatchNumber(1);
	    domainExtractor.setTemplate("$2$");
		
		//Extract media.m3u path
	    RegexExtractor pathExtractor= new RegexExtractor();
	    pathExtractor.setName("Extract chunk urls");
	    pathExtractor.setEnabled(true);
	    pathExtractor.setProperty(TestElement.TEST_CLASS,RegexExtractor.class.getName());
	    pathExtractor.setProperty(TestElement.GUI_CLASS,RegexExtractorGui.class.getName());

	    pathExtractor.setRefName("chunks");
	    pathExtractor.setRegex( "(?m)(^(https://gcs-streams-prod.fubo.tv)(.*))");
	    pathExtractor.setMatchNumber(-1);
	    pathExtractor.setTemplate("$3$");
	    
	    
	    ListedHashTree tsChunkListTree = new ListedHashTree(sampler);
	    tsChunkListTree.add(sampler, domainExtractor);
	    tsChunkListTree.add(sampler, pathExtractor);
	    return tsChunkListTree;
	}
	 
	/* Create HTTPSampler to get streamURL 
	 * 
	 * 
	 * @return  ResultCollector for logging
	 */ 
	public static ResultCollector createResultCollector()
	{
        ResultCollector resultCollector = new ResultCollector();
        resultCollector.setName("View Result Tree");
        resultCollector.setEnabled(true);
       // resultCollector.setErrorLogging(true);
        resultCollector.setProperty(TestElement.TEST_CLASS,
        		ResultCollector.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS,
        		ViewResultsFullVisualizer.class.getName());
        resultCollector.setFilename("C:\\Users\\azurewangyx\\Desktop\\jmeterExamples\\log.txt");
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
        
        return resultCollector;
	}
	    
	/* Create HTTPSampler controller forEach, so that later on we can utilize it 
	 * to get all the chunks in the chunk list
	 * 
	 * @param name controller name
	 * @return ForeachController
	 */
    public static ForeachController forEachController(String name)
    {
    	ForeachController  controller = new ForeachController();
    	controller.setName(name);
    	controller.setEnabled(true);
    	controller.setProperty(TestElement.TEST_CLASS,
    			ForeachController.class.getName());
    	controller.setProperty(TestElement.GUI_CLASS,
    			ForeachControlPanel.class.getName());
    	controller.setInputVal("chunks");
    	controller.setReturnVal("currentChunk");
    	controller.setUseSeparator(true);
    	
    	return controller;
    }
	
    /* Create HTTPSampler to get chunk content (.ts files)
	 * 
	 *
	 * @return  ListedHashTree for getting chunks
	 */
    public static ListedHashTree getChunks()
    {
    	HTTPSampler sampler = createEmptySampler("getChunks");
    	sampler.setDomain("${chunkDomain}");
        sampler.setProtocol("https");	
        sampler.setPath("${currentChunk}");
        sampler.setMethod("GET");
      
        ForeachController controller = forEachController("forEachController");
        ListedHashTree getChunks = new ListedHashTree(controller);
        getChunks.add(controller, sampler);
        
		return getChunks;
    }
	
    public static HTTPSampler createRequest(String sampleName, String method,  String domain, String path, String body)
	{
		return createRequest(sampleName, method, "https", domain, path, body);
	}
    
    /* Utility method to build up a request
     * 
     * @param sampleName
     * @param method
     * @param protocol
     * @param domain
     * @param path
     * @param body for 'POST' and 'PUT' request
     * @return HTTPSampler
     * 
     */
	public static HTTPSampler createRequest(String sampleName, String method, String protocol, String domain, String path, String body)
	{
		HTTPSampler sampler = new HTTPSampler();
		sampler.setName(sampleName);
		sampler.setEnabled(true);
		sampler.setProperty(TestElement.TEST_CLASS,
	        		HTTPSampler.class.getName());
		sampler.setProperty(TestElement.GUI_CLASS,
	        		HttpTestSampleGui.class.getName());
		sampler.setDomain(domain);
		sampler.setProtocol(protocol);
		sampler.setPath(path);
		sampler.setUseKeepAlive(true);
		sampler.setAutoRedirects(false);
		sampler.setFollowRedirects(true);
		sampler.setMethod(method);
		
		if (body != null && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")))
		{
			sampler.addNonEncodedArgument("",body,"");
			sampler.setPostBodyRaw(true);
		}
		
		return sampler;
        
	}
	
	/*
	 * Utility method to create an empty sampler 
	 * 
	 * @param name Sample Name
	 * @return HTTPSampler
	 */
	private static HTTPSampler createEmptySampler(String name)
	{
		HTTPSampler sampler = new HTTPSampler();
		sampler.setName(name);
		sampler.setEnabled(true);
		sampler.setProperty(TestElement.TEST_CLASS,
	        		HTTPSampler.class.getName());
		sampler.setProperty(TestElement.GUI_CLASS,
	        		HttpTestSampleGui.class.getName());

		sampler.setUseKeepAlive(true);
		sampler.setAutoRedirects(false);
		sampler.setFollowRedirects(true);
		
		return sampler;
	}
	
	/*
	 * to create a headManager containing  id_token in the headers FOR AUTHORIZATION
	 * 
	 * @return ListedHashTree representing headManager
	 * 
	 */
	public static ListedHashTree headerManager()
	{
		HeaderManager headManager = new HeaderManager();
		headManager.setName("header for Autho");
		headManager.setEnabled(true);
		headManager.setProperty(TestElement.TEST_CLASS,HeaderManager.class.getName());
		headManager.setProperty(TestElement.GUI_CLASS,HeaderPanel.class.getName());
		headManager.setProperty("header.name", "Authorization");
		headManager.setProperty("header.value", "Bearer ${id_token}");
		
		ListedHashTree headerManagerTree = new ListedHashTree();
		headerManagerTree.add(headManager);
		return headerManagerTree;
	}
	
	/*
	 * To  create logging
	 * @return ResultCollector
	 */
	public static ResultCollector resultCollector()
	{
		// Result collector
	    ResultCollector resultCollector = new ResultCollector();
	    resultCollector.setEnabled(true);
	    resultCollector.setProperty(TestElement.TEST_CLASS,
	    		ResultCollector.class.getName());
	    resultCollector.setProperty(TestElement.GUI_CLASS,
	    		ViewResultsFullVisualizer.class.getName());
	   // resultCollector.setErrorLogging(true);
	   // resultCollector.setSuccessOnlyLogging(false);
	    resultCollector.setFilename("C:\\Users\\azurewangyx\\Desktop\\jmeterExamples\\debugreport.jtl");
	    SampleSaveConfiguration saveConfiguration = new SampleSaveConfiguration();
	    saveConfiguration.setAsXml(true);
	    saveConfiguration.setCode(true);
	    saveConfiguration.setLatency(true);
	    saveConfiguration.setTime(true);
	    saveConfiguration.setTimestamp(true);
	    
	    saveConfiguration.setUrl(true);
	    saveConfiguration.setRequestHeaders(true);
        saveConfiguration.setResponseData(true);
        
	    resultCollector.setSaveConfig(saveConfiguration);
	    
	    return resultCollector;
	}
}
	