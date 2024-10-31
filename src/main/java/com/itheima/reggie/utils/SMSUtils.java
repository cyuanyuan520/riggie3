package com.itheima.reggie.utils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短信发送工具类
 */
public class SMSUtils {

	private static final Logger log = LoggerFactory.getLogger(SMSUtils.class);

	public static void sendMessage(String addressMail, String authCode){
		try {
			SimpleEmail mail = new SimpleEmail();
			mail.setHostName("smtp.qq.com");//发送邮件的服务器
			mail.setAuthentication("482734085@qq.com","tqahwoesaczycbcg");//刚刚记录的授权码，是开启SMTP的密码
			mail.setFrom("482734085@qq.com","瑞吉外卖 ");  //发送邮件的邮箱和发件人
			mail.setSSLOnConnect(true); //使用安全链接
			mail.addTo(addressMail);//接收的邮箱
			//System.out.println("email"+email);
			mail.setSubject("瑞吉外卖登录验证码");//设置邮件的主题
			mail.setMsg("尊敬的瑞吉外卖用户:你好!\n 登录验证码为:" + authCode+"\n"+"     (有效期为一分钟), 未注册账户将自动创建注册!");//设置邮件的内容
			mail.send();//发送
		} catch (EmailException e) {
			e.printStackTrace();
			log.error("邮件系统出问题了!!快去修理一下");
		}
	}

}

