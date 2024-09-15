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
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.filter.EqualsFilter;
import com.invirgance.datagen.modules.Context;
import com.invirgance.datagen.util.CachedIterable;

/**
 *
 * @author jbanes
 */
public class Stores extends AbstractGenerator
{
    private String getCountryCode(String country)
    {
        if(country.equals("US")) return "US";
        if(country.equals("Japan")) return "JP";
        if(country.equals("World")) return null;
        
        throw new IllegalArgumentException("Unrecognized country " + country);
    }
    
    @Override
    public void generate()
    {
        Iterable<JSONObject> franchises = Context.get("franchises");
        CachedIterable zipcodes = new CachedIterable(Context.get("zipcodes"));
        
        CachedIterable us = zipcodes.getFiltered(new EqualsFilter("CountryCode", "US"));
        CachedIterable japan = zipcodes.getFiltered(new EqualsFilter("CountryCode", "JP"));
        CachedIterable lookup;
        
        JSONObject store;
        JSONObject zipcode;
        
        String country;
        int index = 1;
        int stores;
        
        int total;
        int perStore;
        int employees;

        try(OutputCursor cursor = getOutput().write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\": -1,\"StoreNumber\":null,\"Name\": \"Unknown\",\"ZipCode\":null,\"CountryCode\":\"XX\",\"FranchiseId\":-1,\"Employees\":null}"));
            
            for(var franchise : franchises)
            {
                if(franchise.getInt("id") == -1) continue;
                
                stores = franchise.getInt("Stores");
                country = getCountryCode(franchise.getString("International"));
                total = franchise.getInt("Employees");
                perStore = (int)(total * 0.9 / stores);

                for(int i=0; i<stores; i++)
                {
                    employees = random.nextInt((int)(perStore * 0.7), (int)(perStore * 1.3));

                    if(country == null) lookup = (random.nextDouble() > 0.1 ? us : japan);
                    else if(country.equals("US")) lookup = us;
                    else if(country.equals("JP")) lookup = japan;
                    else throw new IllegalArgumentException("Unknown country code: " + country);
                    
                    zipcode = lookup.get(random.nextInt(lookup.size()));
                    store = new JSONObject();
                    
                    store.put("id", index++);
                    store.put("StoreNumber", String.format("%05d", index));
                    store.put("Name", zipcode.getString("LocaleName", zipcode.getString("CityName")) + " - Store #" + String.format("%05d", index));
                    store.put("ZipCode", zipcode.get("ZipCode"));
                    store.put("CountryCode", zipcode.get("CountryCode"));
                    store.put("FranchiseId", franchise.get("id"));
                    store.put("Employees", employees);
                    
                    cursor.write(store);
                }
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
    
}
