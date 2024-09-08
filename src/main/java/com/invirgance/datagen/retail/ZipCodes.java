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
package com.invirgance.datagen.retail;

import com.invirgance.convirgance.ConvirganceException;
import com.invirgance.convirgance.input.DelimitedInput;
import com.invirgance.convirgance.input.JSONInput;
import com.invirgance.convirgance.json.JSONArray;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.source.ClasspathSource;
import com.invirgance.convirgance.source.FileSource;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.IdentityTransformer;
import com.invirgance.convirgance.transform.InsertKeyTransformer;
import com.invirgance.convirgance.transform.sets.UnionIterable;
import java.io.File;
import java.util.Iterator;

/**
 *
 * @author jbanes
 */
public class ZipCodes extends AbstractGenerator
{
    private Iterable<JSONObject> usList;
    private Iterable<JSONObject> japanList;

    public ZipCodes()
    {
        this(getUSList(), getJapanList());
    }
    
    public ZipCodes(Iterable<JSONObject> usList, Iterable<JSONObject> japanList)
    {
        this.usList = usList;
        this.japanList = japanList;
    }
    
    public static Iterable<JSONObject> getUSList()
    {
        return new DelimitedInput('|').read(new ClasspathSource("/retail/us-zipcodes.txt"));
    }
    
    public static Iterable<JSONObject> getJapanList()
    {
        return new DelimitedInput('|').read(new ClasspathSource("/retail/jp-zipcodes.txt"));
    }
    
    public void generate()
    {
        JSONOutput output = new JSONOutput();
        Iterable<JSONObject> unknown = new JSONArray<>("[{\"ZipCode\": \"Unknown\", \"id\": -1, \"CountryCode\": \"Unknown\"}]");
        Iterable<JSONObject> us = new InsertKeyTransformer("CountryCode", "US").transform(this.usList);
        Iterable<JSONObject> jp = new InsertKeyTransformer("CountryCode", "JP").transform(this.japanList);
        Iterable<JSONObject> iterable = new UnionIterable(us, jp);
        
        iterable = new IdentityTransformer() {
            private int index = 1;
            
            @Override
            public JSONObject transform(JSONObject record) throws ConvirganceException
            {
                record.put("id", index++);
                
                return record;
            }
        }.transform(iterable);
        
        iterable = new UnionIterable(unknown, iterable);
        
        output.write(new FileTarget(file), iterable);
    }
    
    @Override
    public Iterator<JSONObject> iterator()
    {
        if(!file.exists()) generate();
        
        return new JSONInput().read(new FileSource(file)).iterator();
    }
    
}
