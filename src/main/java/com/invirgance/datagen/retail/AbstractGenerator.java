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

import com.invirgance.convirgance.input.JSONInput;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.source.FileSource;
import com.invirgance.datagen.modules.Generator;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author jbanes
 */
public abstract class AbstractGenerator implements Iterable<JSONObject>
{
    protected File file;
    
    public abstract void generate();

    @Override
    public Iterator<JSONObject> iterator()
    {
        if(!file.exists()) generate();
        
        return new JSONInput().read(new FileSource(file)).iterator();
    }
    
}
