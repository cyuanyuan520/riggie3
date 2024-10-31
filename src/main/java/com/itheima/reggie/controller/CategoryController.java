package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类(菜品分类 套餐分类)
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("新增分类:{}", category.toString());
        categoryService.save(category);
        return R.success("添加成功");
    }

    /**
     * 分类管理页面分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        log.info("套餐列表分页查询 page={}, pageSize={}", page, pageSize);
        Page<Category> categoryPage = new Page<>(page, pageSize);//确定page页数和大小
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getSort);//添加排序 根据sort逆序排列
        //执行查询
        categoryService.page(categoryPage, wrapper);
        //返回响应
        return R.success(categoryPage);
    }


    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除分类: {}", id);
        categoryService.remove(id);
        return R.success("分类删除成功!");
    }

    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("更新分类{}", category.toString());
        categoryService.updateById(category);
        return R.success("分类更新成功");
    }

    /**
     * 根据type分类查询所有分类
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        log.info("有人在请求查询分类列表");
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        //添加条件
        wrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加降序条件
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //正式开始查询
        List<Category> list = categoryService.list(wrapper);
        return R.success(list);
    }

}
