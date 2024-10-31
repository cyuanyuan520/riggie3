package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品的同时新增口味
    public void saveWithFlavor(DishDto dishDto);

    //查询菜品信息的同时查询菜品对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息的同时更新菜品对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
