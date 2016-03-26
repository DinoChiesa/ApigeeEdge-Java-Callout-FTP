// FtpPut.java
//
// This is an Apigee Edge custom policy that performs an FTP PUT. 
//
// This code is licensed under the Apache 2.0 license. See the LICENSE
// file that accompanies this source.
//
// Friday, 25 March 2016, 12:11
//
// ------------------------------------------------------------------

package com.dinochiesa.edgecallouts;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.dinochiesa.edgecallouts.ftputil.FtpCalloutResult;
import com.dinochiesa.edgecallouts.ftputil.FtpCommandListener;
import com.dinochiesa.util.TemplateString;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.message.Message;
import java.io.IOException;


public class FtpPut implements Execution {
    private static final String _varPrefix = "ftp_";
    private Map properties; // read-only
    public FtpPut (Map properties) {
        this.properties = properties;
    }
    private static final String varName(String s) { return _varPrefix + s; }

    private boolean getDebug() {
        String value = (String) this.properties.get("debug");
        if (value == null) return false;
        if (value.trim().toLowerCase().equals("true")) return true;
        return false;
    }
    private boolean getWantBase64Decode(MessageContext msgCtxt) {
        String value = (String) this.properties.get("want-base64-decode");
        if (value == null) return false;
        value = resolvePropertyValue(value.trim(), msgCtxt);
        if ((value == null) || value.equals("")) return false;
        return value.trim().toLowerCase().equals("true");
    }
    private String getSourceVar(MessageContext msgCtxt) {
        String value = (String) this.properties.get("source-variable");
        if (value == null) return null;
        return resolvePropertyValue(value.trim(), msgCtxt);
    }
    private String getFtpServer(MessageContext msgCtxt) throws Exception {
        return getSimpleRequiredProperty("ftp-server", msgCtxt);
    }
    private String getFtpUser(MessageContext msgCtxt) throws Exception {
        return getSimpleRequiredProperty("ftp-user", msgCtxt);
    }
    private String getFtpPassword(MessageContext msgCtxt) throws Exception {
        return getSimpleRequiredProperty("ftp-password", msgCtxt);
    }
    private String getRemoteFileName(MessageContext msgCtxt) throws Exception {
        return getSimpleRequiredProperty("remote-file-name", msgCtxt);
    }

    private String getInitialDirectory(MessageContext msgCtxt) throws Exception {
        String dest = getSimpleOptionalProperty("initial-directory", msgCtxt);
        if (dest == null) {
            return "/";
        }
        return dest;
    }
    
    private int getFtpPort(MessageContext msgCtxt) throws Exception {
        String port = getSimpleOptionalProperty("ftp-port", msgCtxt);
        if (port == null || port.equals("")) {
            return 21;
        }
        return Integer.parseInt(port,10);
    }

    private String getSimpleOptionalProperty(String propName, MessageContext msgCtxt) throws Exception {
        String value = (String) this.properties.get(propName);
        if (value == null) { return null; }
        value = value.trim();
        if (value.equals("")) { return null; }
        value = resolvePropertyValue(value, msgCtxt);
        if (value == null || value.equals("")) { return null; }
        return value;
    }

    private String getSimpleRequiredProperty(String propName, MessageContext msgCtxt) throws Exception {
        String value = (String) this.properties.get(propName);
        if (value == null) {
            throw new IllegalStateException(propName + " resolves to an empty string.");
        }
        value = value.trim();
        if (value.equals("")) {
            throw new IllegalStateException(propName + " resolves to an empty string.");
        }
        value = resolvePropertyValue(value, msgCtxt);
        if (value == null || value.equals("")) {
            throw new IllegalStateException(propName + " resolves to an empty string.");
        }
        return value;
    }

