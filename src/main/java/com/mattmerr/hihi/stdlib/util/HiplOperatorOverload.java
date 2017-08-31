package com.mattmerr.hihi.stdlib.util;

import com.mattmerr.hihi.stdlib.HObject;
import com.mattmerr.hitch.parsetokens.expression.Operation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by merrillm on 2/6/17.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HiplOperatorOverload {
    
    Operation.OperationType value();
    Class<? extends HObject>[] otherType() default HObject.class;
    
}
