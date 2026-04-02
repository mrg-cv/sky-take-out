package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.security.SignatureException;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段填充处理逻辑(切面就是切点加通知)
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点：对哪些类的哪些方法进行拦截
     */
    //下面一行是切点表达式：前一句锁定在com.sky.mapper下的所有的类的所有的方法，以及(..)匹配所有的参数类型，后一句还需满足方法上加了@AutoFill注解
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {

    }

    /**
     * 前置通知。在拦截后，在通知中为公共字段赋值
     */
    //前置通知注解。匹配上切点表达式"autoFillPointcut"，才会执行通知方法"autoFill"
    @Before("autoFillPointcut()")
    //通过参数——连接点：JointPoint，就可以知道哪个方法被拦截了，以及被拦截的方法的具体参数是什么
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充");
        //获取当前被拦截的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();  // 方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获得方法上的注解对象
        OperationType operationType = autoFill.value();  //获得数据库操作类型

        //获取到当前被拦截的方法的参数（比如说员工实体、分类实体、套餐实体等实体对象），因为数据库操作类型是对这些实体的属性赋值
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){return;}
        Object entity = args[0]; // //获得参数/实体对象

        //准备为实体对象的公共部分的属性进行赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的数据库操作类型，为对应的属性通过反射赋值
        if(operationType == OperationType.INSERT){
            //为四个公共字段赋值
            try {
                //获得方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if(operationType == OperationType.UPDATE){
            //为两个公共字段赋值
            try {
                //获得方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
