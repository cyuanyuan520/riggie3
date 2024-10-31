package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealDishMapper;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void saveNewSetmeal(SetmealDto setmealDto) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto, setmeal);
        //先保存基础数据(套餐基本信息)
        this.save(setmeal);
        Long setmealId = setmeal.getId();//得到了套餐的id: setmealId
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //给每一个菜都附上套餐id
        setmealDishes.forEach(item -> item.setSetmealId(setmealId));
        //套餐里面的具体菜品也传上去了, 万事大吉
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public Page<SetmealDto> getPage(int page, int pageSize, String name) {
        //查询简单部分, 直接构造查询条件
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);//分页查询条件
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();//可能会存在的名字
        wrapper.like(name != null, Setmeal::getName, name);//模糊查询
        this.page(pageInfo, wrapper);
        List<SetmealDto> dtoList = pageInfo.getRecords().stream().map((item) -> {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(item, dto);
            dto.setCategoryName(categoryService.getById(item.getCategoryId()).getName());//给每个套餐设置categoryName
            return dto;
        }).collect(Collectors.toList());
        Page<SetmealDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    @Transactional
    public void deleteSet(List<Long> ids) {
        //检测当前传入的ids集合中是否存在仍然在售的套餐
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Setmeal::getId, ids);
        wrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(wrapper);
        if (count > 0) {
            throw new CustomException("请先下架套餐后再删除!");
        }
        //确认当前没有在售的套餐后: 先删除套餐中的菜品
        LambdaQueryWrapper<SetmealDish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishWrapper);//菜品删除完毕
        //可以开始删除套餐了
        LambdaQueryWrapper<Setmeal> setWrapper = new LambdaQueryWrapper<>();
        setWrapper.in(Setmeal::getId, ids);
        this.remove(setWrapper);//套餐已被删除完毕
    }
}
