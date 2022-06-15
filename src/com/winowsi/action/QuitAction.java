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
 * Description: 离职流程动作将流程里的人员信息状态更新到财务系统U8中
 * 单据编号:{djbh}
 * 申请人:{lzr}
 * 申请日期:{sqsj}
 * 所属部门:{szbm}
 * 所属条线:{sztx}
 * 离职员工姓名:{lzygxm}
 * 工号:{gh}
 * 离职员工部门:{lzygbm}
 * 直接上级:{zjsj}
 * 职位:{zw}
 * 离职申请日期:{lzsqrq}
 * 身份证号码:{sfzhm}
 * 手机号码:{sjhm}
 * 个人邮箱:{gryx}
 * 离职原因:{lzyy}
 *
 * @author : Zao Yao
 * @date : 2022/06/10
 */

public class QuitAction implements Action {
    /**
     * 集成日志拦截器
     */
    private static final Logger log = LoggerFactory.getLogger(QuitAction.class);

    @Override
    public String execute(RequestInfo requestInfo) {
        //TODO 离职流程封装获取token需要的参数
        HashMap<String, String> tokenParams = new HashMap<>(20);
        //收集到的表单数据
        HashMap<String, Object> tableParams = new HashMap<>(16);
        //推送数据的Url
        String postDataUrl = "";
        //获取token的Url
        String tokenUrl = "";
        //1、
        //获取流程ID
        String requestId = requestInfo.getRequestid();
        //获取流程的数据库表名
        String tableName = requestInfo.getRequestManager().getBillTableName();
        log.info("离职流程动作的流程ID：" + requestId + "离职流程动作的数据表名：" + tableName);
        RecordSet recordSet = new RecordSet();
        String sql = "select * from ? as mainTable where requestId=?";
        recordSet.executeQuery(sql, tableName, requestId);
        if (recordSet.next()) {
            //单据编号
            String receiptNumber = Util.null2String(recordSet.getString("djbh"));
            //申请人
            String proposer = Util.null2String(recordSet.getString("lzr"));
            //申请日期
            String applicationDate = Util.null2String(recordSet.getString("sqsj"));
            //所属部门
            String subordinateTodDepartment = Util.null2String(recordSet.getString("szbm"));
            //所属条线
            String subordinateToTheLine = Util.null2String(recordSet.getString("sztx"));
            //离职员工姓名
            String leaveOfficeEmployeeName = Util.null2String(recordSet.getString("lzygxm"));
            //工号
            String workNumber = Util.null2String(recordSet.getString("gh"));
            //离职员工部门
            String leaveOfficeDepartment = Util.null2String(recordSet.getString("lzygbm"));
            //直接上级
            String directSuperior = Util.null2String(recordSet.getString("zjsj"));
            //职位
            String position = Util.null2String(recordSet.getString("zw"));
            //离职申请日期
            String leaveOfficeApplyForDate = Util.null2String(recordSet.getString("lzsqrq"));
            //身份证号码
            String iD = Util.null2String(recordSet.getString("sfzhm"));
            //手机号码
            String phoneNumber = Util.null2String(recordSet.getString("sjhm"));
            //个人邮箱
            String personageEmail = Util.null2String(recordSet.getString("gryx"));
            //离职原因
            String leaveOfficeCause = Util.null2String(recordSet.getString("lzyy"));


            log.info("查到的表单信息：{单据编号：" + receiptNumber + "申请人:" + proposer + "申请日期:" + applicationDate +
                    "所属部门:" + subordinateTodDepartment + "所属条线:" + subordinateToTheLine +
                    "离职员工姓名:" + leaveOfficeEmployeeName + "工号:" + workNumber + "离职员工部门:" + leaveOfficeDepartment +
                    "直接上级:" + directSuperior + "职位:" + position + "离职申请日期:" + leaveOfficeApplyForDate +
                    "身份证号码:" + iD + "手机号码:" + phoneNumber + "个人邮箱:" + personageEmail + "离职原因:" + leaveOfficeCause +
                    "}");

            //TODO 离职流程封装表单数据
            tableParams.put("receiptNumber", receiptNumber);


            //拿到token
            String responseToken = HTTPUtil.doPost(tokenUrl, tokenParams);
            //推送数据
            String responseData = HTTPUtil.doPost(postDataUrl, tableParams);

            return SUCCESS;
        }
        log.info("人员调动流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }
}
