package com.atguigu.gulimall.gulimallcart.controller;

import com.atguigu.gulimall.gulimallcart.service.CartService;
import com.atguigu.gulimall.gulimallcart.vo.CartItemVo;
import com.atguigu.gulimall.gulimallcart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/getCartItem")
    public List<CartItemVo> getCartItems() {
        return cartService.getCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItemBySkuId(skuId);
        return "redirect:http://cart.gulimalls.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimalls.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("checked") Integer checked) {
        cartService.checkItem(skuId, checked);
        return "redirect:http://cart.gulimalls.com/cart.html";
    }

    /**
     * 浏览器有一个cookie; user-key;标识用户身份，一个月后过期;
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份;
     * 浏览器以后保存，每次访间都会带上这个cookie;
     * <p>
     * 登录: session有
     * 没登录:按照cookie里面带来user-key来做。
     * 第一次:如果没有临时用户,帮忙创建—个临时用户。
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * RedirectAttributes ra
     * ra.addFLashAttribute();将数据放在session里面可以在页面取出，但是只能取一次
     * ra.addAttribute( "shuId" , skuId);将数据放在urL后面
     *
     * @return
     */
    @GetMapping("addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addCart(skuId, num);
        // 重定向域（会自动拼接在路径后面）
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimalls.com/addToCartSuccess.html";
    }

    // 使用重定向保证接口防刷，如果一致发送请求只是查询，而不是新增
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        // 查询skuId的数据
        CartItemVo cartItem = cartService.getCartBySkuId(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }
}
