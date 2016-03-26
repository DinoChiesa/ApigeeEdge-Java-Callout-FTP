package com.dinochiesa.edgecallouts.ftp.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import mockit.Mock;
import mockit.MockUp;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.message.Message;

import com.dinochiesa.edgecallouts.FtpPut;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class TestFtpPutCallout {
    private final static String testDataDir = "src/test/resources";

    MessageContext msgCtxt;
    ExecutionContext exeCtxt;
    String messageContent;
    Message message;

    @BeforeMethod()
    public void testSetup1() {

        msgCtxt = new MockUp<MessageContext>() {
            private Map variables;
            public void $init() {
                variables = new HashMap();
            }

            @Mock()
            public <T> T getVariable(final String name){
                if (variables == null) {
                    variables = new HashMap();
                }
                return (T) variables.get(name);
            }

            @Mock()
            public boolean setVariable(final String name, final Object value) {
                if (variables == null) {
                    variables = new HashMap();
                }
                variables.put(name, value);
                return true;
            }

            @Mock()
            public boolean removeVariable(final String name) {
                if (variables == null) {
                    variables = new HashMap();
                }
                if (variables.containsKey(name)) {
                    variables.remove(name);
                }
                return true;
            }

            @Mock()
            public Message getMessage() {
                return message;
            }
        }.getMockInstance();

        exeCtxt = new MockUp<ExecutionContext>(){ }.getMockInstance();

        message = new MockUp<Message>(){
            @Mock()
            public InputStream getContentAsStream() {
                return new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
            }
        }.getMockInstance();
    }

    @DataProvider(name = "batch1")
    public static Object[][] getDataForBatch1()
        throws IOException, IllegalStateException {

        // @DataProvider requires the output to be a Object[][]. The inner Object[]
        // is the set of params that get passed to the test method.  So, if you want
        // to pass just one param to the constructor, then each inner Object[] must
        // have length 1.

        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        // Path currentRelativePath = Paths.get("");
        // String s = currentRelativePath.toAbsolutePath().toString();
        // System.out.println("Current relative path is: " + s);

        // read in all the directories with config.xml files in the test directory
        File testDir = new File(testDataDir);
        if (!testDir.exists()) {
            throw new IllegalStateException("no test directory.");
        }

        File defaultsFile = new File(testDataDir, "default-values.json");
        Map<String,String> defaults = (defaultsFile.exists() && defaultsFile.isFile()) ?
            om.readValue(defaultsFile, HashMap.class) : new HashMap<String,String>();

        File[] files = testDir.listFiles();
        ArrayList<TestCase> list = new ArrayList<TestCase>();
        for (File file : files) {
            if (file.isDirectory()) {
                String dirname = file.getName();
                File testFile = new File(file, "test.json");
                if (testFile.exists() && testFile.isFile()) {
                    TestCase tc = om.readValue(testFile, TestCase.class);
                    File configFile = new File(file, tc.getPolicyConfigPath());
                    if (configFile.exists() && configFile.isFile()) {
                        tc.setTestName(dirname);
                        tc.setPolicyConfigPath(configFile.getAbsolutePath());
                        // use defaults where values are not present
                        for (Map.Entry<String, String> entry : defaults.entrySet()) {
                            String key = entry.getKey();
                            String defaultValue = entry.getValue();
                            String actualValue = tc.getContext().get(key);
                            if (actualValue == null) {
                                tc.getContext().put(key, defaultValue);
                            }
                        }
                        list.add(tc);
                    }
                }
            }
        }

        // OMG!!  Seriously? Is this the easiest way to generate a 2-d array?
        int n = list.size();
        if (n == 0) {
            throw new IllegalStateException("no tests found.");
        }
        Object[][] data = new Object[n][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Object[]{ list.get(i) };
        }
        return data;
    }

    @Test
    public void testDataProviders() throws IOException {
        Assert.assertTrue(getDataForBatch1().length > 0);
    }

    @Test(dataProvider = "batch1")
    public void test2_Configs(TestCase tc) throws IOException {
        if (tc.getDescription()!= null)
            System.out.printf("  %10s - %s\n", tc.getTestName(), tc.getDescription() );
        else
            System.out.printf("  %10s\n", tc.getTestName() );

        // set message content if any
        messageContent = (tc.getMessageContent()!= null) ? tc.getMessageContent() : "";

        // set variables into message context
        for (Map.Entry<String, String> entry : tc.getContext().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            msgCtxt.setVariable(key, value);
        }
        ConfigObject c = ConfigObject.readConfig(tc.getPolicyConfigPath());
        FtpPut callout = new FtpPut(c.getProperties());

        // execute and retrieve output
        ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);

        String result = msgCtxt.getVariable("ftp_result");
        System.out.println("RESULT: " + result);

        String s = tc.getExpected().get("success");
        ExecutionResult expectedResult = (s!=null && s.toLowerCase().equals("true")) ?
                                           ExecutionResult.SUCCESS : ExecutionResult.ABORT;
        // check result and output
        if (expectedResult == actualResult) {
            if (expectedResult == ExecutionResult.SUCCESS) {
            }
            else {
            }
        }
        else {
            Assert.assertEquals(actualResult, expectedResult, "result not as expected");
        }
        System.out.println("=========================================================");
    }

}
