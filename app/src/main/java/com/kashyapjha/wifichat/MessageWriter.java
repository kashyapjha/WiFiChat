package com.kashyapjha.wifichat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MessageWriter
{
    public static void writeSentMessage(JSONObject msgObject,String path)
    {
        File file = new File(path);
        try
        {
            if(!file.exists())
            {
                file.createNewFile();
                FileWriter fw=new FileWriter(file);
                fw.append("Me: "+msgObject.getString("Message"));
                fw.append("\n");
                fw.flush();
                fw.close();
            }
            else
            {
                FileWriter fw=new FileWriter(file,true);
                fw.append("Me: "+msgObject.getString("Message"));
                fw.append("\n");
                fw.flush();
                fw.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public static void writeReceivedMessage(JSONObject msgObject,String path)
    {
        try
        {
            File file = new File(path);

            if(!file.exists())
            {
                file.createNewFile();
                FileWriter fw=new FileWriter(file);
                fw.append("Them: "+msgObject.getString("Message"));
                fw.append("\n");
                fw.flush();
                fw.close();
            }
            else
            {
                FileWriter fw=new FileWriter(file,true);
                fw.append("Them: "+msgObject.getString("Message"));
                fw.append("\n");
                fw.flush();
                fw.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }
}
