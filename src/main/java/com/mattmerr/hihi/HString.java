package com.mattmerr.hihi;

/**
 * Created by merrillm on 2/5/17.
 */
public class HString extends HList {
    
    public HString(String value) {
        for (char ch : value.toCharArray())
            add(new HCharacter(ch));
        
        attributes.put("+", new HNativeFunction(
                (args, scope) -> new HString(this.nativeValue() + ((HString)args.get(0)).nativeValue())
        ));
    }
    
    @Override
    public HString stringValue(HScope scope) {
        return this;
    }
    
    public String nativeValue() {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < length(); i++) {
            builder.append(((HCharacter) get(i)).nativeValue());
        }
        
        return builder.toString();
    }
}
