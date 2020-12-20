<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="powersi" uri="http://www.powersi.com.cn/tags"%>
<powersi:html>
<head>
<powersi:head title="选择家庭病床参保人信息" target="_self" />
</head>
<body>
	<powersi:form id="choosePersonAac001HomeSickbedForm" namespace="/common"
		action="CommonManagerAction!choosePersonAac001HomeSickbed.action">
		<powersi:panelbox key="查询条件" cssStyle="display:none;">
			<powersi:hidden id="querystring" name="inHospitalDTO.querystring" />
		</powersi:panelbox>
	</powersi:form>
	<powersi:panelbox key="查询结果">
		<powersi:datagrid id="row" formId="choosePersonAac001HomeSickbedForm"
			showReload="false" checkbox="true" isMultiSelect="false"
			enabledSort="false" onSelectRow="choosePersonAac001HomeSickbed"
			pageSize="10">
			<powersi:datagrid-column name="aac003" display="姓名" width="10%" />
			<powersi:datagrid-column name="aac001" display="电脑号" width="15%" />
			<powersi:datagrid-column name="aac002" display="社会保障号码" width="10%" />
			<powersi:datagrid-column name="bka006_name" display="待遇类别名称"
				width="15%" />
			<powersi:datagrid-column name="aaz267" display="家庭病床申请序列号" width="10%" />
			<powersi:datagrid-column name="aae030" display="有效开始日期" width="10%" />
			<powersi:datagrid-column name="aae031" display="有效结束日期" width="10%" />
			<powersi:datagrid-column name="aab001" display="单位编码" width="10%" />
			<powersi:datagrid-column name="bka008" display="单位名称" width="10%" />
		</powersi:datagrid>
	</powersi:panelbox>
	<powersi:errors />
	<script type="text/javascript">
		function choosePersonAac001HomeSickbed(rowdata, rowid, rowobj) {
			var personAac001 = new Object();
			personAac001.aac001 = rowdata['aac001'];
			personAac001.aaz267 = rowdata['aaz267'];
			if (personAac001) {
				setDialogReturn(personAac001);
			}
			setTimeout("closeDialog();", 500);
		}
	</script>
</body>
</powersi:html>