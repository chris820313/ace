package org.ace.core.annotation.ncc;

import java.lang.annotation.*;

/**
 * Created by doraemon on 7/27/2017.
 * MapperAware注解用来注解一个类或方法
 * 带有这个注解的类中方法运行时，aop将检测参数中是否含有Mapper类的参数
 * 如果具备，则这个参数将自动由SqlSessionFactory的Bean来产生示例并注入到函数中，无须由外部传入
 * 如果外部已经传入了这个参数，则aop不自动注入
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
public @interface MapperAware {
    boolean override() default false;
}
