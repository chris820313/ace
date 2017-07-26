package org.ace.core.aspect.ncc;

import org.ace.core.annotation.ncc.MapperAware;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.lang.reflect.Method;

/**
 * Created by doraemon on 7/27/2017.
 * MapperAware注解切面
 * 这个切面主要管理数据库连接池
 * 审查被包裹函数是否具有mapper类型的参数，且外部没有传入
 * 如果有，则从连接池申请一条连接，注入mapper实现到参数中
 */
@Component
public class MapperAwareAspect {
    @Autowired(required = false)
    private SqlSessionFactory sqlSessionFactory;

    @Around("@within(org.ace.core.annotation.ncc.MapperAware) || @annotation(org.ace.core.annotation.ncc.MapperAware)")
    @SuppressWarnings("unchecked")
    public Object aroundMapperAwareMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        /*如果就没有声明SqlSessionFactory的bean，那么就不需要去管理，直接退出*/
        if (sqlSessionFactory == null) return proceedingJoinPoint.proceed();
        SqlSession session = null;
        /*获取方法定义*/
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        /*检查是否注解标记了override - 方法上注解的override优先，其次是类上的*/
        Boolean override = (method.isAnnotationPresent(MapperAware.class) ? method.getAnnotation(MapperAware.class) : method.getDeclaringClass().getAnnotation(MapperAware.class)).override();
        /*获取参数和参数类型*/
        Object[] args = proceedingJoinPoint.getArgs();
        Class[] argTypes = method.getParameterTypes();
        /*检查参数中是否有mapper*/
        for (int i = 0; i < args.length; i++) {
            if (Mapper.class.isAssignableFrom(argTypes[i])) {
                Class<Mapper> mapperClass = argTypes[i];
                Mapper mapper = (Mapper) args[i];
                if (mapper == null || override) {
                    session = session == null ? sqlSessionFactory.openSession() : session;
                    args[i] = session.getMapper(mapperClass);
                }
            }
        }
        try {
            return proceedingJoinPoint.proceed(args);
        } finally {
            if (session != null) session.close();
        }
    }
}
