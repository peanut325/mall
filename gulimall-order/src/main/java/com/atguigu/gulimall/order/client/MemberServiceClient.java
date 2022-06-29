package com.atguigu.gulimall.order.client;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberServiceClient {

    @GetMapping("/member/memberreceiveaddress/getAddress/{memberId}")
    public List<MemberAddressVo> getMemberAddress(@PathVariable("memberId") Long memberId);

}
