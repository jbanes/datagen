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
import com.invirgance.datagen.modules.Context;
import java.io.File;
import java.util.Date;

/**
 *
 * @author jbanes
 */
public class Dates extends AbstractGenerator
{
    private static final long DAY = 1000 * 60 * 60 * 24;
    private static final String[] DAYS_OF_WEEK = {
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };
    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "Decemeber"
    };
    
    private int days;

    public Dates()
    {
        this.days = Context.getSetting("days", 7);
    }
    
    @Override
    public void generate()
    {
        Date base = new Date(new Date().getTime() - (DAY * days));
        Date date;
        
        JSONObject record;
        
        try(OutputCursor cursor = getOutput().write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\":-1,\"Year\":null,\"Month\":null,\"Day\":null,\"DayName\":null,\"MonthName\":null}"));
            
            for(int day=0; day<days; day++)
            {
                date = new Date(base.getTime() + (day * DAY));
                record = new JSONObject();

                record.put("id", ((date.getYear() + 1900) * 10000) + ((date.getMonth() + 1) * 100) + date.getDate());
                record.put("Year", date.getYear() + 1900);
                record.put("Month", date.getMonth() + 1);
                record.put("Day", date.getDate());
                record.put("DayName", DAYS_OF_WEEK[date.getDay()]);
                record.put("MonthName", MONTHS[date.getMonth()]);
                
                cursor.write(record);
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
    
}
