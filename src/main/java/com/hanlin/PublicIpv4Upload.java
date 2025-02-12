package com.hanlin;

import cn.hutool.core.lang.Console;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

/**
 * 公网IPv4上传程序
 * 用于定时检查并更新腾讯云DNS解析记录的公网IP地址
 * 开发者：linluoluo
 */
public class PublicIpv4Upload {
    // 腾讯云账号secretId
    private static final String SECRET_ID = "secretId";
    // 腾讯云账号secretKey
    private static final String SECRET_KEY = "secretKey";
    // 腾讯云解析的域名
    private static final String DOMAIN = "域名";
    // 腾讯云解析的子域名(如果有，请自行修改，在getServerIP()方法下)

    //记录类型
    private static final String RECORD_TYPE = "A";
    // 记录线路
    private static final String RECORD_LINE = "默认";
    //记录ID
    private static final long RECORD_ID = 000001L;//记得修改

    // 缓存上一次获取的公网IP地址
    private static String cachedIP = null;

    public static void main(String[] args) {
        Date data= new Date();

        //动态定时任务
        //每10分钟执行一次
        CronUtil.schedule("0 */10 * * * *", new Task() {

            @Override
            public void execute() {
                //得到本地IP地址
                String publicIP = getPublicIP();
                //获取域名解析的IP地址
                String serverIP=getServerIP();
                //如果不通了  返回false
                if (!publicIP.equals(serverIP)) {
                    System.out.println(data+"IP地址不一致，进行更新");
                    Console.log(ddns());
                } else {
                    Console.log(data+"当前IP一致，无需更新");
                }
            }
        });
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
        // 防止主线程退出
        while (true) {
            try {
                Thread.sleep(1000); // 主线程休眠，避免占用过多CPU
            } catch (InterruptedException e) {
                Console.log("Main thread interrupted: " + e.getMessage());
            }
        }

    }

    /**
     * 替换腾讯云解析的IP地址
     *
     *
     */
    public static String ddns() {

        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential(SECRET_ID, SECRET_KEY);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("dnspod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            DnspodClient client = new DnspodClient(cred, "", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ModifyRecordRequest req = new ModifyRecordRequest();
            req.setDomain(DOMAIN);
            req.setRecordType(RECORD_TYPE);
            req.setRecordLine(RECORD_LINE);
            //获取本机的公网ipv4地址
            String ip=cachedIP;
            req.setValue(ip);
            Console.log("新的ip是："+ip);
            req.setRecordId(RECORD_ID);
            // 返回的resp是一个ModifyRecordResponse的实例，与请求对象对应
            ModifyRecordResponse resp = client.ModifyRecord(req);
            // 输出json格式的字符串回包
//            Console.log(ModifyRecordResponse.toJsonString(resp));
            return ModifyRecordResponse.toJsonString(resp);
        } catch (TencentCloudSDKException e) {
            Console.log(e.toString());
        }
        return null;
    }

    /**
     * 获取腾讯云解析的IP地址
     *
     * @return 腾讯云解析的IP地址
     */
    public static String getServerIP(){
        String ServerIP=null;
        try {
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey
            Credential cred = new Credential(SECRET_ID, SECRET_KEY);

            // 实例化一个 http 选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("dnspod.tencentcloudapi.com");

            // 实例化一个 client 选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            // 实例化要请求产品的 client 对象，clientProfile 是可选的
            DnspodClient client = new DnspodClient(cred, "", clientProfile);

            // 实例化一个请求对象
            DescribeRecordListRequest req = new DescribeRecordListRequest();

            // 设置请求参数，这里需要替换为你要查询的域名
            req.setDomain(DOMAIN);
            // 可根据需要设置子域名，若不设置则查询主域名下所有记录


            // 通过 client 对象调用 DescribeRecordList 接口，得到响应对象
            DescribeRecordListResponse resp = client.DescribeRecordList(req);

            // 获取解析记录列表
            RecordListItem[] recordList = resp.getRecordList();
            if (recordList != null) {
                for (RecordListItem record : recordList) {
                    System.out.println("记录类型: " + record.getType() +
                            ", 主机记录: " + record.getName() +
                            ", 记录值: " + record.getValue() +
                            ", RecordId: " + record.getRecordId());
                    ServerIP=record.getValue();
                }
            }
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
        System.out.println("腾讯云解析IP获取完毕");
        return ServerIP;
    }

    /**
     * 尝试从多个服务获取公网IP地址，并更新缓存
     *
     * @return 公网IP地址，如果所有服务都不可用则返回 null
     */
    public static String getPublicIP() {
        for (String service : IP_SERVICES) {
            try {
                URL url = new URL(service);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String ip = in.readLine();
                in.close();

                // 检查IP地址是否有效
                if (ip != null && !ip.isEmpty()) {
                    // 如果缓存中的IP与当前获取的IP不同，则更新缓存
                    if (!ip.equals(cachedIP)) {
                        System.out.println("缓存IP是: " + cachedIP + ", 查询本地公网IP: " + ip);
                        cachedIP = ip;
                    }
                    return ip;
                }
            } catch (IOException e) {
                // 当前服务不可用，尝试下一个服务
                System.err.println("Service unavailable: " + service);
            }
        }
        return null; // 所有服务都不可用
    }

    /**
     * 存储用于解析本地公网IP的服务网址
     *
     */
    private static final String[] IP_SERVICES = {
            "http://checkip.amazonaws.com",
            "http://icanhazip.com",
            "http://ifconfig.me/ip",
            "http://ipinfo.io/ip"
    };

}
