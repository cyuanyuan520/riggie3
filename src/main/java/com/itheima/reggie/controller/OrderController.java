package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 订单提交方法
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("提交订单: {}", orders);
        orderService.submit(orders);
        return R.success("订单支付成功");
    }


    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize) {
        Page<OrdersDto> dtoPage = orderService.getPage(page, pageSize);
        return R.success(dtoPage);
    }

    @GetMapping("/page")
    public R<Page<OrdersDto>> adminPage(int page, int pageSize) {
        Page<OrdersDto> dtoPage = orderService.adminPage(page, pageSize);
        return R.success(dtoPage);
    }

}
