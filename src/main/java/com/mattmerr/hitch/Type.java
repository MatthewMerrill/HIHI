package com.mattmerr.hitch;

import com.mattmerr.hihi.HFunction;
import com.mattmerr.hihi.stdlib.HObject;
import java.util.HashMap;

public class Type {

  private Type(){}

  private static HashMap<String, Type> knownTypes = new HashMap<>();


  public static Type declareType(String name) {
    return declareType(name, ThingType);
  }
  public static Type declareType(String name, Type parentType) {
    if (knownTypes.containsKey(name))
      throw new IllegalArgumentException("Type already exists with this name.");

    Type type = new Type();
    type.name = name;
    knownTypes.put(name, type);
    return type;
  }
  public static Type getType(String name) {
    return knownTypes.get(name);
  }

  public String name;
  public Type parentType;

  public HashMap<String, HFunction> staticFunctions = new HashMap<>();
  public HashMap<String, HFunction> instanceFunctions = new HashMap<>();

  public HashMap<String, HObject> staticFields = new HashMap<>();
  public HashMap<String, Type> instanceFields = new HashMap<>();

  public static final Type ThingType;
  static {
    ThingType = new Type();
    ThingType.name = "Thing";
    knownTypes.put("Thing", ThingType);
  }

  public static final Type StringType;
  static {
    StringType = declareType("String");
  }

}
