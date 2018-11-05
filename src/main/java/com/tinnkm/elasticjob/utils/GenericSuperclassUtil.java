package com.tinnkm.elasticjob.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Auther: tinnkm
 * @Date: 2018/11/2 15:00
 * @Description: 获得泛型T的具体类
 * @since: 1.0
 */
public class GenericSuperclassUtil{

    //得到泛型类T
    public static <T> Class<T> getActualTypeArgument(Class<?> clazz) {
        Class<T> entitiClass = null;
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass)
                    .getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                entitiClass = (Class<T>) actualTypeArguments[0];
            }
        }
        return entitiClass;
    }


}
