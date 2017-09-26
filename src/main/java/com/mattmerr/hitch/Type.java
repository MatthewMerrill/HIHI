package com.mattmerr.hitch;

import static java.util.Collections.unmodifiableCollection;

import com.mattmerr.hihi.HFunction;
import com.mattmerr.hihi.stdlib.HObject;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

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
  public static Collection<Type> getTypes() {
    return unmodifiableCollection(knownTypes.values());
  }

  public String name;
  public Type parentType;

  public HashMap<String, ParseNodeFunction> staticFunctions = new HashMap<>();
  public HashMap<String, ParseNodeFunction> instanceFunctions = new HashMap<>();

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
    StringType.instanceFunctions.put("concat", new ParseNodeFunction());
    StringType.instanceFunctions.put("toString", new ParseNodeFunction());
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Type) {
      Type other = (Type) obj;
      return name.equals(other.name);
    }
    return false;
  }

}
