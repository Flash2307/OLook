package com.evalwithin.olook.Data;

import android.graphics.MaskFilter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Pascal on 24/04/2016.
 */
public class Restaurant extends AreaOfInterest
{
    public final static String RESTO_FILENAME = "RestoData.dat";
    public final static String URL_RESTO = "http://api.destinationsherbrooke.com/json/restaurants?SecureKey=SmSE4gaX77120Cj6wlSGwE2t1Sk1om9z2H1X1teBx3rpmddancW51frlB91YNuF9";

    private String phoneNumber;
    private String descCourte;
    private String address;

    public Restaurant (double locX, double locY, String name, String phoneNumber, String descCourte, String address)
    {
        super(locX, locY, name, address + "<br /><b>" + phoneNumber + "</b></a><br />" + descCourte);
        this.phoneNumber = phoneNumber;
        this.descCourte = descCourte;
        this.address = address;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public static String formatPhoneNumber(String strNumber) {
        String formattedPhoneNumber = "";
        if (strNumber.length() == 10) {
            try {
                formattedPhoneNumber = String.format("(%s) %s-%s",
                        strNumber.substring(0, 3),
                        strNumber.substring(3, 6),
                        strNumber.substring(6, 10)
                );
            } catch (NumberFormatException ex) {
                formattedPhoneNumber = strNumber;
            }
        }
        return formattedPhoneNumber;
    }

    public static ArrayList<AreaOfInterest> parseString(String dataString)
    {
        ArrayList<AreaOfInterest> listResto = new ArrayList<>();
        try
        {
            JSONObject jObj = new JSONObject(dataString);
            JSONArray jArray = jObj.getJSONArray("response");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject obj = jArray.getJSONObject(i);

                double locX = obj.optDouble("Longitude", 0);
                double locY = obj.optDouble("Latitude", 0);
                String name = obj.getString("Nom");
                String phoneNbr = obj.optString("NumeroTelephone", "");
                phoneNbr = formatPhoneNumber(phoneNbr);
                String descCourte = obj.optString("DescriptionCourte", "");
                String addr = obj.optString("NumeroCivique", "");
                addr += " " + obj.optString("Rue", "");

                listResto.add(new Restaurant(locX, locY, name, phoneNbr, descCourte, addr.trim()));
            }
            return listResto;
        }
        catch (JSONException e)
        {
            Log.e("Resto JSONException", e.toString());
        }

        return null;
    }
}
