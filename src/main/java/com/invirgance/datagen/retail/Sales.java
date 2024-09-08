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
import com.invirgance.convirgance.json.JSONArray;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.filter.Filter;
import com.invirgance.datagen.modules.Context;
import com.invirgance.datagen.util.CachedIterable;
import com.invirgance.datagen.util.WeightedRandom;
import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author jbanes
 */
public class Sales extends AbstractGenerator
{
    private int days;

    public Sales(File file, long seed)
    {
        this.file = file;
        this.random = new Random(seed);
        this.days = Context.getSetting("days", 7);
    }

    public int getDays()
    {
        return days;
    }

    public void setDays(int days)
    {
        this.days = days;
    }

    private int[] generateCustomers(int count)
    {
        int[] customers = new int[24];
        
        // Manually tuned to simulate busy / slow periods through the day
        for(int i=0; i<8; i++) customers[i] = 0;
        for(int i=8; i<9; i++) customers[i] = random.nextInt(count / 12 / 8);
        for(int i=9; i<11; i++) customers[i] = random.nextInt(count / 12 / 4);
        for(int i=11; i<2; i++) customers[i] = random.nextInt(count / 12 * 2);
        for(int i=2; i<5; i++) customers[i] = random.nextInt(count / 12 / 2);
        for(int i=11; i<2; i++) customers[i] = random.nextInt(count / 12 * 2);
        for(int i=2; i<7; i++) customers[i] = random.nextInt(count / 12 * 3);
        for(int i=7; i<20; i++) customers[i] = random.nextInt(count / 12 / 4);
        for(int i=20; i<customers.length; i++) customers[i] = 0;
        
        return customers;
    }
    
    @Override
    public void generate()
    {
        Iterable<JSONObject> franchises = Context.get("franchises");
        CachedIterable products = new CachedIterable(Context.get("products"));
        Iterable<JSONObject> skus = Context.get("skus");
        
        CachedIterable selectedProducts;
        CachedIterable selectedSkus;
        
        JSONOutput output = new JSONOutput();
        JSONObject record;
        
        String receiptPrefix = Integer.toString(random.nextInt(1000, 10000));
        int[] customers;
        int index = 1;
        int count;
        
        System.out.println("Generating " + days + " days of data...");
        
        try(OutputCursor cursor = output.write(new FileTarget(file)))
        {
            cursor.write(new JSONObject("{\"id\":-1,\"Size\":\"Unknown\",\"Color\":\"Unknown\",\"ProductId\":-1},"));
            
            for(JSONObject franchise : franchises)
            {
                if(franchise.getInt("id") < 0) continue;
                
                selectedProducts = products.getFiltered(new SelectionFilter(franchise, random));
                selectedSkus = new CachedIterable(new InFilter("ProductId", selectedProducts.toStringArray("id")).transform(skus));
                customers = generateCustomers(selectedProducts.size() / 2);

                System.out.print(franchise.getString("Name") + ": " + NumberFormat.getInstance().format(selectedProducts.size()) + " products / " + NumberFormat.getInstance().format(selectedSkus.size()) + " skus / ");

                for(JSONObject customer : new Customers(customers, this.days, receiptPrefix))
                {
                    for(JSONObject sale : new Sale(customer, selectedProducts, selectedSkus))
                    {
                        sale.put("id", index++);
                        
                        cursor.write(sale);
                    }
                }
                
                System.out.println(NumberFormat.getInstance().format(index) + " sales");
            }
        }
        catch(Exception e)
        {
            throw new ConvirganceException(e);
        }
    }
    
    private class SelectionFilter implements Filter
    {
        private Random random;
        private int total;
        private int count;

        public SelectionFilter(JSONObject franchise, Random random)
        {
            this.random = new WeightedRandom(random.nextLong(), 0.25);
            this.total = franchise.getInt("Products");
            this.count = 0;
        }
        
        @Override
        public boolean filter(JSONObject record)
        {
            if(record.getInt("id") < 0) return false;
            if(count >= total) return false;
            
            if(random.nextBoolean())
            {
                count++;
                
                return true;
            }
            
            return false;
        }
    }
    
    private class InFilter implements Filter
    {
        private HashMap<String,Boolean> lookup;
        private String key;
        
        public InFilter(String key, String[] values)
        {
            this.key = key;
            this.lookup = new HashMap<>();
            
            for(String value : values) lookup.put(value, Boolean.TRUE);
        }
        
        @Override
        public boolean filter(JSONObject record)
        {
            return lookup.containsKey(record.getString(key));
        }
    }
    
    private class Sale implements Iterable<JSONObject>
    {
        private JSONArray<JSONObject> lines = new JSONArray<>();
        
        public Sale(JSONObject customer, CachedIterable products, CachedIterable skus)
        {
            double goal = random.nextDouble(12.0, 500.0);
            double total = 0;
            double price;
            
            JSONObject record;
            JSONObject product;
            JSONObject sku;
            int quantity;
            
            while(total < goal)
            {
                record = new JSONObject(customer);
                sku = skus.get(random.nextInt(skus.size()));
                product = products.find(sku.getInt("ProductId"));
                quantity = random.nextInt(1, 5); // TODO: This needs to be a smarter calc
                
                price = (double)product.get("Price");
                total += price * quantity;
                
                record.put("SkuId", sku.get("id"));
                record.put("ProductId", sku.get("ProductId"));
                record.put("Quantity", quantity);
                record.put("UnitPrice", price);
                record.put("DiscountPrice", price);
                record.put("TotalPrice", price * quantity);
                
                lines.add(record);
            }
        }
        
        @Override
        public Iterator<JSONObject> iterator()
        {
            return lines.iterator();
        }
    }
    
    private class Customers implements Iterable<JSONObject>
    {
        private static final long DAY = 1000 * 60 * 60 * 24;
        
        private JSONArray<JSONObject> records;

        public Customers(int[] customers, int days, String receiptPrefix)
        {
            Date base = new Date(new Date().getTime() - (DAY * days));
            Date date;
            
            JSONObject record;
            int count;
            int receipt = 1000000;
            
            this.records = new JSONArray<>();
            
            for(int day=0; day<days; day++)
            {
                date = new Date(base.getTime() + (day * DAY));

                for(int hour=0; hour<24; hour++)
                {
                    for(int minute=0; minute<60; minute++)
                    {
                        count = customers[hour]/60;

                        for(int i=0; i<count; i++)
                        {
                            record = new JSONObject();

                            record.put("Receipt", receiptPrefix + (receipt++));
                            record.put("DateId", ((date.getYear() + 1900) * 10000) + ((date.getMonth() + 1) * 100) + date.getDate());
                            record.put("TimeId", (hour * 100) + minute);

                            records.add(record);
                        }
                    }
                }
            }
        }
        
        @Override
        public Iterator<JSONObject> iterator()
        {
            return records.iterator();
        }
    }
    
}
