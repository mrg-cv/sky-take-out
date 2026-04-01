package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//都是 Lombok 提供的注解 编译时自动生成一些常见的样板代码， ，@Data Lombok 会自动为这个类生成这些常用方法：所有字段的 getter、setter、toString()、equals()、hashCode()
//@AllArgsConstructor 的意思是“生成全参构造方法”  @NoArgsConstructor “生成无参构造方法”。是先创建空对象，后面再一个个赋值
public class PageResult implements Serializable {

    private long total; //总记录数

    private List records; //当前页数据集合

}
