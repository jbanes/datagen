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
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author jbanes
 */
public class BrandsTest
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
    public void testStatics()
    {
        String[] adjectives = Brands.getAdjectives();
        String[] nouns = Brands.getNouns();
        
        assertTrue(nouns.length > 1);
        assertTrue(nouns.length > 1);
        
        for(String adjective : adjectives)
        {
            assertNotNull(adjective);
            assertTrue(adjective.trim().length() == adjective.length());
            assertTrue(adjective.trim().length() > 0);
        }
        
        for(String noun : nouns)
        {
            assertNotNull(noun);
            assertTrue(noun.trim().length() == noun.length());
            assertTrue(noun.trim().length() > 0);
        }
    }
    
    @Test
    public void testGenerate()
    {
        Brands brands = new Brands();
        HashSet set = new HashSet();
        int count = 0;
        
        brands.setFile(new File("target/temp/tests/retail/brands.json"));
        
        for(JSONObject record : brands)
        {
            if(record.getInt("id") == -1)
            {
                assertEquals("Unknown", record.getString("Name"));
                continue;
            }
            
            set.add(record.getString("Name"));
            
            count++;
            
            assertEquals(count, record.getInt("id"));
        }
        
        assertEquals(10000, count);
        assertEquals(10000, set.size());
    }
    
}
