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
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.target.FileTarget;

/**
 *
 * @author jbanes
 */
public class Times extends AbstractGenerator
{
    private String zeroPad(int value)
    {
        if(value < 10) return "0" + value;
        
        return Integer.toString(value);
    }

    @Override
    public void generate()
    {
        JSONOutput output = new JSONOutput();
        JSONObject record;
        
        try(OutputCursor cursor = output.write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\":-1,\"Hour\":null,\"Minute\":null,\"TwelveHour\":null,\"TwentyFourHour\":null}"));
            
            for(int hour=0; hour<24; hour++)
            {
                for(int minute=0; minute<60; minute++)
                {
                    record = new JSONObject();

                    record.put("id", (hour * 100) + minute);
                    record.put("Hour", hour);
                    record.put("Minute", minute);
                    record.put("TwelveHour", (hour%12 == 0 ? 12 : (hour%12)) + ":" + zeroPad(minute) + " " + (hour > 11 ? "PM" : "AM"));
                    record.put("TwentyFourHour", zeroPad(hour) + ":" + zeroPad(minute));

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
