package com.winowsi.action;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;
import weaver.integration.util.HTTPUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.HashMap;

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

    @Override
    public String execute(RequestInfo requestInfo) {
        //封装获取token需要的参数
        HashMap<String, String> tokenParams = new HashMap<>(12);
        //收集到的表单数据
        HashMap<String, Object> tableParams = new HashMap<>(16);
        //推送数据的Url
        String pushDataUrl = "";
        //获取token的Url
        String tokenUrl = "";
        //1、
        //获取流程ID
        String requestId = requestInfo.getRequestid();
        //获取流程的数据库表名
        String tableName = requestInfo.getRequestManager().getBillTableName();
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

            //封装表单数据
            tableParams.put("receiptNumber", receiptNumber);
            tableParams.put("applicationDate", applicationDate);
            tableParams.put("proposer", proposer);
            tableParams.put("applyForDepartment", applyForDepartment);
            tableParams.put("subordinateToTheLine", subordinateToTheLine);
            tableParams.put("regularEmployeeName", regularEmployeeName);
            tableParams.put("regularEmployeeDepartment", regularEmployeeDepartment);
            tableParams.put("workNumber", workNumber);
            tableParams.put("regularEmployeeDate", regularEmployeeDate);
            tableParams.put("regularEmployeeExplain", regularEmployeeExplain);

            //拿到token
            String responseToken = HTTPUtil.doPost(tokenUrl, tokenParams);
            //推送数据
            String responseData = HTTPUtil.doPost(pushDataUrl, tableParams);

            return SUCCESS;
        }
        log.info("试用转正式流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }
}
