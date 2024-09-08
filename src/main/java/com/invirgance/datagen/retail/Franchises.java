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
import com.invirgance.convirgance.json.JSONArray;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.source.ClasspathSource;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.IdentityTransformer;
import com.invirgance.convirgance.transform.sets.UnionIterable;
import com.invirgance.datagen.modules.RetailGenerator;
import java.io.File;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class Franchises extends AbstractGenerator
{
    private Iterable<JSONObject> list;
    
    public Franchises()
    {
        this(getList());
    }
    
    public Franchises(Iterable<JSONObject> list)
    {
        this.list = list;
    }
    
    public static Iterable<JSONObject> getList()
    {
        return new DelimitedInput('|').read(new ClasspathSource("/retail/franchises.txt"));
    }
    
    public void generate()
    {
        JSONOutput output = new JSONOutput();
        Iterable<JSONObject> iterable = this.list;
        
        // Generate employees, stores, and products
        iterable = new IdentityTransformer() {
            
            private int index = 1;
            
            @Override
            public JSONObject transform(JSONObject record) throws ConvirganceException
            {
                int products = random.nextInt(1000, 50000);
                int stores = random.nextInt(100, 5000);
                int employees = Math.min(100*stores, random.nextInt(100, 500) * stores + random.nextInt(-500, 500));
                
                record.put("id", index++);
                record.put("Products", products);
                record.put("Stores", stores);
                record.put("Employees", employees);
                
                return record;
            }
        }.transform(iterable);
        
        iterable = new UnionIterable(new JSONArray<>("[{\"id\": -1,\"Name\":\"Unknown\",\"International\":null,\"Products\":null,\"Stores\":null,\"Employees\":null}]"), iterable);
        
        output.write(new FileTarget(file), iterable);
    }
}
