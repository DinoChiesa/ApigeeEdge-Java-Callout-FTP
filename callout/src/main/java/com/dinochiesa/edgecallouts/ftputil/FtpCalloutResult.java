package com.dinochiesa.edgecallouts.ftputil;

import java.util.ArrayList;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;

public class FtpCalloutResult {
    private static ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

    public FtpCalloutResult() {
        this.messages = new ArrayList<String>();
    }

    public String toJsonString() {
        try {
            String jsonResult = mapper.writer()
                .withDefaultPrettyPrinter()
                .writeValueAsString(this);
            return jsonResult;
        }
        catch (java.lang.Exception exc1) {
            return "{ \"status\" : \"unknown\" }";
        }
    }

    private String status;
    public void setStatus(String value) { this.status = value; }
    public String getStatus() { return status; }

    private ArrayList<String> messages;
    public void setMessages(ArrayList<String> value) { this.messages = value; }
    public ArrayList<String> getMessages() { return messages; }
    public void addMessage(String s) { getMessages().add(s); }
}
