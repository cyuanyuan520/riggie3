package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealDishMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> saveNewSetmeal(@RequestBody SetmealDto setmealDto) {
        setmealService.saveNewSetmeal(setmealDto);
        return R.success("套餐添加成功");
    }

    /**
     * 分页查询套餐列表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<SetmealDto> pageInfo = setmealService.getPage(page, pageSize, name);
        return R.success(pageInfo);
    }

    /**
     * 删除套餐数据
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> deleteSet(@RequestParam List<Long> ids) {
        setmealService.deleteSet(ids);
        return R.success("删除成功");
    }


    /**
     * 客户端显示套餐列表
     * 已经被redis接管
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<SetmealDto>> getSetmealWithDishes(Setmeal setmeal) {
        //先获取需要查询setmeal的分类id
        Long categoryId = setmeal.getCategoryId();
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getCategoryId, categoryId);
        setmealWrapper.eq(Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> list = setmealService.list(setmealWrapper);
        List<SetmealDto> dtoList = list.stream().map((item) -> {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(item, dto);
            Long setmealId = item.getId();//把setmealId找出来了
            LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
            setmealDishWrapper.eq(SetmealDish::getSetmealId, setmealId);
            List<SetmealDish> dishes = setmealDishService.list(setmealDishWrapper);
            dto.setSetmealDishes(dishes);
            return dto;
        }).collect(Collectors.toList());
        log.info("本次请求没有被redis缓存");
        return R.success(dtoList);
    }

    /**
     * 显示套餐详情(一个套餐中有多少种菜 每种菜有多少粪)
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getDishes(@PathVariable Long id) {
        LambdaQueryWrapper<SetmealDish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(dishWrapper);
        List<DishDto> dtoList = list.stream().map((item) -> {
            DishDto dto = new DishDto();
            Long dishId = item.getDishId();//取得菜品id
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dish::getId, dishId);
            Dish one = dishService.getOne(wrapper);
            BeanUtils.copyProperties(one, dto);
            dto.setCopies(item.getCopies());
            return dto;
        }).collect(Collectors.toList());
        return R.success(dtoList);
    }


}
