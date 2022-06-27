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
 * Description: 转正流程动作将流程里的人员信息状态更新到财务系统U8中
 * <p>
 * （1）单据编号：{djbh}
 * （2）申请日期：{sqrq}
 * （3）申请人：{sqr}
 * （4）申请部门：{sqbm}
 * （5）所属条线：{sztx}
 * （6）转正员工姓名：{xm}
 * （7）转正员工所属部门：{bm}
 * （8）工号：{personIdExternal}
 * （9）实际转正日期：{customDate1}
 * （10）转正说明：{gzzj}
 *
 * @author : Zao Yao
 * @date : 2022/06/10
 */

public class FullMemberAction implements Action {
    /**
     * 集成日志拦截器
     */
    private static final Logger log = LoggerFactory.getLogger(FullMemberAction.class);
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
        log.info("转正流程动作的流程ID：" + requestId + "转正流程动作的数据表名：" + tableName);
        RecordSet recordSet = new RecordSet();
        String sql = "select * from ? as mainTable where requestId=?";
        recordSet.executeQuery(sql, tableName, requestId);
        if (recordSet.next()) {
            //单据编号
            String receiptNumber = Util.null2String(recordSet.getString("djbh"));
            //申请日期
            String applicationDate = Util.null2String(recordSet.getString("sqrq"));
            //申请人
            String proposer = Util.null2String(recordSet.getString("sqr"));
            //申请部门
            String applyForDepartment = Util.null2String(recordSet.getString("sqbm"));
            //所属条线
            String subordinateToTheLine = Util.null2String(recordSet.getString("sztx"));
            //转正员工姓名
            String regularEmployeeName = Util.null2String(recordSet.getString("xm"));
            //转正员工所属部门
            String regularEmployeeDepartment = Util.null2String(recordSet.getString("bm"));
            //工号
            String workNumber = Util.null2String(recordSet.getString("personIdExternal"));
            //实际转正日期
            String regularEmployeeDate = Util.null2String(recordSet.getString("customDate1"));
            //转正说明：
            String regularEmployeeExplain = Util.null2String(recordSet.getString("gzzj"));

            log.info("查到的表单信息：{单据编号：" + receiptNumber + "申请日期：" + applicationDate + "申请人：" + proposer +
                    "申请部门：" + applyForDepartment + "所属条线：" + subordinateToTheLine + "转正员工姓名：" + regularEmployeeName +
                    "转正员工所属部门：" + regularEmployeeDepartment + "工号：" + workNumber + "实际转正日期：" + regularEmployeeDate +
                    "转正说明：" + regularEmployeeExplain +
                    "}");

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
            //转正日期
            personInfo.put("dregulardate", "2022-06-26");
            //转正前人员类别
            personInfo.put("pk_currpsncl", "0101");
            //转正后人员类别
            personInfo.put("pk_newpsncl", "0101");
            //备注 非必填
            personInfo.put("vsumm", " ");

            arrayPerson.add(personInfo);
            personData.put("person",arrayPerson);
            tableParams.put("data",personData);
            //推送数据
            String responseData = HTTPUtil.doPost(pushDataUrl, tableParams);
            Map<String, String> responseDataOrMap = JSONObject.parseObject(responseData, Map.class);
            String code = responseDataOrMap.get("code");
            if (CODE.equals(code)){
                return SUCCESS;
            }else {
                log.info("试用转正式流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                requestManager.setMessageid("123#123");
                requestManager.setMessagecontent("试用转正式流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                return FAILURE_AND_CONTINUE;
            }
        }
        log.info("试用转正式流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }
}
