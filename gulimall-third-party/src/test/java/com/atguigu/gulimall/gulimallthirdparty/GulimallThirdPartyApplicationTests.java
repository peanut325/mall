package com.atguigu.gulimall.gulimallthirdparty;

import com.atguigu.gulimall.gulimallthirdparty.service.MsgService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallThirdPartyApplicationTests {

    @Autowired
    private MsgService msgService;

    @Test
    public void sendMessage() {
        boolean b = msgService.sendMessage("18715803510", "1230");
        System.out.println(b);
    }

}
