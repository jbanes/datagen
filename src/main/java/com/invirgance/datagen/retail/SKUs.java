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
import com.invirgance.convirgance.json.JSONArray;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.filter.Filter;
import com.invirgance.datagen.modules.Context;
import com.invirgance.datagen.util.CachedIterable;
import com.invirgance.datagen.util.WeightedRandom;
import java.io.File;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class SKUs extends AbstractGenerator
{
    private Random random;

    public SKUs(File file, long seed)
    {
        this.file = file;
        this.random = new Random(seed);
    }
    
    @Override
    public void generate()
    {
        Iterable<JSONObject> products = Context.get("products");
        JSONOutput output = new JSONOutput();
        int index = 1;
        
        try(OutputCursor cursor = output.write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\":-1,\"Size\":\"Unknown\",\"Color\":\"Unknown\",\"ProductId\":-1},"));
            
            for(JSONObject product : products)
            {
                if(product.getInt("id") < 0) continue;
if(product.getInt("id") == 4) System.out.println("Product: " + product);
                // Computes differentiating attributes and exploode the
                // product into SKUs
                for(JSONObject sku : new Attributes(product, random))
                {
                    sku.put("id", index++);
 if(product.getInt("id") == 4) System.out.println("Sku: " + sku);
                    cursor.write(sku);
                }
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
    
    private class Colors
    {
        public static final String[] COLORS = {
            "Red", "Green", "Blue", "Orange", "Purple", "Lavender",
            "Teal", "Silver", "Black", "Yellow", "Brown", "Gold"
        };
        
        public static final double[] PROBABILITIES = {
            0.5, 0.5, 0.5, 0.2, 0.2, 0.1,
            0.1, 0.4, 0.4, 0.1, 0.1, 0.1
        };
        
        private JSONArray<JSONObject> selected;
        private WeightedRandom random;

        public Colors(Random random)
        {
            JSONObject record;
            
            this.selected = new JSONArray();
            this.random = new WeightedRandom(random.nextLong(), 0.5);
            
            for(int i=0; i<COLORS.length; i++)
            {
                if(this.random.nextBoolean(PROBABILITIES[i])) 
                {
                    record = new JSONObject();
                    
                    record.put("color", COLORS[i]);
                    selected.add(record);
                }
            }
            
            // Pick a single color
            if(this.selected.isEmpty())
            {
                record = new JSONObject();
                
                record.put("color", COLORS[this.random.nextInt(COLORS.length)]);
                selected.add(record);
            }
        }
        
        public JSONArray<JSONObject> explodeSkus(JSONArray<JSONObject> skus)
        {
            JSONArray results = new JSONArray();
            JSONObject record;
            
            for(JSONObject sku : skus)
            {
                for(JSONObject color : selected)
                {
                    record = new JSONObject(sku);

                    record.put("Color", color.get("color"));
                    results.add(record);
                }
            }
            
            return results;
        }
    }
    
    private class Sizes
    {
        public static final String[] SIZES = {
            "Small", "Large", "Medium",  "Extra Large"
        };
        
        private JSONArray<JSONObject> selected;

        public Sizes(Random random)
        {
            int count = random.nextInt(2, SIZES.length);
            JSONObject record;
            
            this.selected = new JSONArray();
            
            for(int i=0; i<count; i++) 
            {
                record = new JSONObject();
                
                record.put("size", SIZES[i]);
                selected.add(record);
            }
        }
        
        public JSONArray<JSONObject> explodeSkus(JSONArray<JSONObject> skus)
        {
            JSONArray results = new JSONArray();
            JSONObject record;
            
            for(JSONObject sku : skus)
            {
                for(JSONObject size : selected)
                {
                    record = new JSONObject(sku);

                    record.put("Size", size.get("size"));
                    results.add(record);
                }
            }
            
            return results;
        }
    }
    
    private class Attributes implements Iterable<JSONObject>
    {
        private boolean size;
        private boolean color;
        
        private JSONObject product;
        private Colors colors;
        private Sizes sizes;
        
        public Attributes(JSONObject product, Random random)
        {
            size = random.nextBoolean();
            color = random.nextBoolean();
            
            this.product = product;
            
            if(color) this.colors = new Colors(random);
            if(size) this.sizes = new Sizes(random);
        }

        @Override
        public Iterator<JSONObject> iterator()
        {
            JSONArray<JSONObject> array = new JSONArray<>();
            JSONObject record = new JSONObject();
            
            record.put("ProductId", product.get("id"));
            record.put("Size", "Default");
            record.put("Color", "Default");
            array.add(record);
            
            if(size) array = sizes.explodeSkus(array);
            if(color) array = colors.explodeSkus(array);
            
            return array.iterator();
        }
    }
}
