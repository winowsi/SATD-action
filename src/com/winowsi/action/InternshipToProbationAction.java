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
    private static final Logger log = LoggerFactory.getLogger(FullMemberAction.class);

    @Override
    public String execute(RequestInfo requestInfo) {
        //封装获取token需要的参数
        HashMap<String, String> tokenParams = new HashMap<>(16);
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
            //封装表单数据
            tableParams.put("name", name);
            tableParams.put("department", department);
            tableParams.put("applyForDate", applyForDate);
            //2、
            //拿到token的结果
            String responseToken = HTTPUtil.doPost(tokenUrl, tokenParams);
            //3、
            //推送数据的结果
            String responseData = HTTPUtil.doPost(pushDataUrl, tableParams);
            return SUCCESS;
        }
        log.info("实习转试用流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }

}
