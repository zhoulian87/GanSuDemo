<%@page import="com.powersi.ssm.biz.medicare.common.util.BizHelper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="powersi" uri="http://www.powersi.com.cn/tags"%>
<%
     //TS19052700141   【需求开发】结算云（医院端）出院结算界面和住院信息查询界面需要显示多诊断信息  
    //修改：188行 添加住院多诊断信息查看，注意 ，出院结算云多诊断信息只能查看不能修改，如需修改多诊断信息，还需在住院信息修改处进行多诊断修改  cj  20190529
	String loginUser = BizHelper.getLoginUser().toUpperCase();
	if(loginUser.endsWith("TEST")){
		request.setAttribute("argName", "#{'aac001':'电脑号','akc190':'住院号','aac002':'社会保障号','aaz217':'就医登记号' }");
	}else{
		request.setAttribute("argName", "#{'aac002':'社会保障号'}");
	}
	//【NTS20050800118】电子凭证动态库开发	 钟声 2020/05/05
    request.setAttribute("loginUser",BizHelper.getLoginUser());
    request.setAttribute("loginUserName",BizHelper.getUserName());
	request.setAttribute("akb020",BizHelper.getAkb020());
	//NTS20102600223 电子凭证机具设备管控  陈诗豪 20201027
	request.setAttribute("akb021",BizHelper.getAkb021());
	request.setAttribute("aaa027",BizHelper.getAaa027());
