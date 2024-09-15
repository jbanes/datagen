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
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.source.ClasspathSource;
import com.invirgance.convirgance.source.Source;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.datagen.util.CachedIterable;
import java.util.HashMap;

/**
 *
 * @author jbanes
 */
public class Brands extends AbstractGenerator
{
    public static final int DEFAULT_BRAND_COUNT = 10000;
    
    private String[] adjectives;
    private String[] nouns;
    private int count;
    
    public Brands()
    {
        this(DEFAULT_BRAND_COUNT);
    }
    
    public Brands(int count)
    {
        this(getAdjectives(), getNouns(), count);
    }
    
    public Brands(String[] adjectives, String[] nouns, int count)
    {
        this.adjectives = adjectives;
        this.nouns = nouns;
        this.count = count;
    }
    
    private static String[] getData(String path)
    {
        Source source = new ClasspathSource(path);
        Iterable<JSONObject> records = new DelimitedInput(new String[]{"value"}, '|').read(source);
        
        return new CachedIterable(records).toStringArray("value");
    }
    
    public static String[] getAdjectives()
    {
        return getData("/retail/brand_adjectives.txt");
    }
    
    public static String[] getNouns()
    {
        return getData("/retail/brand_nouns.txt");
    }
    
    public String generateName()
    {
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        
        return adjective + noun;
    }

    @Override
    public void generate()
    {
        HashMap<String,Boolean> lookup = new HashMap<>();
        JSONObject record;
        String name;
        
        try(OutputCursor cursor = getOutput().write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\": -1, \"Name\": \"Unknown\"}"));

            for(int i=0; i<count; i++)
            {
                name = generateName();
                record = new JSONObject();
                
                if(lookup.containsKey(name))
                {
                    i--;
                    continue;
                }
                
                record.put("id", i+1);
                record.put("Name", name);

                lookup.put(name, Boolean.TRUE);
                cursor.write(record);
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
}