    // If the value of a property value begins and ends with curlies,
    // eg, {apiproxy.name}, then "resolve" the value by de-referencing
    // the context variable whose name appears between the curlies.
    private String resolvePropertyValue(String spec, MessageContext msgCtxt) {
        if (spec.indexOf('{') > -1 && spec.indexOf('}')>-1) {
            // Replace ALL curly-braced items in the spec string with
            // the value of the corresponding context variable.
            TemplateString ts = new TemplateString(spec);
            Map<String,String> valuesMap = new HashMap<String,String>();
            for (String s : ts.variableNames) {
                valuesMap.put(s, (String) msgCtxt.getVariable(s));
            }
            StrSubstitutor sub = new StrSubstitutor(valuesMap);
            String resolvedString = sub.replace(ts.template);
            return resolvedString;
        }
        return spec;
    }

    public ExecutionResult execute (MessageContext messageContext, ExecutionContext execContext) {
        FtpCalloutResult info = new FtpCalloutResult();
        try {
            // The executes in the IO thread!
            String sourceVar = getSourceVar(messageContext);
            InputStream src = null;
            boolean wantBase64Decode = getWantBase64Decode(messageContext);
            if (sourceVar == null) {
                src = messageContext.getMessage().getContentAsStream();
                // conditionally wrap a decoder around it
                if (wantBase64Decode) {
                    src = new Base64InputStream(src);
                }
            }
            else {
                info.addMessage("Retrieving data from " + sourceVar);
                String sourceData = messageContext.getVariable(sourceVar);
                byte[] b = (wantBase64Decode) ?
                    Base64.decodeBase64(sourceData) : sourceData.getBytes(StandardCharsets.UTF_8);
                src = new ByteArrayInputStream(b);
            }
            String remoteName = getRemoteFileName(messageContext);
            remoteName = remoteName.replaceAll(":","").replaceAll("/","-");

            String ftpServer = getFtpServer(messageContext);
            int ftpPort = getFtpPort(messageContext);
            String user = getFtpUser(messageContext);
            String password = getFtpPassword(messageContext);
            
            info.addMessage("connecting to server " + ftpServer);
            FTPClient ftp = new FTPClient();
            ftp.addProtocolCommandListener(new FtpCommandListener(info));
            ftp.connect(ftpServer, ftpPort);
            ftp.enterLocalPassiveMode();
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                info.setStatus("FAIL");
                info.addMessage("The FTP server refused the connection.");
                messageContext.setVariable(varName("result"), info.toJsonString());
                return ExecutionResult.ABORT;
            }

            if (!ftp.login(user, password)) {
                ftp.disconnect();
                info.setStatus("FAIL");
                info.addMessage("Login failure");
                messageContext.setVariable(varName("result"), info.toJsonString());
                return ExecutionResult.ABORT;
            }
            info.addMessage("logged in as " + user);

            String initialDirectory = getInitialDirectory(messageContext);
            if ((initialDirectory != null) && (!initialDirectory.equals(""))) {
                ftp.changeWorkingDirectory(initialDirectory);
            }

            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            OutputStream os = ftp.storeFileStream(remoteName);
            if (os == null) {
                // cannot open output stream
                info.addMessage("cannot open output stream to " + remoteName);
                info.setStatus("FAIL");
            }
            else {
                byte[] buf = new byte[2048];
                int n;
                while ((n = src.read(buf)) > 0) { os.write(buf,0,n); }
                os.close();
                src.close();
                boolean completed = ftp.completePendingCommand();
                info.addMessage("transfer completed: " + completed);
                info.setStatus("OK");
            }

            ftp.disconnect();
            info.addMessage("All done.");
            messageContext.setVariable(varName("result"), info.toJsonString());
        }
        catch (java.lang.Exception exc1) {
            if (getDebug()) {
                System.out.println(ExceptionUtils.getStackTrace(exc1));
            }
            String error = exc1.toString();
            messageContext.setVariable(varName("exception"), error);
            info.setStatus("FAIL");
            info.addMessage(error);
            messageContext.setVariable(varName("result"), info.toJsonString());
            int ch = error.lastIndexOf(':');
            if (ch >= 0) {
                messageContext.setVariable(varName("error"), error.substring(ch+2).trim());
            }
            else {
                messageContext.setVariable(varName("error"), error);
            }
            messageContext.setVariable(varName("stacktrace"), ExceptionUtils.getStackTrace(exc1));
            return ExecutionResult.ABORT;
        }

        return ExecutionResult.SUCCESS;
    }
}
