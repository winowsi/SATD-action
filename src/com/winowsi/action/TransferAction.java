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
 * Description : 调动流程动作将流程里的人员信息状态更新到财务系统U8中
 * <p>
 * 单据编号:{lcbh}
 * 申请人:{sqr}
 * 申请时间:{sqsj}
 * 调动人员姓名:{ddryxm}
 * 调动前所属条线:{ddqsztx}
 * 工号:{gh}
 * 所属部门:{szbm}
 * 直接上级:{zjsj}
 * 虚线上级:{xxsj}
 * 直接下级转交:{zjxjzj}
 * 调动前岗位:{ddqgw}
 * 调动后所属条线:{ddhsztx}
 * 调动后所属部门:{ddhszbm}
 * 调动后岗位:{ddhgw}
 * 调动后直接上级:{ddhzjsj}
 * 调动后虚线上级:{ddhxxsj}
 * 调动后工作地点:{ddhgzdd}
 * 调动类别:{ddlb}：晋升　晋级　调岗　降职　降级　外派　组织架构调整　其它
 * 调动生效时间:{ddsxsj}
 * 调动说明（简述）:{ddsmjs}
 *
 * @author : Zao Yao
 * @date : 2022/06/10
 */

public class TransferAction implements Action {
    /**
     * 集成日志拦截器
     */
    private static final Logger log = LoggerFactory.getLogger(TransferAction.class);

    @Override
    public String execute(RequestInfo requestInfo) {
        //TODO 封装获取token需要的参数
        HashMap<String, String> tokenParams = new HashMap<>(18);
        //收集到的表单数据
        HashMap<String, Object> tableParams = new HashMap<>(14);
        //推送数据的Url
        String postDataUrl = "";
        //获取token的Url
        String tokenUrl = "";
        //1、
        //获取流程ID
        String requestId = requestInfo.getRequestid();
        //获取流程的数据库表名
        String tableName = requestInfo.getRequestManager().getBillTableName();
        log.info("人员调动流程动作的流程ID：" + requestId + "人员调动流程动作的数据表名：" + tableName);
        RecordSet recordSet = new RecordSet();
        String sql = "select * from ? as mainTable where requestId=?";
        recordSet.executeQuery(sql, tableName, requestId);
        if (recordSet.next()) {
            //单据编号
            String receiptNumber = Util.null2String(recordSet.getString("lcbh"));
            //申请人
            String petitioner = Util.null2String(recordSet.getString("sqr"));
            //申请时间
            String appleForTime = Util.null2String(recordSet.getString("sqsj"));
            //调动人员姓名
            String transferPersonnelName = Util.null2String(recordSet.getString("ddryxm"));
            //调动前所属条线
            String transferBeforeStripLine = Util.null2String(recordSet.getString("ddqsztx"));
            //工号
            String workNumber = Util.null2String(recordSet.getString("gh"));
            //所属部门
            String affiliationDepartment = Util.null2String(recordSet.getString("szbm"));
            //直接上级
            String directSuperior = Util.null2String(recordSet.getString("zjsj"));
            //虚线上级
            String dottedLineSuperior = Util.null2String(recordSet.getString("xxsj"));
            //直接下级转交
            String directLowerLevelTo = Util.null2String(recordSet.getString("zjxjzj"));
            //调动前岗位
            String transferBeforePost = Util.null2String(recordSet.getString("ddqgw"));
            //调动后所属条线
            String transferAfterAffiliationStripLine = Util.null2String(recordSet.getString("ddhsztx"));
            //调动后所属部门
            String transferAfterAffiliationDepartment = Util.null2String(recordSet.getString("ddhszbm"));
            //调动后岗位
            String transferAfterPost = Util.null2String(recordSet.getString("ddhgw"));
            //调动后直接上级
            String transferAfterDirectSuperior = Util.null2String(recordSet.getString("ddhzjsj"));
            //调动后虚线上级
            String transferAfterDottedLineSuperior = Util.null2String(recordSet.getString("ddhxxsj"));
            //调动后工作地点
            String transferAfterWorkplace = Util.null2String(recordSet.getString("ddhgzdd"));
            //调动类别
            String transferType = Util.null2String(recordSet.getString("ddlb"));
            //调动生效时间
            String transferEffectiveTime = Util.null2String(recordSet.getString("ddsxsj"));
            //调动说明
            String transferExplain = Util.null2String(recordSet.getString("ddsmjs"));

            log.info("查到的表单信息：{单据编号：" + receiptNumber + "申请人：" + petitioner + "申请时间：" + appleForTime +
                    "调动人员姓名：" + transferPersonnelName + "调动前所属条线：" + transferBeforeStripLine + "工号：" + workNumber +
                    "所属部门：" + affiliationDepartment + "直接上级：" + directSuperior + "虚线上级：" + dottedLineSuperior +
                    "直接下级转交：" + directLowerLevelTo + "调动前岗位：" + transferBeforePost + "调动后所属条线：" + transferAfterAffiliationStripLine +
                    "调动后所属部门：" + transferAfterAffiliationDepartment + "调动后岗位：" + transferAfterPost + "调动后直接上级：" + transferAfterDirectSuperior +
                    "调动后虚线上级：" + transferAfterDottedLineSuperior + "调动后工作地点：" + transferAfterWorkplace + "调动类别：" + transferType +
                    "调动生效时间：" + transferEffectiveTime + "调动说明：" + transferExplain +
                    "}");

            //TODO 封装表单数据
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
