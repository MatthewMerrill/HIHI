package com.mattmerr.hihi.stdlib;

import com.mattmerr.hihi.HNativeFunction;
import com.mattmerr.hihi.HScope;
import com.mattmerr.hihi.stdlib.util.HiplClass;
import com.mattmerr.hihi.stdlib.util.HiplMemberFunction;
import com.mattmerr.hihi.stdlib.util.HiplOperatorOverload;

import java.util.ArrayList;
import java.util.List;

import static com.mattmerr.hitch.parsetokens.expression.Operation.OperationType.ADD;

/**
 * Created by merrillm on 2/5/17.
 */
@HiplClass("List")
public class HList extends HObject {
     
    private List<HObject> values = new ArrayList<>();
    
    public HList(HObject... values) {
        for (HObject value : values)
            this.values.add(value);
    }
    
    @HiplMemberFunction("add")
    @HiplOperatorOverload(value = ADD)
    public HObject add(HScope scope, List<HObject> args) {
        add(args.get(0));
        return HObject.UNDEFINED;
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
        
}
