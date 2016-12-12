/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.HttpServer;

import identitycard2.HttpServer.SessionHandler.SessionStatusEnum;
import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author nguyenduyy
 */
public class SessionData {
    
    
    private String partnerName = null;
    private String requestedPermission = null;
    private String sessionId  =null;
    private Date timestamp = null;
    private SessionStatusEnum status = null;
    private String basename = null;

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }
    public SessionData(){
        status = SessionStatusEnum.INIT;
    }

    
    public String getRequestedPermission() {
        return requestedPermission;
    }

    public void setRequestedPermission(String requestedPermission) {
        this.requestedPermission = requestedPermission;
    }

    public SessionStatusEnum getStatus() {
        return status;
    }

    public void setStatus(SessionStatusEnum status) {
        this.status = status;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getSessionId(){
        return sessionId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

        

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    
}
