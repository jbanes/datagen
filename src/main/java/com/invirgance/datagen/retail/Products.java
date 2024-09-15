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
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.source.ClasspathSource;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.filter.EqualsFilter;
import com.invirgance.datagen.modules.Context;
import com.invirgance.datagen.util.CachedIterable;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class Products extends AbstractGenerator
{
    private Iterable<JSONObject> names;
    private Iterable<JSONObject> prefixes;

    public Products()
    {
        this(getNames(), getPrefixes());
    }
    
    public Products(Iterable<JSONObject> names, Iterable<JSONObject> prefixes)
    {
        this.names = names;
        this.prefixes = prefixes;
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
        return new CachedIterable(list).toStringArray("Name");
    }
    
    private String[] selectCategories(String[] categories)
    {
        ArrayList<String> selected = new ArrayList<>();
        int total = random.nextInt(1, categories.length);
        int item;
        
        while(selected.size() < total)
        {
            item = random.nextInt(1, categories.length);

            if(selected.contains(categories[item])) continue;

            selected.add(categories[item]);
        }
        
        return selected.toArray(String[]::new);
    }
    
    private String generateName(String[] prefixes, String[] names, JSONObject brand, JSONObject type)
    {
        boolean prefix = random.nextBoolean();
        boolean branded = random.nextBoolean();
        
        if(branded)
        {
            return brand.getString("Name") + " " + type.get("SubType");
        }
        
        return (prefix ? prefixes[random.nextInt(prefixes.length)] + " " : "") + names[random.nextInt(names.length)];
    }
    
    @Override
    public void generate()
    {
        CachedIterable types = new CachedIterable(Context.get("categories"));
        CachedIterable type;
        
        String[] names = load(this.names);
        String[] prefixes = load(this.prefixes);
        String[] categories = new CachedIterable(Context.get("categories")).toStringArray("Name");
        
        JSONObject record;
        String[] selected;
        
        int index = 1;
        int total;
        int typeId;
        
        try(OutputCursor cursor = getOutput().write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\":-1,\"Name\":\"Unknown\",\"Price\":null,\"BrandId\":-1,\"CategoryId\":-1}"));
            
            for(JSONObject brand : Context.get("brands"))
            {
                if(brand.getInt("id") < 0) continue;
                
                selected = selectCategories(categories);

                for(String item : selected)
                {
                    type = types.getFiltered(new EqualsFilter("Name", item));
                    total = random.nextInt(1, type.size());
                    
                    for(int i=0; i<total; i++)
                    {
                        record = new JSONObject();
                        typeId = random.nextInt(type.size());

                        record.put("id", index++);
                        record.put("Name", generateName(prefixes, names, brand, type.get(typeId)));
                        record.put("Price", random.nextInt(100, 25000) / 100.0);
                        record.put("BrandId", brand.get("id"));
                        record.put("CategoryId", type.get(typeId).get("id"));

                        cursor.write(record);
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
}
