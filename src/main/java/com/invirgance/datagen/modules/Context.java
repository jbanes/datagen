/*
 * Copyright 2024 INVIRGANCE LLC

Permission is hereby granted, free of charge, to any person obtaining a copy 
of this software and associated documentation files (the “Software”), to deal 
in the Software without restriction, including without limitation the rights to 
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
of the Software, and to permit persons to whom the Software is furnished to do 
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
SOFTWARE.
 */
package com.invirgance.datagen.modules;

import com.invirgance.convirgance.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jbanes
 */
public class Context
{
    private static ThreadLocal<Map<String,Iterable<JSONObject>>> local = new ThreadLocal<>();
    private static ThreadLocal<Map<String,String>> settings = new ThreadLocal<>();
    
    private static Map<String,Iterable<JSONObject>> getLocal()
    {
        Map<String,Iterable<JSONObject>> map = local.get();
        
        if(map == null)
        {
            map = new HashMap<>();
            
            local.set(map);
        }
        
        return map;
    }
    
    private static Map<String,String> getSettings()
    {
        Map<String,String> map = settings.get();
        
        if(map == null)
        {
            map = new HashMap<>();
            
            settings.set(map);
        }
        
        return map;
    }
    
    public static void register(String name, Iterable<JSONObject> iterable)
    {
        getLocal().put(name, iterable);
    }
    
    public static Iterable<JSONObject> get(String name)
    {
        return getLocal().get(name);
    }
    
    public static void reset()
    {
        local.remove();
    }
    
    public static String getSetting(String key)
    {
        return getSettings().get(key);
    }
    
    public static String getSetting(String key, String defautValue)
    {
        String value = getSettings().get(key);
        
        if(value == null) return defautValue;
        
        return value;
    }
    
    public static int getSetting(String key, int defautValue)
    {
        String value = getSettings().get(key);
        
        if(value == null) return defautValue;
        if(value.startsWith("0x")) return Integer.parseInt(value.substring(2), 16);
        
        return Integer.parseInt(value);
    }
    
    public static long getSetting(String key, long defautValue)
    {
        String value = getSettings().get(key);
        
        if(value == null) return defautValue;
        if(value.startsWith("0x")) return Long.parseLong(value.substring(2), 16);
        
        return Long.parseLong(value);
    }
    
    public static void setSetting(String key, String value)
    {
        getSettings().put(key, value);
    }
}
