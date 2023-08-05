package com.qihoo.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RefinvokeMethod {

    public static Object invokeStaticMethod(String class_name,String method_name,Class[] parameterTypes,Object[] parameterValues){
        try {
            Class class_obj = Class.forName(class_name);
            Method method = class_obj.getMethod(method_name,parameterTypes);
            return method.invoke(null,parameterValues);
        }catch (Exception e){
            return null;
        }
    }

    public static Object invokeMethod(String class_name,String method_name,Object obj,Class[] parameterTypes,Object[] parameterValues)
    {
        try {
            Class class_obj = Class.forName(class_name);
            Method method = class_obj.getMethod(method_name,parameterTypes);
            return method.invoke(obj,parameterValues);
        }catch (Exception e)
        {
            return null;
        }
    }

    public static Object getField(String class_name,Object obj,String field_name)
    {
        try {
            Class class_obj = Class.forName(class_name);
            Field field = class_obj.getDeclaredField(field_name);
            field.setAccessible(true);
            return field.get(obj);
        }catch(Exception e)
        {
            return null;
        }
    }

    public static Object getStaticField(String class_name,String field_name)
    {
        try {
            Class class_obj = Class.forName(class_name);
            Field field = class_obj.getDeclaredField(field_name);
            field.setAccessible(true);
            return field.get(null);
        }catch (Exception e)
        {
            return null;
        }
    }

    public static void setField(String class_name,String field_name,Object obj,Object value)
    {
        try {
            Class class_obj = Class.forName(class_name);
            Field field = class_obj.getDeclaredField(field_name);
            field.setAccessible(true);
            field.set(obj,value);
        }catch (Exception e)
        {
        }
    }

    public static void setStaticField(String class_name,String field_name,Object value)
    {
        try {
            Class class_obj = Class.forName(class_name);
            Field field = class_obj.getDeclaredField(field_name);
            field.setAccessible(true);
            field.set(null,value);
        }catch (Exception e){
        }
    }
}
