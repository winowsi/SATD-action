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
 * Description : 入职 offer 审核流程动作流程审批完成后给北森系统推送审批完成的信息
 * <p>
 * 姓名：{xm}
 * 性别：{xb}
 * 个人邮箱:{gryx}
 * offer职务:{zwmc}
 * 联系电话:{lxdh}
 * 身份证号码:{sfzhm}
 * 预计入职时间{yjrzsj}
 * 所属条线:{sztx}
 * 所属部门:{szbm1}
 * 所属城市:{szcs}
 * 试用期:{syq}
 * 直接上级{sfxm}
 * 任职职务:{zw1}
 * 人员来源:{ryly}
 * 人员分类:{ryfl}
 *
 * @author : Zao Yao
 * @date : 2022/06/10
 */

public class InductionAction implements Action {
    /**
     * 集成日志拦截器
     */
    private static final Logger log = LoggerFactory.getLogger(InductionAction.class);
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
        String tableName = requestInfo.getRequestManager().getBillTableName();
        log.info("入职 offer 审核流程动作的流程ID：" + requestId + "入职 offer 审核流程动作的数据表名：" + tableName);
        RecordSet recordSet = new RecordSet();
        String sql = "select * from ? as mainTable where requestId=?";
        recordSet.executeQuery(sql, tableName, requestId);
        if (recordSet.next()) {
            //姓名
            String name = Util.null2String(recordSet.getString("xm"));
            //性别
            String sex = Util.null2String(recordSet.getString("xb"));
            //个人邮箱
            String email = Util.null2String(recordSet.getString("gryx"));
            //offer职务
            String offerDuty = Util.null2String(recordSet.getString("zwmc"));
            //联系电话
            String contactPhoneNumber = Util.null2String(recordSet.getString("lxdh"));
            //身份证号码
            String idCardNumber = Util.null2String(recordSet.getString("sfzhm"));
            //预计入职时间
            String estimatedTimeOfEntry = Util.null2String(recordSet.getString("yjrzsj"));
            //所属条线
            String subordinateToTheLine = Util.null2String(recordSet.getString("sztx"));
            //所属部门
            String subordinateDepartments = Util.null2String(recordSet.getString("szbm1"));
            //所属城市
            String city = Util.null2String(recordSet.getString("szcs"));
            //试用期
            String theProbationPeriod = Util.null2String(recordSet.getString("syq"));
            //直接上级
            String immediateSuperior = Util.null2String(recordSet.getString("sfxm"));
            //任职职务
            String officeJob = Util.null2String(recordSet.getString("zw1"));
            //人员来源
            String personnelSource = Util.null2String(recordSet.getString("ryly"));
            //人员分类
            String personnelClassification = Util.null2String(recordSet.getString("ryfl"));


            log.info("查到的表单信息：{姓名：" + name + "性别：" + sex + "个人邮箱:" + email + "offer职务:" + offerDuty +
                    "联系电话:" + contactPhoneNumber + "身份证号码:" + idCardNumber + "预计入职时间:" + estimatedTimeOfEntry +
                    "所属条线:" + subordinateToTheLine + "所属部门:" + subordinateDepartments + "所属城市:" + city + "试用期:" + theProbationPeriod +
                    "直接上级:" + immediateSuperior + "任职职务:" + officeJob + "人员来源:" + personnelSource + "人员分类:" + personnelClassification +
                    "}");

            //封装接口数据结构
            tableParams.put("source", "OA");
            HashMap<String, Object> personData = new HashMap<>(16);
            ArrayList<Object> arrayPerson = new ArrayList<>();
            //封装流程表单数据
            HashMap<String, String> personInfo = new HashMap<>(16);
            //人员编码
            personInfo.put("pk_psndoc", "SA0001");
            //人员类别编码
            personInfo.put("pk_psnc", "1010");
            //部门编码
            personInfo.put("pk_deptdoc", "10");
            //入职日期
            personInfo.put("Inductiondate", "2022-06-26");
            //备注
            personInfo.put("vsumm", "  ");

            arrayPerson.add(personInfo);
            personData.put("person",arrayPerson);
            tableParams.put("data",personData);

            //推送数据
            String responseData = HTTPUtil.doPost(pushDataUrl, tableParams);
            Map<String, String> responseDataOrMap = JSONObject.parseObject(responseData,Map.class);
            String code = responseDataOrMap.get("code");
            if (CODE.equals(code)){
                return SUCCESS;
            }else {
                log.info("入职 offer 审核流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                requestManager.setMessageid("123#123");
                requestManager.setMessagecontent("入职 offer 审核流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                return FAILURE_AND_CONTINUE;
            }

        }
        log.info("入职 offer 审核流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }
}
