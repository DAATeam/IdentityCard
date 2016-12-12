/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.VerifyApi;

import java.util.Observable;

/**
 *
 * @author nguyenduyy
 */
public class ObservableData extends Observable{
    Object object = null;
    public ObservableData(){
        
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
        setChanged();
        notifyObservers();
    }
    
}
