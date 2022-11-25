package com.jack.gmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jack.gmall.model.product.BaseAttrInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 平台属性
 */
@Mapper
public interface BaseAttrInfoService {

    /**
     * 根据id查询
     * @param id
     * @return
     */
    BaseAttrInfo listById(Long id);

    /**
     * 查询全部
     * @return
     */
    List<BaseAttrInfo> list();

    /**
     * 新增
     * @param baseAttrInfo
     */
    void add(BaseAttrInfo baseAttrInfo);

    /**
     * 修改
     * @param baseAttrInfo
     */
    void update(BaseAttrInfo baseAttrInfo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);

    /**
     * 条件查询
     * @param baseAttrInfo
     * @return
     */
    List<BaseAttrInfo> search(BaseAttrInfo baseAttrInfo);

    /**
     * 分页查询
     * @param current 当前页
     * @param size 每页显示条数
     * @return
     */
    IPage<BaseAttrInfo> page(Integer current,
                                             Integer size);

    /**
     * 分页条件查询
     * @param current 当前页
     * @param size 每页显示条数
     * @param baseAttrInfo 查询条件
     * @return
     */
    IPage<BaseAttrInfo> search(Integer current,
                                            Integer size,
                                            BaseAttrInfo baseAttrInfo);
}
