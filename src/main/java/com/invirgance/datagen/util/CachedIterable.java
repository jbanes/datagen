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
package com.invirgance.datagen.util;

import com.invirgance.convirgance.ConvirganceException;
import com.invirgance.convirgance.json.JSONArray;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.transform.filter.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author jbanes
 */
public class CachedIterable implements Iterable<JSONObject>
{
    private JSONArray<JSONObject> cache;
    private JSONObject[] lookup;
    
    public CachedIterable(Iterable<JSONObject> iterable)
    {
        cache = new JSONArray<>();
        
        for(JSONObject record : iterable)
        {
            cache.add(record);
        }
    }

    @Override
    public Iterator<JSONObject> iterator()
    {
        return cache.iterator();
    }
    
    public JSONObject get(int index)
    {
        return this.cache.get(index);
    }
    
//    private JSONObject find(int offset, int id, int last)
//    {
//        JSONObject record = cache.get(offset);
//        int recordId = record.getInt("id");
//        
//        if(recordId == id) return record;
//        if(recordId < id && last <= id) return find(offset+1, id, recordId);
//        if(recordId > id && last >= id) return find(offset-1, id, recordId);
//        
//        System.err.println("RecordId: " + recordId);
//        System.err.println("Last: " + last);
//        System.err.println("Offset: " + offset);
//        System.err.println(recordId + " < " + id + " && " + last + " < " + id + " = " + (recordId < id && last < id));
//        System.err.println(recordId + " > " + id + " && " + last + " > " + id + " = " + (recordId > id && last > id));
//        
//        throw new IllegalArgumentException("id " + id + " not found in cache!");
//    }
    
    private JSONObject find(int id, int min, int max)
    {
        int offset = ((max-min) >> 1) + min;
        JSONObject record = cache.get(offset);
        int recordId = record.getInt("id");
        
        if(recordId == id) return record;
        if(min >= max) throw new IllegalArgumentException("id " + id + " not found in cache!");
        if(id < recordId) return find(id, min, offset); 
        if(id > recordId) return find(id, offset, max); 
            
        throw new IllegalArgumentException("id " + id + " not found in cache!");
    }
    
    public JSONObject find(int id)
    {
        if(cache.size() > (1024 * 1024)) return find(id, 0, cache.size());
        
        if(lookup == null)
        {
            lookup = new JSONObject[cache.get(cache.size()-1).getInt("id")+1];
            
            for(JSONObject record : cache) lookup[record.getInt("id")] = record;
        }
        
        return lookup[id];
    }
    
    public int size()
    {
        return cache.size();
    }
    
    public CachedIterable getFiltered(Filter filter)
    {
        return new CachedIterable(filter.transform(cache));
    }
    
    public String[] toStringArray(String key)
    {
        ArrayList<String> list = new ArrayList<>();
        HashMap<String,Boolean> lookup = new HashMap<>();
        String value;
        
        for(JSONObject record : this)
        {
            value = record.getString(key, null);
            
            if(value != null && !lookup.containsKey(value)) 
            {
                list.add(value);
                lookup.put(value, Boolean.TRUE);
            }
        }
        
        return list.toArray(String[]::new);
    }
}
