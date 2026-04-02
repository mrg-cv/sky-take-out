package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper; // 可以把 @Autowired 理解成：“自动装配 / 自动注入”。意思是：Spring 启动项目时，会先把很多对象创建好，放到一个“大容器”里。等某个类需要用到它们时，就自动把对应对象塞进来。@Autowire private EmployeeMapper employeeMapper; 意思是：Spring，请你把 EmployeeMapper 对应的那个对象给我。

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        // 第一步，拿到对象中的用户名和密码
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、第二步：根据用户名查询数据库中的数据。找数据库里有没有这个人
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传过来的明文密码进行了md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @para employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        System.out.println("当前线程id：" + Thread.currentThread().getId());
        Employee employee = new Employee();

        //employee.setName(employeeDTO.getName());
        //上面一行代码一个个设置太麻烦，直接对象属性拷贝。从源employeeDTO拷贝到employee。前提是EmployeeDTO和Employee属性名一致
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置账号的状态，默认正常状态1表示正常 0表示稳定
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码，默认123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));


        //因为已经用AOP解决了公共字段自动填充，所以这里的公共属性的赋值操作就不需要一个个set了
        //设置当前记录的创建时间和修改时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }


    /*
    startPage(...) 确实会把分页信息放进 ThreadLocal；但不是 Mapper 手动去拿，而是 MyBatis 分页插件在执行查询时去读。
    之所以还要把 pageNum/pageSize 传给 startPage(...)，是因为这些参数本来就要先由你提供，startPage(...) 再负责把它们写进 ThreadLocal，
    ThreadLocal 只是“存放处”，不是“参数来源”。
     */
    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // selct * from employee limit 0,10
        // 开始查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total,records);
    }



    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, long id) {
        // update employee set status = ? where id = ?

        /*构造实体对象employee传统写法如下注释掉的三行
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/

        // 因为在Employee定义处加了@Builder 构建器注解，所以可以用构建器来构造
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    public Employee getById(long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        //之前已经在根据id修改用户的status的功能时，在mapper层写了全面的动态的update的sql语句，但那里的对象是employee不是empoyeeeDYO，所以需要进行对象转换
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        //因为已经用AOP解决了公共字段自动填充，所以这里的公共属性的赋值操作就不需要一个个set了
        //因为是修改操作，所以需要添加一下修改时间
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }
}

/*
Employee employee = employeeMapper.getByUsername(username);
getByUsername(username) 调用后，为什么真的能去数据库执行 SQL，并返回 Employee？？

第 1 步：调用的不是“接口本身”，而是代理对象
虽然变量类型写的是：EmployeeMapper employeeMapper。但它实际指向的，不是“空接口”，而是 MyBatis 在运行时生成的代理对象。也就是：employeeMapper.getByUsername(username)。表面看像调接口，实际上是在调 代理对象的方法。

第 2 步：代理对象知道这个方法对应哪条 SQL
因为在 EmployeeMapper.java 里，这个方法上面写了：
@Select("select * from employee where username = #{username}")。所以 MyBatis 知道：当你调用：getByUsername(username)
就要执行这条 SQL。

第 3 步：把 Java 参数绑定到 SQL 里
方法参数是：String username
SQL 里写的是：where username = #{username}。这里的 `#{username}`` 的意思不是普通字符串拼接，而是：把 Java 方法参数 username 安全地传给 SQL。比如如果你传进去的是："admin"那么执行时效果相当于：select * from employee where username = 'admin'。但底层不是直接粗暴拼字符串，而是预编译参数绑定，这样更安全，也能防止 SQL 注入。
你现在先记住：#{参数名} = 把 Java 参数传给 SQL 占位符

第 4 步：MyBatis 去拿数据库连接
数据库配置在：sky-server/src/main/resources/application.yml。里面有：
spring:
datasource:
druid:
driver-class-name: ${sky.datasource.driver-class-name}
url: jdbc:mysql://${sky.datasource.host}:${sky.datasource.port}/${sky.datasource.database}...
username: ${sky.datasource.username}
password: ${sky.datasource.password}
意思是：这个项目已经配置好了数据源。MyBatis 执行 SQL 时，会通过这个数据源去连接 MySQL。所以 employeeMapper.getByUsername(username) 最终是真的会访问数据库。

第 5 步：数据库返回查询结果
SQL 执行后，会出现两种常见情况：情况 A：查到了 1 行。比如数据库里有这个员工：
id	username	password	name	status
1	admin	123456	管理员	1
那么数据库会返回这一行数据。
情况 B：没查到。如果没有这个用户名，就返回空结果。在 MyBatis 里，这种情况下最终通常就是：employee == null
所以 EmployeeServiceImpl.java 里才会有：
        if (employee == null) {
        throw new AccountNotFoundException(...)
}

第 6 步：MyBatis 把数据库的一行数据封装成 Employee 对象
Mapper 方法返回类型写的是：Employee。而 Employee 这个类在：sky-pojo/src/main/java/com/sky/entity/Employee.java
所以 MyBatis 会把查询结果中的列，自动装进这个对象里。例如数据库返回：
id = 1
username = admin
        password = 123456
name = 管理员
        status = 1
那么最终会得到一个 Java 对象，大致相当于：
Employee employee = new Employee();
employee.setId(1L);
employee.setUsername("admin");
employee.setPassword("123456");
employee.setName("管理员");
employee.setStatus(1);
然后这个对象返回给 Service。

第 7 步：Service 拿到 Employee 后继续做业务判断
于是 EmployeeServiceImpl.java 里的这句：Employee employee = employeeMapper.getByUsername(username);
执行完之后，employee 里要么是：一个从数据库查出来的员工对象。要么是：null
然后后面的 Service 逻辑才开始继续：
        if (employee == null) {
        throw new AccountNotFoundException(...)
}

        if (!password.equals(employee.getPassword())) {
        throw new PasswordErrorException(...)
}

        if (employee.getStatus() == StatusConstant.DISABLE) {
        throw new AccountLockedException(...)
}

注意这个顺序非常重要：Mapper 只负责“查出来”。Service 负责“查出来之后怎么判断”。这才是两层的职责分工。*/
