/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.Tools;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nguyenduyy
 */
public class Permission {
        
    Integer member_type_id ;
    String member_type_text;
    String level;
    public Permission(){
        
    }

    public Integer getMember_type_id() {
        return member_type_id;
    }

    public void setMember_type_id(Integer member_type_id) {
        this.member_type_id = member_type_id;
        //FIXME : need a standard file from Issuer
        switch(member_type_id){
            case 1 : member_type_text = "Person"; break;
            case 2 : member_type_text = "Service"; break;
            case 3 : member_type_text = "Bank";  break;
            case 4: member_type_text = "Police"; break;
            default: member_type_text = "Unknown"; break;
            
        }
    }

    public String getMember_type_text() {
        return member_type_text;
    }
 

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
}
