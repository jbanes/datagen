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
import com.invirgance.datagen.modules.RetailGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
    private Random random;
    
    public Brands(File file)
    {
        this(file, RetailGenerator.DEFAULT_SEED);
    }
    
    public Brands(File file, long seed)
    {
        this(file, seed, DEFAULT_BRAND_COUNT);
    }
    
    public Brands(File file, long seed, int count)
    {
        this(file, getAdjectives(), getNouns(), seed, count);
    }
    
    public Brands(File file, String[] adjectives, String[] nouns, long seed, int count)
    {
        this.file = file;
        this.adjectives = adjectives;
        this.nouns = nouns;
        this.random = new Random(seed);
        this.count = count;
    }
    
    public static String[] getAdjectives()
    {
        Source source = new ClasspathSource("/retail/brand_adjectives.txt");
        Iterable<JSONObject> records = new DelimitedInput(new String[]{"adjective"}, '|').read(source);
        ArrayList<String> list = new ArrayList<>();
        
        for(JSONObject record : records) list.add(record.getString("adjective"));
        
        return list.toArray(String[]::new);
    }
    
    public static String[] getNouns()
    {
        Source source = new ClasspathSource("/retail/brand_nouns.txt");
        Iterable<JSONObject> records = new DelimitedInput(new String[]{"noun"}, '|').read(source);
        ArrayList<String> list = new ArrayList<>();
        
        for(JSONObject record : records) list.add(record.getString("noun"));
        
        return list.toArray(String[]::new);
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
        JSONOutput output = new JSONOutput();
        HashMap<String,Boolean> lookup = new HashMap<>();
        
        JSONObject record;
        String name;
        
        try(OutputCursor cursor = output.write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"Name\": \"Unknown\", \"id\": -1}"));

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
