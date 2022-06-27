package com.winowsi.action;

import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;
import weaver.integration.util.HTTPUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: 实习转试用将流程里的人员信息状态更新到财务系统U8中
 * <p>
 * (1) 申请人：{xm}
 * (2) 申请部门：{bm}
 * (3) 申请日期：{sqrq}
 * <p>
 * 3、完成
 *
 * @author : Zao Yao
 * @date : 2022/06/14
 */

public class InternshipToProbationAction implements Action {
    /**
     * 集成日志拦截器
     */
    private static final Logger log = LoggerFactory.getLogger(InternshipToProbationAction.class);
    private static final String CODE="200";

    @Override
    public String execute(RequestInfo requestInfo) {
        //流程提示信息
        RequestManager requestManager = requestInfo.getRequestManager();
        //收集到的表单数据
        HashMap<String, Object> tableParams = new HashMap<>(16);
        //推送数据的Url
        String pushDataUrl = "";
        //1、
        //获取流程ID
        String requestId = requestInfo.getRequestid();
        //获取流程的数据库表名
        String tableName = requestManager.getBillTableName();
        log.info("实习转试用流程动作的流程ID：" + requestId + "实习转试用流程动作的数据表名：" + tableName);
        RecordSet recordSet = new RecordSet();
        String sql = "select * from ? as mainTable where requestId=?";
        recordSet.executeQuery(sql, tableName, requestId);
        if (recordSet.next()) {
            //申请人姓名
            String name = Util.null2String(recordSet.getString("xm"));
            //申请人部门
            String department = Util.null2String(recordSet.getString("bm"));
            //申请日期
            String applyForDate = Util.null2String(recordSet.getString("sqrq"));
            log.info("查到的表单信息：{申请人姓名：" + name + "申请人部门：" + department + "申请日期：" + applyForDate + "}");

            //封装接口数据结构
            tableParams.put("source", "OA");
            HashMap<String, Object> personData = new HashMap<>(16);
            ArrayList<Object> arrayPerson = new ArrayList<>();
            //封装流程表单数据
            HashMap<String, String> personInfo = new HashMap<>(16);
            //试用人员
            personInfo.put("pk_psndoc", "SA0001");
            //试用开始日期 非必填
            personInfo.put("dbegingdate", "2022-06-25");
            //试用结束日期 非必填
            personInfo.put("denddate", "2022-06-25");
            //备注 非必填
            personInfo.put("vsumm", "   ");

            arrayPerson.add(personInfo);
            personData.put("person",arrayPerson);
            tableParams.put("data",personData);


            //推送数据的结果
            String responseData = HTTPUtil.doPost(pushDataUrl, tableParams);
            Map<String, String> responseDataOrMap = JSONObject.parseObject(responseData,Map.class);
            String code = responseDataOrMap.get("code");
            if (CODE.equals(code)){
                return SUCCESS;
            }else {
                log.info("实习转试用流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                requestManager.setMessageid("123#123");
                requestManager.setMessagecontent("实习转试用流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                return FAILURE_AND_CONTINUE;
            }
        }
        log.info("实习转试用流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }

}
