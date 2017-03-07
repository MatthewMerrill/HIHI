package com.mattmerr.hihi.stdlib.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by merrillm on 2/6/17.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HiplMemberFunction {
    
    String value();
    HiplClass[] argumentTypes() default {};
    
}
