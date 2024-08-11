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
package com.invirgance.datagen.modules;

import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.datagen.retail.Franchises;
import com.invirgance.datagen.retail.ProductCategories;
import com.invirgance.datagen.retail.Products;
import com.invirgance.datagen.retail.ZipCodes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class RetailGenerator implements Generator
{
    public static final long DEFAULT_SEED = 0x1337C0DE;
    
    private File directory;
    private long seed;

    public RetailGenerator(File directory)
    {
        this(directory, DEFAULT_SEED);
    }
    
    public RetailGenerator(File directory, long seed)
    {
        this.directory = directory;
        this.seed = seed;
        
        if(!directory.exists()) directory.mkdirs();
    }

    @Override
    public void generate() throws IOException
    {
        Random random = new Random(seed);
        File temp = Files.createTempDirectory("retailgen-").toFile();
        JSONOutput output = new JSONOutput();
        
        Context.register("franchises", new Franchises(new File(temp, "franchises.json"), random.nextLong()));
        Context.register("productcategories", new ProductCategories(new File(temp, "productcategories.json"), random.nextLong()));
        Context.register("products", new Products(new File(temp, "products.json"), random.nextLong()));
        Context.register("zipcodes", new ZipCodes(new File(temp, "zipcodes.json")));
        
        output.write(new FileTarget(new File(directory, "franchises.json")), Context.get("franchises"));
        output.write(new FileTarget(new File(directory, "productcategories.json")), Context.get("productcategories"));
        output.write(new FileTarget(new File(directory, "products.json")), Context.get("products"));
        output.write(new FileTarget(new File(directory, "zipcodes.json")), Context.get("zipcodes"));
    }
    
    
    
}
