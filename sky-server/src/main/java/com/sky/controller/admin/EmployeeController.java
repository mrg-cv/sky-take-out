package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {  //login()方法做了4件事：1.接受前端传来的登录信息，登录信息先装进employeeLoginDTO对象（对象里装是：用户名 密码）
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);  //login()方法做了4件事：2.调用业务层去检查employeeLoginDTO对象中装的账号密码。（这个 Controller 不自己判断账号密码对不对，而是交给 employeeService 去处理。Controller 只负责“接请求、交任务、回结果”）

        //login()方法做了4件事：3.登录成功后，JwtUtil这个工具类为前端生成jwt令牌。（token 就像“登录凭证”。登录成功后，后端发给前端一个字符串。前端以后每次访问别的接口，都带上这个字符串。后端就知道：“哦，你已经登录过了。”）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()  //login()方法做了4件事：1.把结果返回给前端。结果装在employeeLoginVO对象中(里面放：员工 id  用户名 姓名 token)
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出")
    public Result<String> logout() {
        return Result.success();
    }
    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        System.out.println("当前线程id：" + Thread.currentThread().getId());
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询，参数为：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }


    /*query 参数也是请求的一部分，路径参数也是请求的一部分，请求体参数也是请求的一部分；它们不是三种不同的“请求”，而是同一个 HTTP 请求里三种不同的放数据位置。
    路径里嵌值的是路径参数，? 后面键值对的是 query 参数，body 里 JSON/表单的是请求体参数。
    对应三个注解：@PathVariable、@RequestParam、@RequestBody。@RequestParam常不加，因为Spring MVC能识别
    例如spring mvc看到这个参数@PathVariable马，那么会从路径中去取这个参数去赋值给方法里的参数变量*/
    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result startOrStop(@PathVariable Integer status,long id){
        log.info("启用禁用员工账号：{},{}", status, id);
        employeeService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getById(@PathVariable long id){
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result updata(@RequestBody EmployeeDTO employeeDTO){
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

}

/*像 GET /admin/employee/page 这样的“请求方式 + 请求路径”共同确定了一条 HTTP 接口。
当前端发起这条请求时，Spring MVC 会根据请求方式、请求路径，以及 Controller 类和方法上的映射注解，匹配到对应的 Controller 方法。
这个 Controller 方法不是接口本身，而是该接口在后端的处理方法。*/
/*例如前端发来请求：GET /admin/employee/page
Spring MVC 会看：第一步：请求方式是不是 GET；第二步：请求路径是不是 /admin/employee/page；第三步：Controller 上的注解
比如类上有：@RequestMapping("/admin/employee")；第四步：方法上的注解。比如方法上有：@GetMapping("/page")
Spring MVC 把这两部分路径拼起来：/admin/employee + /page= /admin/employee/page
然后再结合请求方式 GET，就能匹配到对应方法。*/

