package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    /**
     * 订单提交
     * @param orders
     */
    void submit(Orders orders);

    Page<OrdersDto> getPage(int page, int pageSize);

    Page<OrdersDto> adminPage(int page, int pageSize);
}
