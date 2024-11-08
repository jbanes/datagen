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

import com.invirgance.convirgance.json.JSONObject;
import java.io.File;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author jbanes
 */
public class ZipCodesTest
{
    private static void delete(File dir)
    {
        if(!dir.exists()) return;
        
        for(File file : dir.listFiles())
        {
            if(file.isDirectory()) delete(file);
            else file.delete();
        }
        
        dir.delete();
    }
    
    @BeforeAll
    public static void setup()
    {
        delete(new File("target/temp/tests/retail"));
        
        new File("target/temp/tests/retail").mkdirs();
    }
    
    @Test
    public void testList()
    {
        ZipCodes codes = new ZipCodes();
        int index = -1;
        int id;
        
        codes.setFile(new File("target/temp/tests/retail/zipcodes.json"));
        
        for(JSONObject zipcode : codes)
        {
            if(zipcode.getInt("id") < 1) continue;
            
            id = zipcode.getString("CountryCode").equals("US") ? 0 : 1;
            id = zipcode.getInt("ZipCode") * 100 + id;
            
            assertEquals(id , zipcode.get("id"));
            
            if(index == 0) index++;
            else assertTrue((zipcode.get("CountryCode").equals("JP") || zipcode.get("CountryCode").equals("US")));
        }
    }
    
}
