package com.jack.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jack.gmall.model.product.BaseAttrInfo;
import com.jack.gmall.product.mapper.BaseAttrInfoMapper;
import com.jack.gmall.product.service.BaseAttrInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/22
 * @Description :
 **/

/**
 * 平台属性实现类
 */
@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {

    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    /**
     * 根据id查询
     *
     * @param id
     */
    @Override
    public BaseAttrInfo listById(Long id) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(id);
        if (baseAttrInfo==null){
            throw new RuntimeException("查询失败");
        }
        return baseAttrInfo;
    }

    /**
     * 查询全部
     */
    @Override
    public List<BaseAttrInfo> list() {
        return baseAttrInfoMapper.selectList(null);
    }

    /**
     * 新增
     *
     * @param baseAttrInfo
     */
    @Override
    public void add(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getAttrName()==null){
            throw  new RuntimeException("参数异常");
        }
        int insert = baseAttrInfoMapper.insert(baseAttrInfo);
        if (insert <= 0){
            throw new RuntimeException("添加失败");
        }
    }

    /**
     * 修改
     *
     * @param baseAttrInfo
     */
    @Override
    public void update(BaseAttrInfo baseAttrInfo) {
        int i = baseAttrInfoMapper.updateById(baseAttrInfo);
        if (i < 0){
            throw new RuntimeException("修改失败");
        }
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        int i = baseAttrInfoMapper.deleteById(id);
        if (i <= 0){
            throw new RuntimeException("删除失败");
        }
    }

    /**
     * 条件查询
     *
     * @param baseAttrInfo
     * @return
     */
    @Override
    public List<BaseAttrInfo> search(BaseAttrInfo baseAttrInfo) {
        LambdaQueryWrapper queryWrapper = buildQueryParam(baseAttrInfo);
        return baseAttrInfoMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询
     *
     * @param current 当前页
     * @param size 每页显示条数
     * @return
     */
    @Override
    public IPage<BaseAttrInfo> page(Integer current, Integer size) {
        IPage<BaseAttrInfo> iPage = baseAttrInfoMapper.selectPage(new Page<BaseAttrInfo>(current, size), null);
        return iPage;
    }

    /**
     * 分页条件查询
     *
     * @param current      当前页
     * @param size         每页显示条数
     * @param baseAttrInfo 查询条件
     * @return
     */
    @Override
    public IPage<BaseAttrInfo> search(Integer current, Integer size, BaseAttrInfo baseAttrInfo) {
        IPage<BaseAttrInfo> iPage = null;
        if (baseAttrInfo==null){
            iPage = page(current, size);
        }
        LambdaQueryWrapper queryWrapper = buildQueryParam(baseAttrInfo);
        iPage = baseAttrInfoMapper.selectPage(new Page<>(current, size), queryWrapper);
        return iPage;
    }

    private LambdaQueryWrapper  buildQueryParam(BaseAttrInfo baseAttrInfo) {
        LambdaQueryWrapper<BaseAttrInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (baseAttrInfo.getCategoryId() != null){
            queryWrapper.eq(BaseAttrInfo::getCategoryId,baseAttrInfo.getCategoryId());
        }
        if (!StringUtils.isEmpty(baseAttrInfo.getAttrName())){
            queryWrapper.like(BaseAttrInfo::getAttrName,baseAttrInfo.getAttrName());
        }
        return queryWrapper;
    }

}
