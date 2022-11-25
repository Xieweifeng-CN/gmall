package com.jack.gmall.product.controller;

import com.jack.gmall.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jack.gmall.model.product.BaseAttrInfo;
import com.jack.gmall.product.service.BaseAttrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/22
 * @Description :
 * 平台属性接口
 **/
@RestController
@RequestMapping("/api/baseAttrInfo")
public class BaseAttrInfoController {

    @Autowired
    private BaseAttrInfoService baseAttrInfoService;

    /**
     * 根据id查询
     */
    @GetMapping("/list/{id}")
    public Result list(@PathVariable(value = "id") Long id){
        BaseAttrInfo baseAttrInfo = baseAttrInfoService.listById(id);
        return Result.ok(baseAttrInfo);
    }

    /**
     * 查询全部
     */
    @GetMapping
    public Result list(){
        List<BaseAttrInfo> list = baseAttrInfoService.list();
        return Result.ok(list);
    }

    /**
     * 新增
     * @param baseAttrInfo
     * @return
     */
    @PostMapping
    public Result add(@RequestBody BaseAttrInfo baseAttrInfo){
        baseAttrInfoService.add(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 修改
     * @param baseAttrInfo
     * @return
     */
    @PutMapping
    public Result update(@RequestBody BaseAttrInfo baseAttrInfo){
        baseAttrInfoService.update(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id){
        baseAttrInfoService.delete(id);
        return Result.ok();
    }

    /**
     * 条件查询
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/search")
    public Result search(@RequestBody BaseAttrInfo baseAttrInfo){
        List<BaseAttrInfo> search = baseAttrInfoService.search(baseAttrInfo);
        return Result.ok(search);
    }

    /**
     * 分页查询
     * @param current
     * @param size
     * @return
     */
    @GetMapping("/page/{current}/{size}")
    public Result page(
                    @PathVariable("current") int current,
                    @PathVariable("size") int size){
        IPage<BaseAttrInfo> page = baseAttrInfoService.page(current, size);
        return Result.ok(page);
    }

    /**
     * 分页条件查询
     * @param current
     * @param size
     * @return
     */
    @GetMapping("/search/{current}/{size}")
    public Result search(
            @PathVariable("current") int current,
            @PathVariable("size") int size,
            @RequestBody BaseAttrInfo baseAttrInfo){
        IPage<BaseAttrInfo> page = baseAttrInfoService.search(current, size,baseAttrInfo);
        return Result.ok(page);
    }
}
