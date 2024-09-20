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

import com.invirgance.convirgance.ConvirganceException;
import com.invirgance.convirgance.output.BSONOutput;
import com.invirgance.convirgance.output.DelimitedOutput;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.Output;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.datagen.retail.*;
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
        this(directory, Context.getSetting("seed", DEFAULT_SEED));
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
//        File temp = Files.createTempDirectory("retailgen-").toFile();
        File temp = directory;
        
        String format = Context.getSetting("format", "csv");
        Output output;
        
        AbstractGenerator generator;
        String[] generators = new String[] {
            "franchises", "categories", "brands", "products", "zipcodes",
            "stores", "skus", "employees", "sales", "dates", "times"
        };
        
        Context.register("franchises", new Franchises());
        Context.register("categories", new Categories());
        Context.register("brands", new Brands());
        Context.register("products", new Products());
        Context.register("zipcodes", new ZipCodes());
        Context.register("stores", new Stores());
        Context.register("skus", new SKUs());
        Context.register("employees", new Employees());
        Context.register("sales", new Sales());
        Context.register("dates", new Dates());
        Context.register("times", new Times());
        
        switch(format)
        {
            case "csv":
                output = new DelimitedOutput(',');
                break;
            
            case "json":
                output = new JSONOutput();
                break;
            
            case "bson":
                output = new BSONOutput();
                break;
                
            default:
                throw new ConvirganceException("Unknown format: " + Context.getSetting("format"));
        }
        
        for(String name : generators)
        {
            System.out.println("Generating " + name + "...");
            
            generator = (AbstractGenerator)Context.get(name);
            
            generator.setFile(new File(temp, name + ".tmp"));
            generator.setRandom(random.nextLong());

            output.write(new FileTarget(new File(directory, name + "." + format)), Context.get(name));
        }
        
        // Cleanup
        for(String name : generators) 
        {
            generator = (AbstractGenerator)Context.get(name);

            if(Context.getSetting("deletetemp", true)) generator.getFile().delete();
        }
    }
}
