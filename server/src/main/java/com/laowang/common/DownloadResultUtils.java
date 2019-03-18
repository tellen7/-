package com.laowang.common;

import com.laowang.model.DownloadResult;

public class DownloadResultUtils {

    //封装各种成功情况
    public static DownloadResult success(DownloadResult.StatusCode code, String msg, Object data) {
        DownloadResult result = new DownloadResult();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static DownloadResult success(String msg, Object data){
        return success(DownloadResult.StatusCode.SUCCESS,msg,data);
    }

    public static DownloadResult success(Object data){
        return success(DownloadResult.StatusCode.SUCCESS,"OK",data);
    }

    public static DownloadResult success(String msg){
        return  success(msg,null);
    }

    public static DownloadResult success() {
        return success("成功");
    }
    //封装失败情况

    public static DownloadResult error(String msg){
        DownloadResult result = new DownloadResult();
        result.setCode(DownloadResult.StatusCode.FAIL);
        result.setMsg(msg);
        return result;
    }
    //封装重发请求

    public static DownloadResult again(){
        DownloadResult result = new DownloadResult();
        result.setCode(DownloadResult.StatusCode.AGAIN);
        result.setMsg("请稍后再请求一次");
        return result;
    }
}
