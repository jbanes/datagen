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
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.source.ClasspathSource;
import com.invirgance.convirgance.source.FileSource;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.datagen.modules.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class Products extends AbstractGenerator
{
    private Iterable<JSONObject> names;
    private Iterable<JSONObject> prefixes;
    private Random random;

    public Products(File file, long seed)
    {
        this(file, getNames(), getPrefixes(), seed);
    }
    
    public Products(File file, Iterable<JSONObject> names, Iterable<JSONObject> prefixes, long seed)
    {
        this.file = file;
        this.names = names;
        this.prefixes = prefixes;
        this.random = new Random(seed);
    }
    
    public static Iterable<JSONObject> getNames()
    {
        return new DelimitedInput('|').read(new ClasspathSource("/retail/product_names.txt"));
    }
    
    public static Iterable<JSONObject> getPrefixes()
    {
        return new DelimitedInput('|').read(new ClasspathSource("/retail/product_prefixes.txt"));
    }
    
    private String[] load(Iterable<JSONObject> list)
    {
        ArrayList<String> result = new ArrayList<>();
        
        for(JSONObject item : list)
        {
            result.add(item.getString("Name"));
        }
        
        return result.toArray(String[]::new);
    }
    
    private int computeTotal()
    {
        int total = 0;
        
        for(JSONObject franchise : Context.get("franchises"))
        {
            if(franchise.get("id").equals(-1)) continue;
            
            total += (Integer)franchise.get("Products");
        }
        
        return total;
    }
    
    public void generate()
    {
        final String[] names = load(this.names);
        final String[] prefixes = load(this.prefixes);
        
        JSONOutput output = new JSONOutput();
        JSONObject record;
        
        int index = 1;
        int total = computeTotal();
        boolean prefix;
        
        try(OutputCursor cursor = output.write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"Name\": \"Unknown\", \"id\": -1}"));
            
            for(int i=0; i<total; i++)
            {
                record = new JSONObject();
                prefix = random.nextBoolean();
                
                record.put("id", index++);
                record.put("Name", (prefix ? prefixes[random.nextInt(prefixes.length)] + " " : "") + names[random.nextInt(names.length)]);
                record.put("Price", random.nextInt(500, 25000) / 100.0);
                
                cursor.write(record);
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
}
