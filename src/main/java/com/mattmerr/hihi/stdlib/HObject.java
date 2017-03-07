package com.mattmerr.hihi.stdlib;

import com.mattmerr.hihi.HNativeFunction;
import com.mattmerr.hihi.HScope;
import com.mattmerr.hihi.stdlib.util.HiplMemberFunction;
import com.mattmerr.hihi.stdlib.util.HiplOperatorOverload;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Created by merrillm on 2/5/17.
 */
public class HObject {
    
    protected Map<String, HObject> attributes = new HashMap<>();
    
    public HObject() {
        addAllAnnotatedMethods();
        addAllOperatorOverloads();
    }
    private void addAllAnnotatedMethods() {
        List<Method> methods = MethodUtils.getMethodsListWithAnnotation(this.getClass(), HiplMemberFunction.class);
        Map<String, Method> precedenceMappings = new HashMap<>();
        
        for (Method method : methods) {
            HiplMemberFunction annote = method.getAnnotation(HiplMemberFunction.class);
            
            if (!HObject.class.isAssignableFrom(method.getReturnType())) {
                System.err.printf("Method %s does not have return type HObject!\n", method.getName());
                continue;
            }
            
            if (!precedenceMappings.containsKey(annote.value())
                    || precedenceMappings.get(annote.value())
                        .getDeclaringClass()
                        .isAssignableFrom(method.getDeclaringClass())) {
                precedenceMappings.put(annote.value(), method);
            }
        }
        
        assignAll(precedenceMappings);
    }
    private void addAllOperatorOverloads() {
        List<Method> methods = MethodUtils.getMethodsListWithAnnotation(this.getClass(), HiplOperatorOverload.class);
        Map<String, Method> precedenceMappings = new HashMap<>();
    
        for (Method method : methods) {
            HiplOperatorOverload annote = method.getAnnotation(HiplOperatorOverload.class);
        
            if (!HObject.class.isAssignableFrom(method.getReturnType())) {
                System.err.printf("Method %s does not have return type HObject!\n", method.getName());
                continue;
            }
            
            if (!precedenceMappings.containsKey(annote.value())
                    || precedenceMappings.get(annote.value())
                        .getDeclaringClass()
                        .isAssignableFrom(method.getDeclaringClass())) {
                precedenceMappings.put(annote.value(), method);
            }
        }
        
        assignAll(precedenceMappings);
    }
    private void assignAll(Map<String, Method> precedenceMappings) {
        for (String key : precedenceMappings.keySet()) {
            attributes.put(key, new HNativeFunction((scope, args) -> {
                try {
                    return (HObject) precedenceMappings.get(key).invoke(this, scope, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }
    
    
    public HObject get(HScope scope, String identifier) {
        if (!attributes.containsKey(identifier))
            return UNDEFINED;
        
        return attributes.get(identifier);
    }
    
    public <T extends HObject> T getExpecting(Class<T> clazz, HScope scope, String identifier) {
        HObject result = attributes.get(identifier);
        
        if (!clazz.isInstance(result))
            throw new RuntimeException("Result " + result + " could not be cast to " + clazz.getName());
        
        return clazz.cast(result);
    }
    
    
//    @HiplMemberFunction("stringify")
    public HString stringValue(HScope scope, List<HObject> args) {
//        if (attributes.containsKey("stringify")) {
//            return (HString) ((HFunction)attributes.get("stringify")).call(scope);
//        }
        
        return new HString("[Object]");
    }
    
    public static final HObject UNDEFINED = new HObject(){
        
        @Override
        public HString stringValue(HScope scope, List<HObject> args) {
            return new HString("UNDEFINED");
        }
        
        @Override
        public String toString() {
            return "UNDEFINED";
        }
        
        @Override
        public HObject get(HScope scope, String identifier) {
            throw new RuntimeException(format("Cannot get %s of undefined.", identifier));
        }
    };
}
