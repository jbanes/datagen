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
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.source.ClasspathSource;
import com.invirgance.convirgance.source.Source;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.filter.EqualsFilter;
import com.invirgance.datagen.modules.Context;
import com.invirgance.datagen.util.CachedIterable;
import com.invirgance.datagen.util.SegmentationTransformer;
import com.invirgance.datagen.util.WeightedRandom;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class Employees extends AbstractGenerator
{
    
    public static CachedIterable getFirstnames(String sex)
    {
        Source source = new ClasspathSource("/retail/firstnames.txt");
        Iterable<JSONObject> iterable = new DelimitedInput('|').read(source);
        
        if(sex != null) iterable = new EqualsFilter("Sex", sex).transform(iterable);
        
        iterable = new SegmentationTransformer("Number").transform(iterable);
        
        return new CachedIterable(iterable);
    }
    
    public static CachedIterable getLastnames()
    {
        Source source = new ClasspathSource("/retail/lastnames.txt");
        Iterable<JSONObject> iterable = new DelimitedInput('|').read(source);
        
        iterable = new SegmentationTransformer("Number").transform(iterable);
        
        return new CachedIterable(iterable);
    }
    
    private String getName(CachedIterable cache, int value)
    {
        for(JSONObject record : cache)
        {
            if(value >= record.getInt("Start") && value < record.getInt("End"))
            {
                return record.getString("Name");
            }
        }
        
        throw new IllegalArgumentException(value + " is outside the bounds of 0 - " + cache.last().getInt("End"));
    }

    @Override
    public void generate()
    {
        CachedIterable males = getFirstnames("Male");
        CachedIterable females = getFirstnames("Female");
        CachedIterable lastnames = getLastnames();
        
        Iterable<JSONObject> stores = Context.get("stores");
        CachedIterable name;
        JSONObject record;
        
        Random male = new WeightedRandom(getRandom().nextLong(), 0.489); // Population is ~51.1% female
        Random segmentation = new Random(getRandom().nextLong());
        Random duplicate = new WeightedRandom(getRandom().nextLong(), 0.05); // Allow 5% of duplicate names
        
        int employees;
        int franchise = 0;
        int index = 1;
        boolean sex;
        
        HashMap<String,Boolean> lookup = new HashMap<>();
        String first;
        String last;
        
        try(OutputCursor cursor = getOutput().write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\":-1,\"StoreId\":-1,\"Firstname\":\"Unknown\",\"Lastname\":\"Unknown\",\"Sex\":\"Unknown\"}"));
                
            for(JSONObject store : stores)
            {
                if(store.getInt("id") < 0) continue;
                
                employees = store.getInt("Employees");
                
                if(franchise != store.getInt("FranchiseId"))
                {
                    lookup = new HashMap<>();
                    franchise = store.getInt("FranchiseId");
                }

                for(int i=0; i<employees; i++)
                {
                    record = new JSONObject();
                    sex = male.nextBoolean();
                    name = sex ? males : females;
                    first = getName(name, segmentation.nextInt(name.last().getInt("End")));
                    last = getName(lastnames, segmentation.nextInt(lastnames.last().getInt("End")));
                    
// TODO: Need more names for this to work properly
//                    if(lookup.containsKey(first + " " + last) && !duplicate.nextBoolean())
//                    {
//                        while(lookup.containsKey(first + " " + last))
//                        {
//                            sex = male.nextBoolean();
//                            name = sex ? males : females;
//                            first = getName(name, segmentation.nextInt(name.last().getInt("End")));
//                            last = getName(lastnames, segmentation.nextInt(lastnames.last().getInt("End")));
//                        }
//                    }

                    lookup.put(first + " " + last, Boolean.TRUE);
                    
                    record.put("id", index++);
                    record.put("StoreId", store.get("id"));
                    record.put("Firstname", first);
                    record.put("Lastname", last);
                    record.put("Sex", sex ? "Male" : "Female");
                    
                    cursor.write(record);
                }
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
    
    
}
