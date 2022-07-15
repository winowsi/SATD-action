package com.winowsi.action;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.io.IOException;
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
    private static final String CODE = "200";
    private static final String SEX = "0";

    @Override
    public String execute(RequestInfo requestInfo) {
        //流程提示信息
        RequestManager requestManager = requestInfo.getRequestManager();
        //收集到的表单数据
        HashMap<String, Object> tableParams = new HashMap<>(16);
        //推送数据的Url
        String pushDataUrl = "http://u8t.satdkj.com:10808/uapws/personInduction";

        //1、
        //获取流程ID
        String requestId = requestInfo.getRequestid();
        //获取流程的数据库表名
        String tableName = requestInfo.getRequestManager().getBillTableName();
        log.info("入职 offer 审核流程动作的流程ID：" + requestId + "入职 offer 审核流程动作的数据表名：" + tableName);
        RecordSet recordSet = new RecordSet();
        String sql = "select * from "+tableName+" where requestId="+requestId;
        recordSet.execute(sql);
        log.info("入职 offer 审核流程查询SQL"+sql);
        if (recordSet.next()) {
            log.info("入职 offer 审核流程查询成功");
            //是否推送到预入职
            String bol = Util.null2String(recordSet.getString("sftsdyrz"));
            if (!SEX.equals(bol)) {
                log.info("入职 offer 审核流程HRBP选择不推送到预入职");
                return SUCCESS;
            }
            //公司编码
            String gs = Util.null2String(recordSet.getString("sqgs1"));
            String subCompanyCodeById = getSubCompanyCodeById(gs);
            //姓名
            String name = Util.null2String(recordSet.getString("xm"));
            //性别
            String sex = Util.null2String(recordSet.getString("xb")).equals(SEX) ? "男" : "女";
            //身份证号码
            String idCardNumber = Util.null2String(recordSet.getString("sfzhm"));
            //所属部门编码
            String subordinateDepartments = Util.null2String(recordSet.getString("szbm1"));
            String departmentCode = getDepartmentCodeById(subordinateDepartments);
            //预计入职日期
            String EstimatedDateOfEntry = Util.null2String(recordSet.getString("yjrzsj"));
            //HRBP 人员编码
            String HRBP = Util.null2String(recordSet.getString("hrbp"));
            String HRBPPersonCode = getPersonCodeById(HRBP);
            //试用期
            String theProbationPeriod = Util.null2String(recordSet.getString("syq"));
            //直接上级
            String immediateSuperior = Util.null2String(recordSet.getString("sfxm"));
            String personCode = getPersonCodeById(immediateSuperior);
            //岗位编码
            String workCode = Util.null2String(recordSet.getString("gwbm"));
            //一级城市
            String city1 = Util.null2String(recordSet.getString("yjcs"));
            String cityName1 = getCityNameById(city1);
            //二级城市
            String city2 = Util.null2String(recordSet.getString("szcs"));
            String cityName2 = getCityNameById(city2);
            //人员类别
            String personType = Util.null2String(recordSet.getString("ryfl"));
            String personTypeName = getSelectPersonTypeById(personType);

            log.info("查到的表单信息：{姓名：" + name + ",性别：" + sex + ",身份证号码:" + idCardNumber +
                    ",所属分部：" + subCompanyCodeById + ",所属部门编码:" + departmentCode + ",一级城市:" + cityName1 +
                    ",所属城市:" + cityName2 + ",试用期:" + theProbationPeriod + ",直接上级编码:" + personCode +
                    ",岗位编码:" + workCode + ",HRBP编码:" + HRBPPersonCode + ",人员类别:" + personTypeName + ",预计入职日期:" + EstimatedDateOfEntry +
                    "}");

            //封装接口数据结构
            tableParams.put("source", "OA");
            tableParams.put("method", "personInduction");
            HashMap<String, Object> personData = new HashMap<>(16);
            //封装流程表单数据
            HashMap<String, String> personInfo = new HashMap<>(16);
            //公司编码
            personInfo.put("pk_corp", subCompanyCodeById);
            //姓名
            personInfo.put("psnname", name);
            //性别
            personInfo.put("sex", sex);
            //身份证号
            personInfo.put("id", idCardNumber);
            //人员类别
            personInfo.put("pk_psncl", personTypeName);
            //部门编码
            personInfo.put("pk_deptdoc", departmentCode);
            //岗位编码
            personInfo.put("jobcode", workCode);
            //一级城市
            personInfo.put("custcitycode", cityName1);
            //二级城市
            personInfo.put("secondcitycode", cityName2);
            //直接上级
            personInfo.put("managercode", personCode);
            //HRBP
            personInfo.put("hrbp", HRBPPersonCode);
            //试用期 非必填
            personInfo.put("trialdate", theProbationPeriod);
            //入职日期
            personInfo.put("inductiondate", EstimatedDateOfEntry);
            personData.put("person", personInfo);
            tableParams.put("data", personData);
            //推送数据
            String params = JSONObject.toJSONString(tableParams);
            log.info("封装的Json信息:"+params);
            String responseData = postDoJson(pushDataUrl, params);

            Map<String, Object> responseDataOrMap = JSONObject.parseObject(responseData, Map.class);
            log.info("返回的Json信息:"+responseDataOrMap.toString());
            String code = responseDataOrMap.get("code").toString();
            if (CODE.equals(code)) {
                return SUCCESS;
            } else {
                log.info("入职 offer 审核流程动作请求用友接口返回出错：" + responseDataOrMap.get("message") + "," + responseDataOrMap.get("code"));
                requestManager.setMessageid("123#123");
                requestManager.setMessagecontent("入职 offer 审核流程动作请求用友接口返回出错：" + responseDataOrMap.get("message") + "," + responseDataOrMap.get("code"));
                return FAILURE_AND_CONTINUE;
            }

        }
        log.info("入职 offer 审核流程动作的recordSet.next()中的代码没有正常执行");
        return FAILURE_AND_CONTINUE;
    }

    /**
     * @param id 分部id
     * @return 分部编码
     */
    public static String getSubCompanyCodeById(String id) {
        RecordSet recordSet = new RecordSet();
        String subCompanyCodeSql = "select subcompanycode from hrmsubcompany where id=" + id;
        recordSet.execute(subCompanyCodeSql);
        if (recordSet.next()) {
            String subCompanyCode = Util.null2String(recordSet.getString("subcompanycode"));
            log.info("入职 offer 审核流程级 分部编码 查询成功" + subCompanyCode);
            return subCompanyCode;
        }
        log.info("根据 分部 ID没有查到对的部门编码");
        return "";
    }

    /**
     * @param id 部门id
     * @return 部门编码
     */
    public static String getDepartmentCodeById(String id) {
        RecordSet recordSet = new RecordSet();
        String departmentSql = "select departmentcode from hrmdepartment where id=" + id;
        recordSet.execute(departmentSql);
        if (recordSet.next()) {
            String departmentCode = Util.null2String(recordSet.getString("departmentcode"));
            log.info("入职 offer 审核流程级 部门编码 查询成功" + departmentCode);
            return departmentCode;
        }
        log.info("根据 部门 ID没有查到对的部门编码");
        return "";
    }

    /**
     * @param id 人员id
     * @return 人员编码
     */
    public static String getPersonCodeById(String id) {
        RecordSet recordSet = new RecordSet();
        String personSql = "select WORKCODE from hrmresource where ID=" + id;
        recordSet.execute(personSql);
        if (recordSet.next()) {
            String personCode = Util.null2String(recordSet.getString("WORKCODE"));
            log.info("入职 offer 审核流程级 人员编码 查询成功" + personCode);
            return personCode;
        }
        log.info("根据人员 ID没有查到对的人员编码");
        return "";
    }

    /**
     * @param city 城市ID
     * @return 城市名
     */
    public static String getCityNameById(String city) {
        RecordSet recordSet = new RecordSet();
        String citySql = "select CITYNAME from hrmcity where id =" + city;
        recordSet.execute(citySql);
        if (recordSet.next()) {
            String cityName = Util.null2String(recordSet.getString("CITYNAME"));
            log.info("入职 offer 审核流程级城市查询成功" + cityName);
            return cityName;
        }
        log.info("根据城市ID没有查到对应城市");
        return "";
    }

    /**
     * @param Id 人员类别id
     * @return 人员类别
     */
    public static String getSelectPersonTypeById(String Id) {
        switch (Id) {
            case "1":
                return "正式员工";
            case "2":
                return "实习员工";
            case "3":
                return "外部员工—残疾人";
            case "4":
                return "外部员工—劳务";
            case "5":
                return "外部员工—兼职";
            default:
                return "";
        }
    }

    /**
     * @param url    推送地址
     * @param params Json参数
     * @return json结果
     */
    public static String postDoJson(String url, String params) {
        String rep = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(params, charSet);
        httpPost.setEntity(entity);
        HttpResponse response;
        try {
            response = httpClient.execute(httpPost);
            rep = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();

        }
        return rep;
    }
}
