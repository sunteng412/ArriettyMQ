package com.mrfox.arrirtty.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/*****
 * BeanUtils 相同属性注入
 * @author     : MrFox
 * @date       : 2020-09-04 17:33
 * @description:
 * @version    :
 ****/
@Slf4j
public abstract class BeanUtils {

    /**
     * 获取所有属性和值
     *
     * @param object
     * @return
     */
    public static List<Field> getAllFields(Object object) {
        Class clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * @param source 源a
     * @param target 目标
     */
    public static void copyProperties(Object source, Object target) {

        List<Field> allFields = getAllFields(target);

        allFields.forEach(field -> {
            try {
                String simpleName = field.getName();
                PropertyDescriptor targetPd = new PropertyDescriptor(simpleName, target.getClass());
                // 获取其setter方法
                Method writeMethod = targetPd.getWriteMethod();
                if (writeMethod != null) {
                    // 获取source对象与target对象targetPd属性同名的PropertyDescriptor对象sourcePd
                    PropertyDescriptor sourcePd = new PropertyDescriptor(targetPd.getName(), source.getClass());
                    // 获取source对应属性的getter方法
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null &&
                            // 判断是否可赋值，如上述例子：desk是DeskVO、DeskDO，返回false；而chairs都是List<?>，返回true
                            ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        setValue(source, target, targetPd, writeMethod, readMethod);
                    }
                }

            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * @param source  源a
     * @param target  目标
     * @param ignores 忽略
     */
    public static void copyProperties(Object source, Object target, String... ignores) {

        List<Field> allFields = getAllFields(target);

        List<String> propertiesList = Arrays.asList(ignores);

        allFields.stream().filter(
                field -> !propertiesList.contains(field.getName())
        ).forEach(field -> {
            try {
                String simpleName = field.getName();
                PropertyDescriptor targetPd = new PropertyDescriptor(simpleName, target.getClass());
                // 获取其setter方法
                Method writeMethod = targetPd.getWriteMethod();
                if (writeMethod != null) {
                    // 获取source对象与target对象targetPd属性同名的PropertyDescriptor对象sourcePd
                    PropertyDescriptor sourcePd = new PropertyDescriptor(targetPd.getName(), source.getClass());
                    // 获取source对应属性的getter方法
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null &&
                            // 判断是否可赋值，如上述例子：desk是DeskVO、DeskDO，返回false；而chairs都是List<?>，返回true
                            ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        setValue(source, target, targetPd, writeMethod, readMethod);
                    }
                }

            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param source  源a
     * @param target  目标
     * @param ignores 忽略
     */
    public static void copyMapProperties(Map source, Object target, String... ignores) {

        List<Field> allFields = getAllFields(target);

        List<String> propertiesList = Arrays.asList(ignores);

        allFields.stream().filter(
                field -> !propertiesList.contains(field.getName())
        ).forEach(field -> {
            try {
                Object tempObj;
                String simpleName = field.getName();
                //判断是否是基本包装类型
                boolean isBasicProperty = field.getType().getName().startsWith("java.lang");
                if (isBasicProperty) {
                    tempObj = source.get(simpleName);
                }else {
                    tempObj = field.getType().newInstance();
                    copyMapProperties(source, tempObj);
                }
                if(Objects.nonNull(tempObj)){
                    PropertyDescriptor targetPd = new PropertyDescriptor(simpleName, target.getClass());
                    // 获取其setter方法
                    Method writeMethod = targetPd.getWriteMethod();
                    if (writeMethod != null) {
                        // 通过内省获取source对象属性的值
                        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                            writeMethod.setAccessible(true);
                        }
                        // 通过反射给target对象属性赋值(判断是否忽略空值)
                        writeMethod.invoke(target, isBasicProperty ? transformObj(tempObj,field.getType()) : tempObj);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param source  源a
     * @param target  目标
     * @param ignores 忽略
     */
    public static void copyMapPropertiesByMappingMap(Map source, Object target, Map<String,String> mappingMap, String... ignores) {
        Assert.notNull(mappingMap,"mappingMap不能为空");
        List<Field> allFields = getAllFields(target);

        List<String> propertiesList = Arrays.asList(ignores);

        allFields.stream().filter(
                field -> !propertiesList.contains(field.getName())
        ).forEach(field -> {
            try {
                Object tempObj;
                String simpleName = field.getName();
                String sourceFieldName = mappingMap.getOrDefault(simpleName,simpleName);
                //判断是否是基本包装类型
                boolean isBasicProperty = field.getType().getName().startsWith("java.lang");
                if (isBasicProperty) {
                    tempObj = source.get(sourceFieldName);
                }else {
                    tempObj = field.getType().newInstance();
                    copyMapProperties(source, tempObj);
                }

                if(Objects.nonNull(tempObj)){
                    PropertyDescriptor targetPd = new PropertyDescriptor(simpleName, target.getClass());
                    // 获取其setter方法
                    Method writeMethod = targetPd.getWriteMethod();
                    if (writeMethod != null) {
                        // 通过内省获取source对象属性的值
                        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                            writeMethod.setAccessible(true);
                        }
                        // 通过反射给target对象属性赋值(判断是否忽略空值)
                        writeMethod.invoke(target, isBasicProperty ? transformObj(tempObj,field.getType()) : tempObj);
                    }
                }
            } catch (Exception e) {
            log.warn("[BeanUtils#copyMapPropertiesByMappingMap]转换失败",e);
            }
        });
    }



    private static Object transformObj(Object obj, Class<?> type) {
        String simpleName = type.getSimpleName();

        if (simpleName.equals("Integer")) {
            return Integer.parseInt(obj.toString());
        }else if(simpleName.equals("Long")){
            return Long.parseLong(obj.toString());
        }

        return obj;
    }


  /*  public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("precessExecutorRequestCoreSize", "1111");

        ProcessReqConf processReqConf = new ProcessReqConf();
        processReqConf.setProcessReqExecutorCoreSize(1);

        ProcessReqConf processReqConf1 = new ProcessReqConf();

        copyMapProperties(properties, processReqConf1);

        System.out.println(processReqConf1);

    }*/


    private static void setValue(Object source, Object target, PropertyDescriptor targetPd, Method writeMethod, Method readMethod) {
        try {
            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                readMethod.setAccessible(true);
            }
            // 通过内省获取source对象属性的值
            Object value = readMethod.invoke(source);
            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                writeMethod.setAccessible(true);
            }
            // 通过反射给target对象属性赋值(判断是否忽略空值)
            writeMethod.invoke(target, value);

        } catch (Exception ex) {

        }
    }

    /**
     * 获取一个实体bean中某个字段的值
     *
     * @param bean  实体bean
     * @param field 字段名
     * @return
     */
    public static String getProperty(Object bean, String field) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field, bean.getClass());
        Method getMethod = propertyDescriptor.getReadMethod();
        Object value = getMethod.invoke(bean);
        return value.toString();
    }

    /**
     * 设置属性值
     *
     * @param bean
     * @param field
     * @param value
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void setProperty(Object bean, String field, String value) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field, bean.getClass());
        Method setMethod = propertyDescriptor.getWriteMethod();
        setMethod.invoke(bean, value);
    }


    /******
     * 判断是否属于同类
     * 0-不同类型,1-同类型,2-不同类型但是需要转换
     * @param
     * @return
     *****/
    static Integer isAssignable(Class<?> lhsType, Class<?> rhsType, boolean flag) {
        if (flag) {
            //如果是target是Long,source是Date
            if (lhsType.equals(Long.class) && rhsType.equals(Date.class)) {
                return 2;
            } else {
                return ClassUtils.isAssignable(lhsType, rhsType) ? 1 : 0;
            }
        } else {
            return ClassUtils.isAssignable(lhsType, rhsType) ? 1 : 0;
        }
    }


}
