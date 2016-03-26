package com.dinochiesa.edgecallouts.ftp.tests;

import java.util.HashMap;

public class TestCase {

    private String _testName;
    private String _description;
    private String _policyConfigPath;
    private String _messageContent;
    private HashMap<String,String> _context; // JSON hash
    private HashMap<String,String> _expected; // JSON hash

    // getters
    public String getTestName() { return _testName; }
    public String getDescription() { return _description; }
    public String getPolicyConfigPath() { return _policyConfigPath; }
    public String getMessageContent() { return _messageContent; }
    public HashMap<String,String> getContext() { return _context; }
    public HashMap<String,String> getExpected() { return _expected; }

    // setters
    public void setTestName(String n) { _testName = n; }
    public void setDescription(String d) { _description = d; }
    public void setPolicyConfigPath(String p) { _policyConfigPath = p; }
    public void setMessageContent(String d) { _messageContent = d; }
    public void setContext(HashMap<String,String> hash) { _context = hash; }
    public void setExpected(HashMap<String,String> hash) { _expected = hash; }
}
