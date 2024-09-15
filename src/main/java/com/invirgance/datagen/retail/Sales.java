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
import com.invirgance.convirgance.input.BSONInput;
import com.invirgance.convirgance.input.JSONInput;
import com.invirgance.convirgance.json.JSONArray;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.BSONOutput;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.OutputCursor;
import com.invirgance.convirgance.source.FileSource;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.transform.filter.EqualsFilter;
import com.invirgance.convirgance.transform.filter.Filter;
import com.invirgance.datagen.modules.Context;
import com.invirgance.datagen.util.CachedIterable;
import com.invirgance.datagen.util.WeightedRandom;
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

    public Sales()
    {
        this.days = Context.getSetting("days", 365);
    }

    public int getDays()
    {
        return days;
    }

    public void setDays(int days)
    {
        this.days = days;
    }
    
    private int sum(int[] customers)
    {
        int total = 0;
        
        for(int count : customers)
        {
            total += count;
        }
        
        return total;
    }

    private int[] generateCustomers(int count)
    {
        int[] customers = new int[24];
        int total;
        int reduce;
        
        int twelfth = Math.max(count / 12, 24);
        
        count = Math.max(count, 24 * 12);
        
        // Manually tuned to simulate busy / slow periods through the day
        for(int i=0; i<8; i++) customers[i] = 0;
        for(int i=8; i<9; i++) customers[i] = random.nextInt(twelfth / 8);
        for(int i=9; i<11; i++) customers[i] = random.nextInt(twelfth / 4);
        for(int i=11; i<2; i++) customers[i] = random.nextInt(twelfth * 2);
        for(int i=2; i<5; i++) customers[i] = random.nextInt(twelfth / 2);
        for(int i=11; i<2; i++) customers[i] = random.nextInt(twelfth * 2);
        for(int i=2; i<7; i++) customers[i] = random.nextInt(twelfth * 3);
        for(int i=7; i<20; i++) customers[i] = random.nextInt(twelfth / 4);
        for(int i=20; i<customers.length; i++) customers[i] = 0;
        
        while((total = sum(customers)) > count)
        {
            reduce = Math.max(total/12, 1);
            
            for(int i=0; i<customers.length; i++)
            {
                if(customers[i] > reduce) customers[i] -= reduce;
                else if(customers[i] > 1) customers[i]--;
            }
        }
        
        return customers;
    }
    
    @Override
    public void generate()
    {
        Iterable<JSONObject> franchises = Context.get("franchises");
        Iterable<JSONObject> stores = Context.get("stores");
        CachedIterable products = new CachedIterable(Context.get("products"));
        Iterable<JSONObject> skus = Context.get("skus");
        
        CachedIterable selectedProducts;
        CachedIterable selectedSkus;
        
        String receiptPrefix;
        int[] customers;
        int index = 1;
        int count;
        
        System.out.println("Generating " + days + " days of data...");
        
        try(OutputCursor cursor = getOutput().write(new FileTarget(file)))
        {            
            for(JSONObject franchise : franchises)
            {
                if(franchise.getInt("id") < 0) continue;
                
                selectedProducts = products.getFiltered(new SelectionFilter(franchise, random));
                selectedSkus = new CachedIterable(new InFilter("ProductId", selectedProducts.toStringArray("id")).transform(skus));
                count = index;
                
                System.out.print(franchise.getString("Name") + ": " + NumberFormat.getInstance().format(selectedProducts.size()) + " products / " + NumberFormat.getInstance().format(selectedSkus.size()) + " skus / ");
                
                for(JSONObject store : new EqualsFilter("FranchiseId", franchise.get("id")).transform(stores))
                {
                    customers = generateCustomers(selectedProducts.size() / 32);
                    receiptPrefix = Integer.toString(random.nextInt(1000, 10000));

                    for(JSONObject customer : new Customers(customers, this.days, receiptPrefix))
                    {
                        for(JSONObject sale : new Sale(customer, store, selectedProducts, selectedSkus))
                        {
                            sale.put("id", index++);

                            cursor.write(sale);
                        }
                    }
                }
                
                System.out.print(NumberFormat.getInstance().format(franchise.getInt("Stores")) + " stores / ");
                System.out.println(NumberFormat.getInstance().format(index - count) + " sales");
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
        
        public Sale(JSONObject customer, JSONObject store, CachedIterable products, CachedIterable skus)
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
                quantity = generateQuantity(product, sku, random);
                
                price = product.getDouble("Price");
                total += price * quantity;
                
                record.put("FranchiseId", store.get("FranchiseId"));
                record.put("StoreId", store.get("id"));
                record.put("BrandId", product.get("BrandId"));
                record.put("ProductId", sku.get("ProductId"));
                record.put("SkuId", sku.get("id"));
                record.put("Quantity", quantity);
                record.put("UnitPrice", price);
                record.put("DiscountPrice", price);
                record.put("TotalPrice", price * quantity);
                
                lines.add(record);
            }
        }
        
        private int generateQuantity(JSONObject product, JSONObject sku, Random random)
        {
            double price = product.getDouble("Price");
            double probability = random.nextDouble();
            
            // We try to create a real-world distribution of quantities. Cheaper
            // items will tend to be bought in larger quantities. More expensive
            // items will be bought in smaller, usually individual, quantities.
            if(price < 5.0) return random.nextInt(1, 10);
            else if(price < 30.0 && probability > 0.8) return random.nextInt(2, 5);
            else if(probability > 0.98) return random.nextInt(2, 5);
            else return 1;
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
                            record = new JSONObject(true);

                            record.put("id", null);
                            record.put("FranchiseId", null);
                            record.put("StoreId", null);
                            record.put("BrandId", null);
                            record.put("ProductId", null);
                            record.put("SkuId", null);
                            record.put("DateId", ((date.getYear() + 1900) * 10000) + ((date.getMonth() + 1) * 100) + date.getDate());
                            record.put("TimeId", (hour * 100) + minute);
                            record.put("Receipt", receiptPrefix + (receipt++));
                            record.put("Quantity", null);
                            record.put("UnitPrice", null);
                            record.put("DiscountPrice", null);
                            record.put("TotalPrice", null);

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
