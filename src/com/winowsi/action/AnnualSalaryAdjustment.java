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
 * Description: 员工调薪流程将流程里的人员信息状态更新到财务系统U8中
 * <p>
 * 申请人：{sqr}
 * 申请时间：{jsrq}
 * 调薪员工姓名：{dxygxm}
 * 工号：{gh}
 * 入职时间：{rzsj}
 * 所属部门：{bm}
 * 虚线上级：{xxsj}
 * 调整前薪级序列：{dzqxj}:M　S　T　P
 * 调整前薪级:{xj}:1　2　3　4　5　6　7　8　9
 * 调整前薪档:{dzqxd}:1　2　3　4　5　6　7　8　9
 * 调整前薪酬总额:{dzqxcze}
 * 调薪说明:{dxsm}
 * 调薪生效时间:{ksrq}
 * 调整后薪级序列:{dzhxj}
 * 调整后薪级:{dzhxj1}
 * 调整后薪档:{dzhxd}
 * 调整后薪酬总额:{dzhxcze}
 *
 * @author : Zao Yao
 * @date : 2022/06/15
 */

public class AnnualSalaryAdjustment implements Action {
    /**
     * 集成日志拦截器
     */
    private static final Logger log = LoggerFactory.getLogger(AnnualSalaryAdjustment.class);
    private static final String CODE="200";

    @Override
    public String execute(RequestInfo requestInfo) {
        //流程提示信息
        RequestManager requestManager = requestInfo.getRequestManager();
        //收集到的表单数据
        HashMap<String, Object> tableParams = new HashMap<>(14);
        //推送数据的Url
        String postDataUrl = "";
        //1、
        //获取流程ID
        String requestId = requestInfo.getRequestid();
        //获取流程的数据库表名
        String tableName = requestInfo.getRequestManager().getBillTableName();
        log.info("员工调薪流程动作的流程ID：" + requestId + "员工调薪流程动作的数据表名：" + tableName);
        RecordSet recordSet = new RecordSet();
        String sql = "select * from ? as mainTable where requestId=?";
        recordSet.executeQuery(sql, tableName, requestId);
        if (recordSet.next()) {
            //申请人
            String proposer = Util.null2String(recordSet.getString("sqr"));
            //申请时间
            String timeOfApplication = Util.null2String(recordSet.getString("jsrq"));
            //调薪员工姓名
            String salaryAdjustmentEmployee = Util.null2String(recordSet.getString("dxygxm"));
            //工号
            String workNumber = Util.null2String(recordSet.getString("gh"));
            //入职时间
            String entryTime = Util.null2String(recordSet.getString("rzsj"));
            //所属部门
            String affiliationDepartment = Util.null2String(recordSet.getString("bm"));
            //虚线上级
            String dottedLineSuperior = Util.null2String(recordSet.getString("xxsj"));
            //调整前薪级序列
            String modulationBeforePayList = Util.null2String(recordSet.getString("dzqxj"));
            //调整前薪级
            String modulationBeforePay = Util.null2String(recordSet.getString("xj"));
            //调整前薪档
            String modulationBeforeSalary = Util.null2String(recordSet.getString("dzqxd"));
            //调整前薪酬总额
            String modulationBeforeTotalRemuneration = Util.null2String(recordSet.getString("dzqxcze"));
            //调薪说明
            String toneExplain = Util.null2String(recordSet.getString("dxsm"));
            //调薪生效时间
            String toneEffectiveTime = Util.null2String(recordSet.getString("ksrq"));
            //调整后薪级序列
            String modulationAfterPayList = Util.null2String(recordSet.getString("dzhxj"));
            //调整后薪级
            String modulationAfterPay = Util.null2String(recordSet.getString("dzhxj1"));
            //调整后薪档
            String modulationAfterSalary = Util.null2String(recordSet.getString("dzhxd"));
            //调整后薪酬总额
            String modulationAfterTotalRemuneration = Util.null2String(recordSet.getString("dzhxcze"));

            log.info("查到的表单信息：{申请人：" + proposer + "申请时间：" + timeOfApplication +
                    "调薪员工姓名：" + salaryAdjustmentEmployee + "工号：" + workNumber + "入职时间：" + entryTime +
                    "所属部门：" + affiliationDepartment + "虚线上级：" + dottedLineSuperior +
                    "调整前薪级序列：" + modulationBeforePayList + "调整前薪级：" + modulationBeforePay +
                    "调整前薪档：" + modulationBeforeSalary + "调整前薪酬总额：" + modulationBeforeTotalRemuneration +
                    "调薪说明：" + toneExplain + "调薪生效时间：" + toneEffectiveTime + "调整后薪级序列：" + modulationAfterPayList +
                    "调整后薪级：" + modulationAfterPay + "调整后薪档" + modulationAfterSalary +
                    "调整后薪酬总额：" + modulationAfterTotalRemuneration +
                    "}");

            //封装接口数据结构
            tableParams.put("source", "OA");
            HashMap<String, Object> personData = new HashMap<>(16);
            ArrayList<Object> arrayPerson = new ArrayList<>();
            //封装流程表单数据
            HashMap<String, String> personInfo = new HashMap<>(16);
            //人员编码
            personInfo.put("pk_psndoc", "SA0001");
            //调整金额
            personInfo.put("actpsncount", "1000");
            //薪资项目
            personInfo.put("k_wa_item", "0001");
            //薪资级别
            personInfo.put("pk_wa_grd", "S1");
            //定调级标志
            personInfo.put("fix_adjust_flag ", "Y");
            //备注
            personInfo.put("vsumm", "");

            arrayPerson.add(personInfo);
            personData.put("person",arrayPerson);
            tableParams.put("data",personData);


            //推送数据
            String responseData = HTTPUtil.doPost(postDataUrl, tableParams);
            Map<String, String> responseDataOrMap = JSONObject.parseObject(responseData, Map.class);
            String code = responseDataOrMap.get("code");

            if (CODE.equals(code)){
                return SUCCESS;
            }else {
                log.info("员工调薪流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                requestManager.setMessageid("123#123");
                requestManager.setMessagecontent("员工调薪流程动作请求用友接口返回出错："+responseDataOrMap.get("message")+","+responseDataOrMap.get("code"));
                return FAILURE_AND_CONTINUE;
            }

        }
        log.info("员工调薪流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }
}
