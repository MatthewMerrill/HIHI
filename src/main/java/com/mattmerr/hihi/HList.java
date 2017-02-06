package com.mattmerr.hihi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by merrillm on 2/5/17.
 */
public class HList extends HObject {
     
    private List<HObject> values = new ArrayList<>();
    
    public HList(HObject... values) {
        for (HObject value : values)
            this.values.add(value);
    }
    
    public void add(HObject value) {
        values.add(value);
    }
    
    public HObject get(int index) {
        return values.get(index);
    }
    
    public void removeAt(int index) {
        values.remove(index);
    }
    
    public int length() {
        return values.size();
    }
    
    {
        this.attributes.put("+", new HNativeFunction((args, scope) -> {
            values.add(args.get(0));
            return UNDEFINED;
        }));
    }
    
}
