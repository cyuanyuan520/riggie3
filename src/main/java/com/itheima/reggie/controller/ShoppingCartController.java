package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 完成添加购物车操作
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("add shoppingCart: {}", shoppingCart);
        //获取用户id
        Long currentId = BaseContext.getCurrentId();//用户id:currentId
        //判断购物车里是否已经存在相同的商品
        LambdaQueryWrapper<ShoppingCart> dishWrapper = new LambdaQueryWrapper<>();//构造查询条件
        //分类 因为有可能加入购物车的是菜品或者套餐
        if (shoppingCart.getDishId() != null) {
            //说明当前加入购物车的是一个单独的菜品
            dishWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //说明当前加入购物车的是一个套餐
            dishWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        dishWrapper.eq(ShoppingCart::getUserId, currentId);
        ShoppingCart cart = shoppingCartService.getOne(dishWrapper);//查询出来的结果cart
        if (cart == null) {
            //这个菜是第一次被加入购物车
            cart = shoppingCart;
            cart.setUserId(currentId);
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(cart);
        } else {
            //这个菜不是第一次被加入购物车
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);
            shoppingCartService.updateById(cart);
        }
        return R.success(cart);
    }


    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车");
        //获取当前用户的id
        Long currentId = BaseContext.getCurrentId();
        //构建查询条件
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, currentId);
        wrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        return R.success(list);
    }

    /**
     * 删除购物车中的食物
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("删除购物车: {}", shoppingCart);
        //先判断购物车中这个东西是不是只有一个 , 如果是的话直接删除数据, 不是的话就数目-1
        Long currentId = BaseContext.getCurrentId();//取得当前用户的id
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();//构建查询条件
        wrapper.eq(ShoppingCart::getUserId, currentId);//查询条件绑定用户id
        if (shoppingCart.getDishId() != null) {
            //用户想删除的是菜品数量
            wrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //用户想删除的是套餐数量
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cart = shoppingCartService.getOne(wrapper);
        if (cart != null) {//防止空指针异常
            Integer number = cart.getNumber();//获取菜品数量
            if (number == 1) {//当前数量为1 直接删除这条记录
                shoppingCartService.removeById(cart);
            } else {
                cart.setNumber(number - 1);//数量不为一 -1
                shoppingCartService.updateById(cart);
            }
        }
        return R.success("购物车删除成功!");
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        log.info("clean");
        Long currentId = BaseContext.getCurrentId();//获取当前用户id
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(wrapper);
        return R.success("购物车清空成功");
    }

}
