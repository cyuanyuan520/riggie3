package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类
     */
    @Override
    public void remove(Long id) {
        //检查分类有没有关联菜品
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishWrapper);
        if (dishCount >0) {
            //抛出业务异常
            throw new CustomException("当前分类下关联有菜品, 无法安全删除!");
        }
        //检查分类有没有关联套餐
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getCategoryId, id);
        int setCount = setmealService.count(setmealWrapper);
        if (setCount >0) {
            //抛出业务异常
            throw new CustomException("当前分类下关联有套餐, 无法安全删除!");
        }
        //名下既没有菜品也没有套餐, 可以执行删除了
        super.removeById(id);
    }
}
