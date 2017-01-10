/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.Tools;

import identitycard2.JoinApi.ApiFormat;

/**
 *  store information of user , collect from data file 
 * @author nguyenduyy
 */
public class Info {

    
    
    String field, value, status, expire_date, field_text;
    public Info(){
        field ="";
        value = "";
        status = "";
        expire_date = "";
        field_text = "";
        
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
        if(field.equals(ApiFormat.CL_NAME)){
            field_text = "Name";
        }
        else if(field.equals(ApiFormat.CL_JOB)){
            field_text = "Job";
        }
        else if(field.equals(ApiFormat.CL_DRIVE)){
            field_text = "DriveCard expire at ";
        }
        else if(field.equals(ApiFormat.CL_ACCOUNT)){
            field_text = "Bank Account";
        }
        else if(field.equals(ApiFormat.CL_SERNAME)){
            field_text = "Service name";
        }
        else if(field.equals("service_account")){
            field_text = "Service Account";
        }
        else if(field.equals("expire_date")){
            field_text = "Valid to";
        }
        else field_text = "Other";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(String expire_date) {
        this.expire_date = expire_date;
    }
    public String getField_text() {
        return field_text;
    }

    
    
    
}
