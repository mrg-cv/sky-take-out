package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
/*Mapper 的职责只有一个：查/改数据库
 @Mapper 是什么意思。它告诉 Spring / MyBatis：这是一个数据库操作接口，不是普通接口。框架会自动帮你生成实现类，你不用自己写 new EmployeeMapperImpl()这种东西。
这个注解的作用是告诉框架：这是一个 MyBatis 的 Mapper 接口，请把它当成一个可管理的组件。
也就是说，Spring + MyBatis 在项目启动时看见 @Mapper 后，会知道：这个接口不是普通接口；它需要被创建成一个可用对象；以后别的类可以 @Autowired 注入它*/
@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")  //@Select(...) 表示：这个方法对应的 SQL 就是这条查询语句。所以：Employee getByUsername(String username)不是空方法
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */
    @Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user, status)" +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
    void insert(Employee employee);
}



/*
MyBatis 做了一件事：它会在项目启动时，自动给这个接口生成一个“代理对象”。这个代理对象你可以先粗浅理解成：“看起来像 EmployeeMapper，
实际上是框架在背后帮你写好的一个实现类”也就是说：虽然你没有手写：class EmployeeMapperImpl implements EmployeeMapper
但是 MyBatis 在运行时，帮你动态生成了一个类似这样的对象。所以 EmployeeMapper 虽然表面是接口，但在运行时，Spring 容器里其实已经有了一个“代理实现对象”。
这就是为什么在EmployeeServiceImp文件中 @Autowired 能注入成功。*/
