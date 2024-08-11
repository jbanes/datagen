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
import com.invirgance.convirgance.transform.sets.UnionIterable;
import com.invirgance.datagen.modules.RetailGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class ProductCategories extends AbstractGenerator
{
    private Iterable<JSONObject> list;
    private Random random;
    
    public ProductCategories(File file)
    {
        this(file, getList(), RetailGenerator.DEFAULT_SEED);
    }
    
    public ProductCategories(File file, long seed)
    {
        this(file, getList(), seed);
    }
    
    public ProductCategories(File file, Iterable<JSONObject> list, long seed)
    {
        this.file = file;
        this.list = list;
        this.random = new Random(seed);
    }
    
    public static Iterable<JSONObject> getList()
    {
        return new DelimitedInput('|').read(new ClasspathSource("/retail/product_categories.txt"));
    }
    
    public void generate()
    {
        JSONOutput output = new JSONOutput();
        Iterable<JSONObject> iterable = this.list;
        
        iterable = new IdentityTransformer() {
            
            private int index = 1;
            
            @Override
            public JSONObject transform(JSONObject record) throws ConvirganceException
            {
                record.put("id", index++);
                
                return record;
            }
        }.transform(iterable);
        
        iterable = new UnionIterable(new JSONArray<>("[{\"Name\": \"Unknown\", \"id\": -1}]"), iterable);
        
        output.write(new FileTarget(file), iterable);
    }
}
