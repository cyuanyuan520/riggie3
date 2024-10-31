package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    void saveNewSetmeal(SetmealDto setmealDto);

    Page<SetmealDto> getPage(int page, int pageSize, String name);

    void deleteSet(List<Long> ids);
}
