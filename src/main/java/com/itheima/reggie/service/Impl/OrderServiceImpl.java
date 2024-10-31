package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 订单提交
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);//获取当前用户对象
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);//获取当前地址对象
        if (addressBook == null) {
            throw new CustomException("当前用户没有地址!无法提交订单");
        }
        //获取当前用户购物车内容
        LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartService.list(cartWrapper);//当前用户的购物车
        if (cartList == null) {//检查当前购物车
            throw new CustomException("购物车数据为空, 无法提交订单!");
        }
        //先给订单生成id(这样才能给order detail填上id)
        long orderId = IdWorker.getId();                                //订单id: orderId
        AtomicInteger amount = new AtomicInteger(0);            //多线程环境下进行整数操作
        //开始构造orderDetail集合
        List<OrderDetail> orderDetailList = cartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            BeanUtils.copyProperties(item, orderDetail);//直接一步到位 进行数据拷贝
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        log.info("当前订单总价{}", amount.get());

        //给order对象进行设置
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //给订单表插入一条数据
        this.save(orders);
        //给订单明细表插入数据
        orderDetailService.saveBatch(orderDetailList);
        //清除购物车
        shoppingCartService.remove(cartWrapper);
    }

    @Override
    public Page<OrdersDto> getPage(int page, int pageSize) {
        //构造分页查询条件
        Page<Orders> pageINfo = new Page<>(page, pageSize);
        //指定只能查询当前用户
        Long currentId = BaseContext.getCurrentId();
        User byId = userService.getById(currentId);

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, currentId);
        this.page(pageINfo, wrapper);//进行查询
        List<Orders> orderRecords = pageINfo.getRecords();//原来的普通分页记录, 里面没有具体信息
        Page<OrdersDto> dtoPage = new Page<>(page, pageSize);//这是最后要返回的数据
        BeanUtils.copyProperties(orderRecords, dtoPage, "records");
        //自己记录records
        List<OrdersDto> ordersDtos = orderRecords.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item, ordersDto);
            Long orderId = item.getId();//获取订单id
            LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
            orderDetailWrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailWrapper);
            ordersDto.setOrderDetails(orderDetailList);

            return ordersDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(ordersDtos);
        return dtoPage;
    }

    @Override
    public Page<OrdersDto> adminPage(int page, int pageSize) {
        //构造分页查询条件
        Page<Orders> pageINfo = new Page<>(page, pageSize);
        this.page(pageINfo);//进行查询
        List<Orders> orderRecords = pageINfo.getRecords();//原来的普通分页记录, 里面没有具体信息
        Page<OrdersDto> dtoPage = new Page<>(page, pageSize);//这是最后要返回的数据
        BeanUtils.copyProperties(orderRecords, dtoPage, "records");
        //自己记录records
        List<OrdersDto> ordersDtos = orderRecords.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item, ordersDto);
            Long orderId = item.getId();//获取订单id
            LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
            orderDetailWrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailWrapper);
            ordersDto.setOrderDetails(orderDetailList);

            return ordersDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(ordersDtos);
        return dtoPage;
    }

}
