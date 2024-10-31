package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //先把菜品简单数据封装到dish表
        this.save(dishDto);//这个操作后dish.id其实就已经回来了
        Long dishId = dishDto.getId();
        //使用Stream流处理dishDto.flavors
        List<DishFlavor> flavors = dishDto.getFlavors();
        //存储各个flavor的dishId
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        //批量保存
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 查询基本菜品信息的同时查询到对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish byId = this.getById(id);
        if (byId == null) {
            throw new CustomException("此菜品不存在");
        }
        DishDto dto = new DishDto();
        BeanUtils.copyProperties(byId, dto);
        //查询菜品的口味信息(用flavor表)
        LambdaQueryWrapper<DishFlavor> wrapperFlavor = new LambdaQueryWrapper<>();
        wrapperFlavor.eq(DishFlavor::getDishId, byId.getId());
        List<DishFlavor> list = dishFlavorService.list(wrapperFlavor);
        dto.setFlavors(list);
        return dto;
    }

    /**
     * 更新菜品信息的同时更新菜品对应的口味信息
     * @param dto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);
        //先给常规变量赋值
        this.updateById(dish);
        //先删除原先的口味
        LambdaQueryWrapper<DishFlavor> wrapperFlavor = new LambdaQueryWrapper<>();
        wrapperFlavor.eq(DishFlavor::getDishId, dish.getId());//设置查询条件
        dishFlavorService.remove(wrapperFlavor);//已经把原来的口味清理掉了
        //重新上传
        List<DishFlavor> flavors = dto.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {//新增功能:设置口味前先检查集合是否为空集 防止空指针异常
            flavors.stream().forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
        }
        dishFlavorService.saveBatch(flavors);
    }
}
