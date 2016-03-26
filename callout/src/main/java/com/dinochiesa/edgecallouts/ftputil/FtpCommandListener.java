package com.dinochiesa.edgecallouts.ftputil;

import com.dinochiesa.edgecallouts.ftputil.FtpCalloutResult;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ProtocolCommandEvent;

public class FtpCommandListener implements ProtocolCommandListener {
    private FtpCalloutResult result;
    public FtpCommandListener(FtpCalloutResult result) {
       this.result = result;
    }
    public void protocolCommandSent(ProtocolCommandEvent event) {
        String msg = event.getMessage();
        if (msg.startsWith("PASS")) {
            msg = "PASS xxxx\r\n"; // mask
        }
        this.result.addMessage("Sent: " + msg.trim());
    }
    public void protocolReplyReceived(ProtocolCommandEvent event) {
        this.result.addMessage("Recd: " + event.getMessage().trim());
    }
}
