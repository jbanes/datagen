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

package com.invirgance.datagen;

import com.invirgance.datagen.modules.Context;
import com.invirgance.datagen.modules.RetailGenerator;
import java.io.File;

/**
 *
 * @author jbanes
 */
public class Datagen 
{
    public static void main(String[] args) throws Exception
    {
        String dir = null;
        String format = null;
                
        String[] parsed;
        
        for(int i=0; i<args.length; i++)
        {
            if(args[i].equals("--setting")) 
            {
                if(args.length-1 < i+1) break;
                
                parsed = args[++i].split("=");
                
                Context.setSetting(parsed[0], parsed[1]);
            }
            else if(dir == null)
            {
                dir = args[i];
            }
            else if(format == null)
            {
                format = args[i];
            }
            else
            {
                dir = null;
                break;
            }
        }
        
        if(args.length < 1 || dir == null)
        {
            System.err.println("Usage: java -jar datagen.jar <output directory> [csv|json]");
            return;
        }
        
        if(format != null) Context.setSetting("format", format);
        
        new RetailGenerator(new File(dir)).generate();
    }
}
