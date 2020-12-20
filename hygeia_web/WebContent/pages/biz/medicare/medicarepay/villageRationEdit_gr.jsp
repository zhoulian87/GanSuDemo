<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="powersi" uri="http://www.powersi.com.cn/tags"%>
<%@ taglib prefix="biz" uri="http://www.powersi.com.cn/biztags"%>
<%@ taglib prefix="mediTag" uri="http://www.powersi.com.cn/medicaretaglib"%>
<%
	String path = request.getContextPath(); 
	String village_no = request.getAttribute("village_no")==null?"":request.getAttribute("village_no").toString();
%>
<powersi:html>
<head>
<powersi:head title="村卫生室参保人定额维护" />
</head>
<body>
	<powersi:form name="mainForm" action="MedicalVillagePayAction!getRationData.action" namespace="/medicarepay">
		<powersi:hidden id="kct1id" name="rationDto.kct1id" />
		<powersi:hidden id="ration_type" name="rationDto.ration_type" value="2"/>
		<powersi:groupbox title="定额信息">
			<powersi:panelbox-toolbar>
				<powersi:button id="btnSave" key="button_save" value="保存" onclick="saveInfo()" title="Alt+S" data-hotkey="Alt+S" ></powersi:button>
				<powersi:button id="btnCancel" key="button_cancel" value="取消" onclick="closeDialog()" title="Alt+D" data-hotkey="Alt+D" ></powersi:button>	
			</powersi:panelbox-toolbar>
			<powersi:editorlayout cols="8">
				<tr>
					<powersi:combobox id="villageinfo" valueFieldID="village_no" name="village_list" valueField="village_no" textField="village_name" 
						isMultiSelect="false"  label="村卫生室" initValue="<%=village_no %>" colspan="3">
						<powersi:hidden id="village_no"  value="" name="rationDto.village_no"/>
					</powersi:combobox>
					<powersi:textfield id="aae041" name="rationDto.aae041" label="开始年月" mask="period" maxlength="6" required="true" />
					<powersi:textfield id="aae042" name="rationDto.aae042" label="结束年月" mask="period" maxlength="6" />
				</tr>
				<tr>
					<powersi:textfield id="bkb024" name="rationDto.bkb024" label="定额"  maxlength="13"/>
					<td colspan="4"/>
				</tr>
			</powersi:editorlayout>
		</powersi:groupbox>
		<powersi:errors />
	</powersi:form>
</body>
<script type="text/javascript">
window.onload = function(){	
	
}

function saveInfo(){
	//检验必填项
	var checkValueId = ["village_no","aae041","bkb024"];
	for(var i = 0; i < checkValueId.length; i++){
		if($('#'+checkValueId[i]).val() == ""){
			popupAlert("请检查,存在未填写的必选项!", "提示", "warn");
			return false;
		}
	}
	
 	if($('#bkb024').val() != ""){
		if(isNaN($('#bkb024').val())){
			popupAlert("定额只能为数字！", "提示", "warn");
			$('#bkb024').focus();
			return false;
		}
		if(!/^\d{1,10}(\.\d{1,2})?$/.test($('#bkb024').val())){
			popupAlert("定额输入错误，小数点前只能10位，小数点后只能2位!", "提示", "warn");
			$('#bkb024').focus();
			return false;
		}
	}
 	
 	var kct1id = $('#kct1id').val();
 	var url = "";
 	if(kct1id == null || kct1id == ""){
 		url=rootPath +"/medicarepay/MedicalVillagePayAction!addPayTypeData.action";
 	}else{
 		url=rootPath +"/medicarepay/MedicalVillagePayAction!updPayTypeData.action";
 	}
 	
 	var saveItemData = $("#mainForm").serialize();
 	postJSON(url,saveItemData, function(ret) {
		if (ret.errortype == 2) {
			popupAlert(ret.message, "提示", "error");
		} else {
			popupAlert(ret.message, "提示", "success");
			closeDialog();
		}
	});
}
</script>
</powersi:html>