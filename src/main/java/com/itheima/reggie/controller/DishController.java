package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService flavorService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("菜品添加{}", dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("菜品添加成功...");
    }

    /**
     * 菜品信息的分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //构建分页条件
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //构建实际上要返回的DishDto的分页
        Page<DishDto> dtoInfo = new Page<>();
        //添加条件过滤
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, Dish::getName, name);//添加名字条件
        wrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, wrapper);

        //将pageInfo里的数据(除了records转移到DtoInfo)||使用对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoInfo, "records");
        //将records里面存的集合取出来
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> listDto = records.stream().map(record -> {
            DishDto recordDto = new DishDto();//首先创建一个新的dto
            BeanUtils.copyProperties(record, recordDto);
            Long categoryId = record.getCategoryId();//得到这个record的categoryId
            Category byId = categoryService.getById(categoryId);
            if (byId != null) {//确保不会空指针错误
                String categoryName = byId.getName();//分类的名字取出来了
                recordDto.setCategoryName(categoryName);
            }
            return recordDto;
        }).collect(Collectors.toList());

        //循环完 把records的缺补上
        dtoInfo.setRecords(listDto);
        //最后返回数据
        return R.success(dtoInfo);
    }

    /**
     * 根据id查询菜品全部信息, 包含口味(用来给add/edit页面回显数据)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dto = dishService.getByIdWithFlavor(id);
        return R.success(dto);
    }


    /**
     * 更新菜品信息
     * @param dto
     * @return
     */
    @PutMapping
    public R<String> edit(@RequestBody DishDto dto) {
        dishService.updateWithFlavor(dto);
        return R.success("修改成功!");
    }


    /**
     * 添加套餐页面中, 根据套餐信息查询菜品列表(id和name都可以处理)
     * @param dish
     * @return
     */
    @GetMapping("list")
    public R<List<DishDto>> getByCategoryId(Dish dish) {
        //分两个分支(因为可能有两种情况/有时候可能传入的没有菜品名)
        if (dish.getCategoryId() != null) {//id不为空, 所以是按照分类来查询的
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dish::getCategoryId, dish.getCategoryId());
            wrapper.eq(Dish::getStatus, 1);//确保查询出来的都是起售了的套餐
            List<Dish> list = dishService.list(wrapper);
            List<DishDto> dtoList = new ArrayList<>();
            //将简单条件克隆到dto上
            for (Dish trip1 : list) {
                DishDto dto = new DishDto();
                BeanUtils.copyProperties(trip1, dto);
                dtoList.add(dto);
            }
            dtoList.stream().forEach((item) -> {
                if (item.getCategoryId() != null) {
                    Long dishId = item.getId();//取出dishId
                    LambdaQueryWrapper<DishFlavor> wrapperFlavor = new LambdaQueryWrapper<>();
                    wrapperFlavor.eq(DishFlavor::getDishId, dishId);
                    List<DishFlavor> flavorList = dishFlavorService.list(wrapperFlavor);
                    item.setFlavors(flavorList);
                }
            });
            return R.success(dtoList);
        }
        //现在是第二个分支(根据菜品名字查询)
        LambdaQueryWrapper<Dish> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.like(Dish::getName, dish.getName());
        nameWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> nameList = dishService.list(nameWrapper);
        List<DishDto> dtoNameList = new ArrayList<>();
        //将简单条件克隆到dto上
        for (Dish trip2 : nameList) {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(trip2, dto);
            dtoNameList.add(dto);
        }
        return R.success(dtoNameList);
    }


}
