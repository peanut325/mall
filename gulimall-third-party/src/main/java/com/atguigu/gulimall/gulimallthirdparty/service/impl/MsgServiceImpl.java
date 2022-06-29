package com.atguigu.gulimall.gulimallthirdparty.service.impl;

import com.atguigu.gulimall.gulimallthirdparty.constant.ConstantMsgUtil;
import com.atguigu.gulimall.gulimallthirdparty.service.MsgService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import org.springframework.stereotype.Service;

@Service
public class MsgServiceImpl implements MsgService {
    @Override
    public boolean sendMessage(String phone, String code) {
        try {
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential(ConstantMsgUtil.SECRET_ID, ConstantMsgUtil.SECRET_KEY);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(ConstantMsgUtil.END_POINT);
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(cred, "ap-guangzhou", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            // 国内短信需要加上86
            String[] phoneNumberSet1 = {"86" + phone};
            req.setPhoneNumberSet(phoneNumberSet1);

            req.setSmsSdkAppId(ConstantMsgUtil.APP_ID);
            req.setSignName(ConstantMsgUtil.SIGN_NAME);
            req.setTemplateId(ConstantMsgUtil.TEMPLATE_ID);

            String[] templateParamSet1 = {code};
            req.setTemplateParamSet(templateParamSet1);

            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            // 输出json格式的字符串回包
            System.out.println(SendSmsResponse.toJsonString(resp));

            return true;
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
            return false;
        }
    }
}