%>
<powersi:html>
<head>
<powersi:head title="出院结算" />
<script type="text/javascript" src="${rootPath}/resource/js/clinicUtils.js"></script>
<style>
.showpay {
	height: 25px !important;
	font-size: 18px !important;
	text-align: right !important;
}
</style>
</head>
<body>
	<%-- 【NTS20050800118】电子凭证动态库开发	NationECCode_ocx动态库注册 钟声 2020/05/05--%>
    <div style="display: none;">
        <object id="NationECCode_ocx"
                codeBase="${rootPath}/resource/cab/NationECCode_ocx.cab#version=1,0,0,1"
                width="0" height="0" border="0"
                classid="clsid:8EEF7302-1FC8-4BA0-8EA5-EC29FDBCA45B"></object>
     <%-- 【NTS20120101270】医保系统新增获取扫码设备SN码校验医保电子凭证设备准入需求 陈诗豪 2020/12/02--%>           
        <object id="idNldata" classid="CLSID:FEB51B1F-4C74-4305-8806-65605D529E22" codebase="NLDatastream_COM.cab#version=1,0,0,1"></object>
		            
    </div>
	<powersi:form id="outChargeMaternity" namespace="/inhospital"
		action="InhospitalManagerAction!outChargeMaternity.action">
		<powersi:panelbox key="查询条件">
			<powersi:panelbox-toolbar>
				<!--  【NTS20050800118】电子凭证动态库开发 唤醒扫码机 by 钟声 20200505 -->
                <div id="operFlag">
                    <td>电子凭证</td>
					<!-- 【NTS20052500228】使用支付宝设备点击二维码获取人员信息时，
                        只需要操作人员选择二维码还是刷脸的形式直接唤醒设备进行扫二维码或刷脸操作。
                        不需要参保人员在支付宝设备上进行选择。 龚喜洋  2020/05/26 -->
					<select id="transType" class="select" onchange="" style="width:100px;">
						<option value="0" selected="" style="width: 50px">微信解码</option>
						<option value="1" style="width: 50px" selected>支付宝解码</option>
						<option value="2" style="width: 50px" >支付宝刷脸</option>
					</select>
                </div>&nbsp;
                <powersi:button id="btnVoucher" label="获取电子凭证" onclick="callScanMachine()" disabled="false" />
                <!--  end  -->	
				<powersi:button id="readele_btn" key="扫码" />
				<powersi:button id="btTryCalcTreat"  label="费用试算" onclick="outChargeMaternityTryCalcTreat()" key="button_calc0"/>
				<powersi:button id="btCalcOutCharge" label="费用结算" onclick="saveOutChargeMaternity()"			key="button_calc1" />
				<powersi:button id="btoutChargeMaternityPrint" label="打印结算单" onclick="outChargeMaternityPrint()"  key="button_print"/>
				<powersi:button id="btFundStatusQuery" label="基金状态"      onclick="fundStatusQuery();" key="button_query"/>	
				<powersi:button id="btoutChargeMaternityReset" label="重置" onclick="outChargeMaternityReset()" key="button_reset" />
				<powersi:reset id="btReset" label="重置" cssStyle="display:none;" />
			</powersi:panelbox-toolbar>
			<powersi:editorlayout cols="10%,10%,15%,8%,12%,8%,12%,10%,15%">
				<powersi:editorlayout-row>
					<powersi:codeselect id="argName" name="inHospitalDTO.argName"
						label="" cssClass="select2"  list="${argName }" />
					<td>
						<powersi:textfield id="querystring" name="inHospitalDTO.querystring" title="请输入信息回车"
							placeholder="请输入信息回车！" readonly="false" onkeyup="checkIdentify()"
							buttonText="读卡" buttonId="readic_button" buttonDisabled="false" />
					</td>		
					<powersi:textfield id="aac002" name="inHospitalDTO.aac002"
						label="社会保障号" readonly="true" />
					<powersi:textfield id="bka008" name="inHospitalDTO.bka008"
						label="单位名称" readonly="true" />
					<powersi:textfield id="aac003" name="inHospitalDTO.aac003"
						label="姓名" readonly="true" />
				</powersi:editorlayout-row>
			</powersi:editorlayout>
		</powersi:panelbox>
		<powersi:panelbox key="住院信息">
			<powersi:editorlayout cols="8">
				<powersi:editorlayout-row>
					<powersi:hidden id="caa027" name="inHospitalDTO.caa027" />
					<powersi:hidden id="aaa027" name="inHospitalDTO.aaa027" />
					<powersi:hidden id="kcd1id" name="inHospitalDTO.kcd1id" />
					<powersi:hidden id="bka001" name="inHospitalDTO.bka001" />
					<powersi:hidden id="bke548" name="inHospitalDTO.bke548" />
					<powersi:hidden id="bke549" name="inHospitalDTO.bke549" />
					<powersi:hidden id="pcardinfo" name="inHospitalDTO.pcardinfo" />
					<powersi:hidden id="ppayinfo" name="inHospitalDTO.ppayinfo" />
					<powersi:hidden id="isInpPsw" value="true"/>
					<powersi:hidden id="outchargeid" value="1"/><!-- 住院结算读卡返回aac002 -->
					<powersi:hidden id="akf001" name="inHospitalDTO.akf001" />
					<powersi:hidden id="aka130" name="inHospitalDTO.aka130" />
					<powersi:hidden id="aae140" name="inHospitalDTO.aae140" />
					<powersi:hidden id="aae240" name="inHospitalDTO.aae240" />
					<powersi:hidden id="akc196" name="inHospitalDTO.akc196" />
					<powersi:hidden id="bka006" name="inHospitalDTO.bka006" />
					<powersi:hidden id="aaz267" name="inHospitalDTO.aaz267" />
					<powersi:hidden id="bke301" name="inHospitalDTO.bke301" />
					<powersi:hidden id="bka100" name="inHospitalDTO.bka100" />
					<!--【NTS20051100306】 【医保】通过电子凭证办理的待遇业务需要有相关记录，确保后续能够统计查询  龚喜洋  2020/06/04 -->
					<powersi:hidden id="aka242" name="inHospitalDTO.aka242" value="0"/>
					<!--【NTS20071500412】 【医保】通过电子凭证办理的结算需要发送消息通知，需要传电子凭证，身份证，姓名给后台  方然青  2020/07/20 -->
					<powersi:hidden id="ecIndexNo" name="inHospitalDTO.ecIndexNo" value=""/>
					<!--【NTS20082000023】 【医保】通过电子凭证办理的结算需要发送消息电子凭证中台回流接口  陈诗豪  2020/08/20 -->
					<powersi:hidden id="idNo" name="inHospitalDTO.idNo" value=""/>
					<powersi:hidden id="idType" name="inHospitalDTO.idType" value=""/>
					<powersi:hidden id="ecToken" name="inHospitalDTO.ecToken" value=""/>
					<!--【NTS20120101270】 【医保】医保系统新增获取扫码设备SN码校验医保电子凭证设备准入需求  陈诗豪  2020/12/02 -->
					<powersi:hidden id="sn_code" name="inHospitalDTO.sn_code" value=""/>
					<powersi:textfield id="aac004_name"
						name="inHospitalDTO.aac004_name" label="性  别" readonly="true" />
					<powersi:textfield id="aac001" name="inHospitalDTO.aac001"
						label="电脑号" readonly="true" />
					<powersi:textfield id="bac001_name"
						name="inHospitalDTO.bac001_name" label="公务员级别" readonly="true" />
					<powersi:textfield id="baa027_name"
						name="inHospitalDTO.baa027_name" label="人员所属中心" readonly="true" />
				</powersi:editorlayout-row>
				<powersi:editorlayout-row>
					<powersi:textfield id="akb021" 
						name="inHospitalDTO.akb021" label="医院名称" readonly="true" />
					<powersi:textfield id="aka130_name"
						name="inHospitalDTO.aka130_name" label="业务类型" readonly="true" />
					<powersi:textfield id="aae140_name"
						name="inHospitalDTO.aae140_name" label="险种类型" readonly="true" />
					<powersi:textfield id="bka006_name"
						name="inHospitalDTO.bka006_name" label="待遇类型" readonly="true" />
				</powersi:editorlayout-row>
				<powersi:editorlayout-row>
					<powersi:textfield id="aac006"
						name="inHospitalDTO.aac006" label="出生日期" readonly="true" />
					<powersi:textfield id="bka035_name" name="inHospitalDTO.bka035_name" 
						label="人员类别" readonly="true" />
					<powersi:textfield id="bka020" name="inHospitalDTO.bka020"
						label="入院科室" readonly="true" />
					<powersi:textfield id="bka022" name="inHospitalDTO.bka022"
						label="入院病区" readonly="true" />
				</powersi:editorlayout-row>
				<powersi:editorlayout-row>
					<powersi:textfield id="ake020" name="inHospitalDTO.ake020"
						label="床位号" readonly="true" />
					<powersi:textfield id="ake022_name"
						name="inHospitalDTO.ake022_name" label="主治医生" readonly="true" />
					<powersi:textfield id="akc190" name="inHospitalDTO.akc190"
						label="住院号" readonly="true" />
					<powersi:textfield id="aaz217" name="inHospitalDTO.aaz217"
						label="就医登记号" readonly="true" />
				</powersi:editorlayout-row>
				<powersi:editorlayout-row>
					<powersi:textfield id="aae030" name="inHospitalDTO.aae030"
						label="入院日期" readonly="true" />
					<powersi:textfield id="bkz101"
						name="inHospitalDTO.bkz101" label="入院诊断" readonly="true" />
					<powersi:textfield id="bkz102"
						name="inHospitalDTO.bkz102" label="出院诊断" title="请输入出院诊断"
						onkeydown="keydown(this)" required="true" readonly="true"
						buttonText="选择" buttonId="bkz102_button" buttonDisabled="false"
						onbuttonclick="chooseAkc196()" />	
					<powersi:codeselect id="ake021" name="inHospitalDTO.ake021"
						label="出院诊断医生" required="true" list="#request.ake022List"
						displayonly="false" cssClass="select2" showValue="false" 
						headerKey="" headerValue="请选择..." />
				</powersi:editorlayout-row>
				<powersi:editorlayout-row>
					<powersi:textfield id="aae031" name="inHospitalDTO.aae031"
						label="出院日期" onchange="onchangeAae031(this)" mask="date"
						required="true" readonly="false" />
					<powersi:codeselect id="bka066" name="inHospitalDTO.bka066"
						label="出院转归情况" codeType="bka066" required="true"
						displayonly="false" cssClass="select2" showValue="false" />
					<powersi:textfield id="bmc030" name="inHospitalDTO.bmc030"
						label="妊娠周期" required="true" />	
					<powersi:codeselect id="amc026" name="inHospitalDTO.amc026"
						codeType="amc026" codeFilter="data_value <> '7' and data_value <> '8'"
						label="生育类别" required="true"/>	
				</powersi:editorlayout-row>
				<powersi:editorlayout-row>
					<powersi:textfield id="amc020" name="inHospitalDTO.amc020"
						label="手术日期" required="true" mask="date"/>	
					<powersi:textfield id="amc031" name="inHospitalDTO.amc031"
						label="胎次" required="false" />	
					<powersi:textfield id="amc028" name="inHospitalDTO.amc028"
						label="胎儿数" required="false"/>	
					<powersi:textfield id="amc022" name="inHospitalDTO.amc022"
						label="出生证号" required="false" placeholder="多个出生证号使用空格隔开"/>
				</powersi:editorlayout-row>
				<powersi:editorlayout-row>
					<powersi:textfield id="amc023" name="inHospitalDTO.amc023"
						label="结婚证号" required="false"/>	
					<powersi:textfield id="amc021" name="inHospitalDTO.amc021"
						label="准生证号" required="false" />	
					<powersi:textfield id="bka245" name="inHospitalDTO.bka245"
						label="预付款" readonly="true" cssStyle="color:red;" />
				</powersi:editorlayout-row>
			</powersi:editorlayout>
		</powersi:panelbox>
	</powersi:form>
	<powersi:panelbox cssStyle="display:none;">
		<powersi:form id="fundform" namespace="/inhospital"
			action="InhospitalManagerAction!queryFundPay.action">
			<powersi:hidden id="kcd1id_hid" name="inHospitalDTO.kcd1id_hid" />
			<powersi:hidden id="aaz217_hid" name="inHospitalDTO.aaz217_hid" />
			<powersi:hidden id="bka891" name="inHospitalDTO.bka891" />
			 <!--  【NTS20050800118】电子凭证动态库开发 支付宝扫码机唯一流水号存储 by 钟声 20200505 -->
        	<powersi:hidden id="outBizNo" /> 
		</powersi:form>
	</powersi:panelbox>
	<powersi:panelbox key="基金支付">
		<powersi:datagrid id="grid" formId="fundform" height="85%"
			delayLoad="true" showReload="false" autoWidth="true"
			enabledSort="false" alternatingRow="true" colDraggable="false"
			isScroll="false" totalRender="renderTotal" usePager="false">
			<powersi:datagrid-column name="aad006" display="支付基金" width="33%" />
			<powersi:datagrid-column name="aae019_upper" display="支付金额大写"
				width="33%" />
			<powersi:datagrid-column name="aae019_lower" display="支付金额小写"
				width="34%" />
		</powersi:datagrid>
	</powersi:panelbox>
	<powersi:panelbox key="其它诊断信息(点击展开可查看)" isCollapse="true">		
			<powersi:datagrid id="gridDiagnoseinfo" delayLoad="true" showReload="false" autoWidth="true" enabledSort="false"
				alternatingRow="true" checkbox="true" colDraggable="false" height="200" pageSize="100" rownumbers="true" > 
           		<powersi:datagrid-column display="编码" name="aka120" width="20%"  />
           		<powersi:datagrid-column display="名称" name="aka121" width="80%" />
           		<powersi:datagrid-column display="类型" name="bke559" width="0%" hide="true"  />
           	 	<powersi:datagrid-column display="序号" name="bke558" width="0%" hide="true"  />
       		</powersi:datagrid>
    			<powersi:hidden id="diagnoseinfo" name="diagnoseinfo" />
	</powersi:panelbox>
	<powersi:panelbox key="结算信息">
		<powersi:editorlayout cols="8%,12%,8%,12%,8%,12%,8%,12%,8%,12%">
			<powersi:editorlayout-row>
				<powersi:textfield id="akc264" name="inHospitalDTO.akc264"
					label="总的费用" cssClass="showpay" value="0.00" disabled="true" />
				<powersi:textfield id="bka832" name="inHospitalDTO.bka832"
					label="基金支付" cssClass="showpay" value="0.00" disabled="true" />
				<powersi:textfield id="bka831" name="inHospitalDTO.bka831"
					label="自付金额" cssClass="showpay" value="0.00" disabled="true" />
				<powersi:textfield id="cash_pay_own"
					name="inHospitalDTO.cash_pay_own" label="自费金额" cssClass="showpay"
					value="0.00" disabled="true" />
				<powersi:textfield id="aka151" name="inHospitalDTO.aka151"
					label="起付金额" cssClass="showpay" value="0.00" disabled="true" />
			</powersi:editorlayout-row>
			<powersi:editorlayout-row>
				<powersi:textfield id="bka842" name="inHospitalDTO.bka842"
					label="医院垫付" cssClass="showpay" value="0.00" disabled="true" />
				<powersi:textfield id="akb066" name="inHospitalDTO.akb066"
					label="个账支付" cssClass="showpay" value="0.00"/>
				<powersi:textfield id="akb067" name="inHospitalDTO.akb067"
					label="现金支付" cssClass="showpay" value="0.00" disabled="true" />
				<powersi:textfield id="todayCollect" label="今收取" cssClass="showpay"
					value="0.00" onkeydown="dealKeyDown(this)" name="todayCollect" />
				<powersi:textfield id="needFind" label="需找回" cssClass="showpay"
					value="0.00" disabled="true" />
			</powersi:editorlayout-row>
		</powersi:editorlayout>
	</powersi:panelbox>
	<powersi:errors />
	<!--  【NTS20050800118】电子凭证动态库开发 电子凭证接口请求js by 钟声 20200505 -->
	<script type="text/javascript" src="${rootPath}/resource/js/medicaretag/electronicVoucher.js"></script>
	<script type="text/javascript">
	     //多诊断序号
        var num_diagnose_sn = 1;
		var objCard = null;
		$(document).ready(function() {
			$("#querystring").focus();
			$("#btoutChargeMaternityPrint").prop("disabled", true);
			$("#btTryCalcTreat").prop("disabled", true);
			$("#btCalcOutCharge").prop("disabled", true);
		});

		function readIcCard() {
			$("#kcd1id").val("");
			findAaz217ByAac002();
		}

		function findAaz217ByAac002(){
			var querystring = powersi.trim($("#querystring").val());
			var argName = $("#argName").val();
			outChargeMaternityReset();
			if (powersi.isnull(querystring)) {
				return;
			}
			$("#querystring").val(querystring);
			$("#argName").val(argName);
			$("#argName").change();
			$("#querystring").prop("disabled", true);
			postJSON(
					"${rootPath}/inhospital/InhospitalManagerAction!findOutChargeAaz217Maternity.action",
						{"inHospitalDTO.querystring" : querystring,
							 "inHospitalDTO.argName" : argName
						}, function(json) {
							$("#querystring").prop("disabled", false);
							if (!checkJSONResultNew(json)) {
								return;
							}
							$.each(json.data, function(key, value) {
								if (!powersi.isnull(value)) {
									$("#" + key).val(value);
								}
							});
							$("#btTryCalcTreat").prop("disabled", false);
							selectAke020();
					});
		}
		
		/*查询就医登记信息*/
		function findOutChargeAaz217MaternityByBka100() {
			var outChargeData = $("#outChargeMaternity").serialize();
			postJSON(
					"${rootPath}/inhospital/InhospitalManagerAction!findOutChargeAaz217Maternity.action",
					outChargeData, function(json) {
						if (!checkJSONResultNew(json)) {
							return;
						}
						$.each(json.data, function(key, value) {
							if (!powersi.isnull(value)) {
								$("#" + key).val(value);
							}
						});
						$("#btTryCalcTreat").prop("disabled", false);
					});
		}

		function renderTotal(allData, currentData) {
			var aae019 = 0;
			if (powersi.rows_length(allData['rows'])) {
				$.each(allData['rows'], function(n, row) {
					if (row['__status'] !== "delete") {
						aae019 += parseFloat(row['aae019']);
					}
				});
			}
			var a = [];
			a.push('<div class="divCenter textSuccess">');
			a.push('<span>');
			a.push('基金支付总额：<b>');
			a.push(aae019.toFixed(2));
			a.push('</b>');
			a.push('</span>');
			a.push('</div>');
			return a.join('');
		}

		function dealKeyDown(para) {
			if (window.event.keyCode == "13") {
				var filed_name = para.id;
				if ("todayCollect" == filed_name) {
					var todayCollect = $("#todayCollect").val();
					var bka831 = $("#bka831").val();
					var bka245 = $("#bka245").val();
					var akb067 = $("#akb067").val();
					if (isNaN(todayCollect)) {
						popupAlert("金额不能为字符");
						return $("#todayCollect").focus();
					}
					if (parseFloat(todayCollect) <= 0) {
						popupAlert("金额不能小于等于0");
						return $("#todayCollect").focus();
					}
					if (powersi.isnull(todayCollect)) {
						todayCollect = 0;
					}
					if (powersi.isnull(bka245)) {
						bka245 = 0.0;
					}
					if (powersi.isnull(akb067)) {
						akb067 = 0.0;
					}
					$("#needFind").val(
							(parseFloat(bka245) + parseFloat(todayCollect) - parseFloat(akb067))
									.toFixed(2));
				}
			}
		}

		/*基金状态*/
		function fundStatusQuery() {
			var aac001 = powersi.trim($("#aac001").val());
			if (powersi.isnull(aac001)) {
				return;
			}
			var aae140 = powersi.trim($("#aae140").val());
			if (powersi.isnull(aae140)) {
				return;
			}
			var caa027 = powersi.trim($("#caa027").val());
			if (powersi.isnull(caa027)) {
				return;
			}
			var aka130 = powersi.trim($("#aka130").val());
			if (powersi.isnull(aka130)) {
				return;
			}
			popupDialog(
					{
						url : "${rootPath}/common/CommonManagerAction!queryPersonFund.action?inHospitalDTO.aac001="
								+ aac001 + "&inHospitalDTO.aae140=" + aae140
								+ "&inHospitalDTO.caa027=" + caa027
								+ "&inHospitalDTO.aka130=" + aka130,
						onClosed : function() {
							var retValue = this.returnValue;
							if (retValue) {

							} else {

							}
						}
					}, 400, 700);
		}
		
		/*费用计算(试算)*/
		function outChargeMaternityTryCalcTreat() {
			$("#aae140").val("510");
			if(!checkForm()){
				return;
			}
			var kcd1id = powersi.trim($("#kcd1id").val());
			if (powersi.isnull(kcd1id)) {
				return;
			}
			var aaz217 = powersi.trim($("#aaz217").val());
			if (powersi.isnull(aaz217)) {
				return;
			}
			var akb066 = powersi.trim($("#akb066").val());
			$("#aae240").val(akb066);
			var bka006 = powersi.trim($("#bka006").val());
			grid.reset();
			feeItemReset();
			$("#btTryCalcTreat").prop("disabled", true);
			var dataOutCharge = $("#outChargeMaternity").serialize();
			postJSON(
					"${rootPath}/inhospital/InhospitalManagerAction!outChargeMaternityTryCalcTreat.action",
					dataOutCharge, 
					function(json) {
						$("#btTryCalcTreat").prop("disabled", false);
						if (!checkJSONResultNew(json)) {
							return;
						}
						if (json.data != "no") {
							$.each(json.data, function(key, value) {
								if (!powersi.isnull(value)) {
									$("#" + key).val(value);
								}
							});
						}
						if (!powersi.isnull(json.message)) {
							popupInfo(json.message);
						}
						queryFundPay("0");
						$("#btCalcOutCharge").prop("disabled", false);
					});
		}

		/*结算收费保存*/
		function saveOutChargeMaternity() {
			$("#aae140").val("510");	
			if(!checkForm()){
				return;
			}
			var kcd1id = powersi.trim($("#kcd1id").val());
			if (powersi.isnull(kcd1id)) {
				return;
			}
			var aaz217 = powersi.trim($("#aaz217").val());
			if (powersi.isnull(aaz217)) {
				return;
			}
			var akb066 = powersi.trim($("#akb066").val());
			$("#aae240").val(akb066);
			if(electronicVoucher !== null){
				if(checkVoucher()){
					return;
				}
				//【NTS20051100306】 【医保】通过电子凭证办理的待遇业务需要有相关记录，确保后续能够统计查询  龚喜洋  2020/06/04
				$("#aka242").val("1"); // 1表示电子凭证办理的业务
			}
			feeItemReset();
			$("#btCalcOutCharge").prop("disabled", true);
			var dataOutCharge = $("#outChargeMaternity").serialize();
			postJSON(
					"${rootPath}/inhospital/InhospitalManagerAction!outChargeMaternity.action",
					dataOutCharge, function(json) {
						$("#btCalcOutCharge").prop("disabled", false);
						if (!checkJSONResultNew(json)) {
							return;
						}
						if (json.data != "no") {
							$.each(json.data, function(key, value) {
								if (!powersi.isnull(value)) {
									$("#" + key).val(value);
								}
							});
						}
						if (!powersi.isnull(json.message)) {
							popupInfo(json.message);
						}
						queryFundPay("1");
						afterSaveOutCharge();
						//【NTS20051200240】湘潭-医保电子凭证支付通知需求  支付通知 by 钟声 2020/5/13
						if(electronicVoucher !== null){
							sendMessage( "settle",true,"${loginUser}","${loginUserName}",
									$("#akc264").val(),$("#bka831").val(),$("#bka832").val());
						}
					});
		}

		function afterSaveOutCharge() {
			$("#querystring").prop("disabled", false);
			$("#btoutChargeMaternityPrint").prop("disabled", false);
			$("#btTryCalcTreat").prop("disabled", true);
			$("#btCalcOutCharge").prop("disabled", true);
		}

		function queryFundPay(bka891) {
			if (powersi.isnull(bka891)) {
				return;
			}
			$("#bka891").val(bka891)
			var kcd1id_hid = powersi.trim($("#kcd1id_hid").val());
			if (powersi.isnull(kcd1id_hid)) {
				return;
			}
			var aaz217_hid = powersi.trim($("#aaz217_hid").val());
			if (powersi.isnull(aaz217_hid)) {
				return;
			}
			$("#fundform").submit();
		}

		/*重置界面*/
		function outChargeMaternityReset() {
			$("#btReset").click();
			feeItemReset();
			$("#aaz217_hid").val('');
			$("#kcd1id_hid").val('');
			$("#querystring").val('');
			$("#argName").val('akc190');
			$("#argName").change();
			$("#bka066").val('');
			$("#bka066").change();
			$("#querystring").prop("disabled", false);
			$("#btoutChargeMaternityPrint").prop("disabled", true);
			$("#btTryCalcTreat").prop("disabled", true);
			$("#btCalcOutCharge").prop("disabled", true);
			$("#ake021").empty();
			$("#ake021").val('');
			$("#ake021").change();
			grid.reset();
			//电子凭证 钟声
			electronicVoucher = null;
			//【NTS20051100306】 【医保】通过电子凭证办理的待遇业务需要有相关记录，确保后续能够统计查询  龚喜洋  2020/06/04
			$("#aka242").val("0");
			$("#ecIndexNo").val("");
			$("#idNo").val("");
			$("#idType").val("");
			$("#ecToken").val("");
		}

		/*支付金额重置*/
		function feeItemReset() {
			$("#akc264").val("0.0");
			$("#bka832").val("0.0");
			$("#bka831").val("0.0");
			$("#cash_pay_own").val("0.0");
			$("#aka151").val("0.0");
			$("#bka842").val("0.0");
			$("#akb066").val("0.0");
			$("#akb067").val("0.0");
			$("#todayCollect").val("0.0");
			$("#needFind").val("0.0");
		}

		/*打印结算单*/
		function outChargeMaternityPrint() {
			var aaz217 = $('#aaz217').val();
			if (powersi.isnull(aaz217)) {
				return;
			}
			popupDialog(
					{
						url : "${rootPath}/common/SettlementReportNewCenterAction!settlementReport.action?inHospitalDTO.aaz217="
								+ aaz217,
						onClosed : function() {

						}
					}, 600, 1000);
		}
		
		function checkIdentify(){
			if (window.event.keyCode == 13) {
				var querystring = powersi.trim($("#querystring").val());
				var argName = $("#argName").val();
				if('aac002'==argName){//如果是身份证获取人员信息，需先校验身份信息
					identify(querystring);
				}else{
					getPerson(querystring);
				}
			}
		}
		
		/*查询就医登记信息*/
		function getPerson(querystring) {
				$("#bke548").val("");
				$("#bke549").val("");
				$("#pcardinfo").val("");
				$("#ppayinfo").val("");
				var argName = $("#argName").val();
				outChargeMaternityReset();
				if (powersi.isnull(querystring)) {
					return;
				}
				$("#querystring").val(querystring);
				$("#argName").val(argName);
				$("#argName").change();
				$("#querystring").prop("disabled", true);
				postJSON(
						"${rootPath}/inhospital/InhospitalManagerAction!findOutChargeAaz217Maternity.action",
							{"inHospitalDTO.querystring" : querystring,
								 "inHospitalDTO.argName" : argName
							}, function(json) {
								$("#querystring").prop("disabled", false);
								if (!checkJSONResultNew(json)) {
									return;
								}
								$.each(json.data, function(key, value) {
									if (!powersi.isnull(value)) {
										$("#" + key).val(value);
									}
								});
								$("#btTryCalcTreat").prop("disabled", false);
								$("#akc196").val("");
								$("#bkz102").val("");
								selectAke020();
								loadDiagnoseInfo();
						});
		}
		
		/*选择出院疾病诊断*/
		function chooseAkc196() {
			popupDialog(
					{
						url : "${rootPath}/common/CommonManagerAction!chooseDisease.action?ka06dto.aka035=6&ka06dto.bke301="+$("#bke301").val(),
						onClosed : function() {
							var retValue = this.returnValue;
							if (retValue) {
								$("#bkz102").val(retValue.aka121);
								$("#akc196").val(retValue.aka120);
								return $("#ake021").focus();
							} else {
								$("#bkz102").val("");
								$("#akc196").val("");
								return $("#bkz102").focus();
							}
						}
					}, 500, 600);
		}
		
		/*加载出院诊断医生*/
		function selectAke020() {
			//入院科室
			var akf001 = $("#akf001").val();
			if (powersi.isnull(akf001)) {
				return;
			}
			postJSON(
					"${rootPath}/inhospital/InhospitalManagerAction!loadAke022List.action",
					{
						"inHospitalDTO.akf001" : akf001
					}, function(json) {
						if (!checkJSONResultNew(json)) {
							return;
						}
						if (json.data != "no") {
							var a = [];
							$.each(json.data, function(key, value) {
								a.push('<option value="');
								a.push(key);
								a.push('"');
								a.push(">");
								a.push(value);
								a.push("</option>");
							});
							$("#ake021").append(a.join(''));
						}
						if (!powersi.isnull(json.message)) {
							popupInfo(json.message);
						}
					});
		}
		
		function onchangeAae031(para) {
			var aae031 = $("#aae031").val();
			if (powersi.isnull(aae031)) {
				return $("#aae031").focus();
			}
			var bka017 = $("#bka017").val();
			var bka890 = $("#bka890").val();
			if (aae031 < bka017) {
				popupAlert("出院日期不能小于入院日期");
				return $("#aae031").focus();
			}
			if (aae031 > bka890) {
				popupAlert("出院日期不能大于当前日期");
				return $("#aae031").focus();
			}
			return $("#bka043").focus();
		}
		
		
		function loadDiagnoseInfo() {
		 	var aaz217=$("#aaz217").val();
			if (powersi.isnull(aaz217)) {
				popupAlert("请先查询出信息!", "提示", "info");
				return;
			}
			postJSON(
					"${rootPath}/inhospital/InhospitalManagerAction!loadDiagnoseInfos.action",
					{
						"aaz217" : aaz217
					}, function(json) {
						if (!checkJSONResultNew(json)) {
							return;
						}
						if (json.data != "no" && json.data != null && json.data != "null") {
							 for(var i=0;i<json.data.length;i++){
								 if(num_diagnose_sn<=parseInt(json.data[i].bke558)){
									  num_diagnose_sn=parseInt(json.data[i].bke558)+1;
								}
							 }
							gridDiagnoseinfo.loadData(json.data);   
						}
						if (!powersi.isnull(json.message)) {
							alert(json.message);
						}
					});
		}
		//【NTS20050800118】电子凭证动态库开发   用于获取扫码机返回的电子凭证,并进行查询 钟声 2020/04/29
	    function callScanMachine(){
	        var transType  = $("#transType").val();
	        if(transType !=='0'){//获取唯一业务流水号  outBizNo
	            var outBizNo = getOutBizNo();
	            $("#outBizNo").val(outBizNo);
	        }
	        var bka021 = "${akb021}";
            var aaa027 = "${aaa027}";
	        var resultMap =  machineScanning(transType,"${loginUser}","${loginUserName}","${akb020}","01301",outBizNo,bka021,aaa027);
	        if(resultMap.code===0){
	            var idNo ;
	            var ecIndexNo;
	            var idType;
				var ecToken;
	            if(transType === '0'){
	                electronicVoucher = resultMap.data;
	                idNo = electronicVoucher.idNo;
	                ecIndexNo = electronicVoucher.ecIndexNo;
	                idType = electronicVoucher.idType;
					ecToken = electronicVoucher.ecToken;
	            }else {
	                electronicVoucher = resultMap.extra;
	                idNo = electronicVoucher.idNo;
	                ecIndexNo = electronicVoucher.ecIndexNo;
	                idType = electronicVoucher.idType;
					ecToken = electronicVoucher.ecToken;
	            }
	            $("#ecIndexNo").val(ecIndexNo);
	            $("#idNo").val(idNo);
				$("#ecToken").val(ecToken);
				$("#idType").val(idType);
	            $("#aka242").val("1");
	            $("#querystring").val(idNo);
	            $("#argName option:first").prop('selected', 'selected');
	            $("#argName option:selected").val('aac002');
	            // /$("#argName").change();
	            getPerson(idNo);
	        }else {
	            //失败
	            electronicVoucher = null;
	            alert(resultMap.message);
	        }
	    }
	</script>
</body>
</powersi:html>