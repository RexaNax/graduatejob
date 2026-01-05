package cn.lxinet.lfs.mapper;

import cn.lxinet.lfs.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 * 
 * 【原理讲解】
 * 继承 BaseMapper<User> 后，MyBatis-Plus 自动提供：
 * - selectById(id): 根据ID查询
 * - selectOne(wrapper): 条件查询单条
 * - insert(entity): 插入
 * - updateById(entity): 更新
 * 无需手写 SQL
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
