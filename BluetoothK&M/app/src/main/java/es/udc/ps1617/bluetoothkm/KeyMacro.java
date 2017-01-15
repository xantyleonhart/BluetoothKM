package es.udc.ps1617.bluetoothkm;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by santi on 13/01/17.
 */
public class KeyMacro {
    private String text;
    private String keyCodes;

    public KeyMacro(String name, String codes){
        text=name;
        keyCodes=codes;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getKeyCodes() {
        return keyCodes;
    }

    public void setKeyCodes(String keyCodes) {
        this.keyCodes = keyCodes;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        try {
            json.put("name",text);
            json.put("keyCodes",keyCodes);
        } catch (JSONException e) {
            Log.e("JSON","Error converting macro to JSON");
            return null;
        }
        return json;
    }
}
