package com.powersi.ssm.biz.medicare.common.action;

import com.powersi.biz.medicare.catalog.pojo.HospCataDTO;
import com.powersi.biz.medicare.catalog.service.api.HospCataApiService;
import com.powersi.biz.medicare.catalog.service.api.HospElectpresService;
import com.powersi.biz.medicare.comm.pojo.KA06DTO;
import com.powersi.biz.medicare.comm.pojo.KAB8DTO;
import com.powersi.biz.medicare.comm.pojo.ListResult;
import com.powersi.biz.medicare.comm.service.DiseaseQueryService;
import com.powersi.biz.medicare.comm.service.HospitalCatalogMatchedQueryService;
import com.powersi.biz.medicare.comm.service.IdentifyPasswordService;
import com.powersi.biz.medicare.hosp.pojo.Kzh04Dto;
import com.powersi.biz.medicare.hosp.service.api.HospManagerService;
import com.powersi.biz.medicare.inhospital.pojo.InHospitalDTO;
import com.powersi.biz.medicare.inhospital.pojo.Kad5DTO;
import com.powersi.biz.medicare.inhospital.service.api.mcce.MCCEbizh120001Service;
import com.powersi.biz.medicare.inhospital.service.api.mcce.MCCEbizh120302Service;
import com.powersi.biz.medicare.inhospital.service.bean.BeanService;
import com.powersi.comm.service.memory.MemoryDB;
import com.powersi.comm.utils.TimeUtils;
import com.powersi.hygeia.framework.CodetableMapping;
import com.powersi.hygeia.framework.exception.HygeiaException;
import com.powersi.hygeia.framework.util.UtilFunc;
import com.powersi.hygeia.web.util.DataGridHelper;
import com.powersi.hygeia.web.util.PagerHelper;
import com.powersi.ssm.biz.medicare.api.service.mcce.ConfirmListServiceImpl;
import com.powersi.ssm.biz.medicare.api.service.mcce.IdentifyPasswordServiceImpl;
import com.powersi.ssm.biz.medicare.api.service.mcce.MCCEbizh120001ServiceImpl;
import com.powersi.ssm.biz.medicare.api.service.mcce.MCCEbizh120302ServiceImpl;
import com.powersi.ssm.biz.medicare.common.util.BizHelper;
import com.powersi.ssm.biz.medicare.common.util.SFTPUtils;
import com.powersi.ssm.biz.medicare.diagnose.service.DiagnoseRegisterService;
import com.powersi.ssm.biz.medicare.inhospital.action.BaseInhospitalManagerAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.util.*;

/**
 * 公共组件管理
 *
 * @author 刘勇
 */
@Action(value = "CommonManagerAction", results = {
        @Result(name = "chooseDisease", location = "/pages/biz/medicare/comm/ChooseDisease.jsp"),
        @Result(name = "chooseDisease_remote", location = "/pages/biz/medicare/comm/ChooseDisease_remote.jsp"),
        @Result(name = "chooseDiseases", location = "/pages/biz/medicare/comm/ChooseDiseases.jsp"),
        @Result(name = "queryPersonFund", location = "/pages/biz/medicare/comm/QueryPersonFund.jsp"),
        @Result(name = "cumulativeQuery", location = "/pages/biz/medicare/comm/QueryPersonCumulative.jsp"),
        @Result(name = "choosePersonAac001Special", location = "/pages/biz/medicare/comm/ChoosePersonAac001Special.jsp"),
        @Result(name = "choosePersonAac001HomeSickbed", location = "/pages/biz/medicare/comm/ChoosePersonAac001HomeSickbed.jsp"),
        @Result(name = "chooseMedicalPeriodInjury", location = "/pages/biz/medicare/comm/ChooseMedicalPeriodInjury.jsp"),
        @Result(name = "identify", location = "/pages/biz/medicare/comm/IdentifyCard.jsp"),
        @Result(name = "choosePersonAac001", location = "/pages/biz/medicare/comm/ChoosePersonAac001.jsp"),
        @Result(name = "chooseOperation", location = "/pages/biz/medicare/comm/ChooseOperation.jsp")})
public class CommonManagerAction extends BaseInhospitalManagerAction {

    private static final long serialVersionUID = 1L;
    private final String MAP_HYGEIA_BASE_KAB8 = "MAP_HYGEIA_BASE_KAB8";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MemoryDB memoryDB;
    @Autowired
    private BeanService beanService;
    @Autowired
    private DiagnoseRegisterService diagnoseRegisterService;
    /**
     * 基金状态查询
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String queryPersonFund() {
        try {
            this.initCtrlInHospitalDTO();
            if (!this.isPostRequest()) {
                this.getInHospitalDTO().setAae030(this.getDateService().dateToString(new Date(), "yyyyMMdd"));
                this.getInHospitalDTO().setAae031(this.getDateService().dateToString(new Date(), "yyyyMMdd"));
            }
            MCCEbizh120302Service mCCEbizh120302Service = this.getHygeiaServiceManager()
                    .getBeanByClass(MCCEbizh120302ServiceImpl.class, this.getInHospitalDTO().getAkb020());
            Map fundStatusMap = new HashMap();
            List<Map> fundStatusList = mCCEbizh120302Service.queryInsuredFundList(this.getInHospitalDTO());

            if (fundStatusList != null && !fundStatusList.isEmpty()) {
                String aad006 = "";
                String aaa157 = "";
                String aae003 = "";
                String aac031 = "";
                Map monthMap = null;
                for (Map fundStatusRow : fundStatusList) {
                    aad006 = UtilFunc.getString(fundStatusRow, "aad006");
                    aaa157 = UtilFunc.getString(fundStatusRow, "aaa157");
                    aae003 = UtilFunc.getString(fundStatusRow, "aae003");
                    aac031 = UtilFunc.getString(fundStatusRow, "aac031");
                    if ("9".equals(aac031)) {
                        aac031 = "未参保";
                    } else if ("1".equals(aac031)) {
                        aac031 = "冻结";
                    } else {
                        aac031 = "正常";
                    }
                    if (StringUtils.isNotEmpty(this.getInHospitalDTO().getAaa157())
                            && !aaa157.equalsIgnoreCase(this.getInHospitalDTO().getAaa157())) {
                        continue;
                    }
                    if (!fundStatusMap.containsKey(aad006)) {
                        monthMap = new LinkedHashMap();
                        fundStatusMap.put(aad006, monthMap);
                    } else {
                        monthMap = (Map) fundStatusMap.get(aad006);
                    }

                    if ("340".equals(this.getInHospitalDTO().getAae140())) {
                        if ("202".equals(aaa157)) {
                            monthMap.put(aae003, aac031);
                        }
                    } else {
                        monthMap.put(aae003, aac031);
                    }
                }
            }

            this.setAttribute("fundStatusMap", fundStatusMap);
            return "queryPersonFund";
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 基金选择Map
     *
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map getFundMap() {
        Map fundMap = new HashMap();
        fundMap.put("301", "公务员基金");
        fundMap.put("511", "职工生育基金");
        fundMap.put("001", "职工统筹基金");
        fundMap.put("801", "居民统筹基金");
        fundMap.put("201", "大病互助基金");
        fundMap.put("806", "大病补偿基金");
        fundMap.put("306", "自付补偿基金");
        fundMap.put("006", "职工门诊统筹基金");
        return fundMap;
    }

    /**
     * 个人工伤医疗期
     *
     * @return
     */
    public String chooseMedicalPeriodInjury() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    PagerHelper.initPagination(this.getRequest());
                    MCCEbizh120001Service mCCEbizh120001Service = this.getHygeiaServiceManager()
                            .getBeanByClass(MCCEbizh120001Service.class, this.getInHospitalDTO().getAkb020());
                    List<InHospitalDTO> inHospitalDTORows = mCCEbizh120001Service
                            .searchPersonInfo(this.getInHospitalDTO());
                    if (inHospitalDTORows == null) {
                        inHospitalDTORows = new ArrayList<InHospitalDTO>();
                    }
                    PagerHelper.getPaginationObj().setCount(inHospitalDTORows.size());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(inHospitalDTORows));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseMedicalPeriodInjury";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 选择医院已匹配目录
     *
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String chooseHospitalCatalogMatched() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    this.getInHospitalDTO().setSearchType(this.getSearchType());
                    this.getInHospitalDTO().setSearchTerm(this.getSearchTerm());
                    this.getInHospitalDTO().setAke007(this.getAke007());
                    PagerHelper.initPagination(this.getRequest());
                    this.getInHospitalDTO().setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
                    this.getInHospitalDTO().setPageSize(PagerHelper.getPaginationObj().getPageSize());
                    HospitalCatalogMatchedQueryService hospitalCatalogMatchedQueryService = (HospitalCatalogMatchedQueryService) this
                            .getHygeiaServiceManager()
                            .getBean("hospitalCatalogMatchedQueryServiceesb", this.getInHospitalDTO().getAkb020());
                    // 如是村卫生站，只让检索出大类供选择
                    if ("1".equals(this.getParameter("healthposts_flag", ""))) {
                        this.getInHospitalDTO().setHealthposts_flag("1");
                    }
                    ListResult listResult = hospitalCatalogMatchedQueryService
                            .queryHospitalCatalogMatched(this.getInHospitalDTO());
                    this.loadFeeDataItemName(listResult);
                    if ("2".equals(this.getParameter("type", ""))) {
                        HospManagerService hospManagerService = this.getHygeiaServiceManager()
                                .getBeanByClass(HospManagerService.class, this.getInHospitalDTO().getAkb020());
                        Kzh04Dto kzh04Dto = new Kzh04Dto();
                        kzh04Dto.setAkb020(this.getInHospitalDTO().getAkb020());
                        List<Kzh04Dto> kzh04Dtos = hospManagerService.selectFeeSetMealFee(kzh04Dto,
                                this.getAllParameters());
                        if (kzh04Dtos != null && !kzh04Dtos.isEmpty()) {
                            List retList = hospManagerService.createKzh04InputFee(kzh04Dto, this.getAllParameters(),
                                    kzh04Dtos);
                            if (retList != null && !retList.isEmpty() && listResult.getList() != null) {
                                listResult.getList().addAll(0, retList);
                            }
                        }
                    }
                    PagerHelper.getPaginationObj().setCount(listResult.getCount());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(listResult.getList()));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseHospitalCatalogMatched";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 基准库目录
     *
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public String chooseHospitalDatumMatched() {
        try {
            if (this.isPostRequest()) {
                try {
                    PagerHelper.initPagination(this.getRequest());
                    HospCataDTO hcata = new HospCataDTO();
                    hcata.setAaa027(BizHelper.getAaa027());
                    hcata.setAkb020(BizHelper.getAkb020());
                    hcata.setBka051(this.getAke007());
                    hcata.setSearchType(this.getSearchType());
                    hcata.setSearchTerm(this.getSearchTerm());
                    hcata.setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
                    hcata.setPageSize(PagerHelper.getPaginationObj().getPageSize());
                    //hcata.setStartRow(
                    //		(this.getInHospitalDTO().getCurrentPage() - 1) * this.getInHospitalDTO().getPageSize());
                    this.beanService.copyProperties(this.getInHospitalDTO(), hcata, true);
                    HospElectpresService hospElectService = this.getHygeiaServiceManager()
                            .getBeanByClass(HospElectpresService.class, this.getInHospitalDTO().getAkb020());
                    ListResult listResult = hospElectService.queryHospitalDatumMatched(hcata);
                    List<Kad5DTO> kad5DTOs = (List<Kad5DTO>) listResult.getList();
                    for (int i = 0; kad5DTOs != null && i < kad5DTOs.size(); i++) {
                        if (StringUtils.isNotBlank(kad5DTOs.get(i).getAka065())) {
                            kad5DTOs.get(i).setAka065_name(CodetableMapping.getDisplayValue("aka065",
                                    kad5DTOs.get(i).getAka065(), kad5DTOs.get(i).getAka065()));
                        }
                        if (StringUtils.isNotBlank(kad5DTOs.get(i).getBka052())) {
                            kad5DTOs.get(i).setBka052_name(CodetableMapping.getDisplayValue("aka070",
                                    kad5DTOs.get(i).getBka052(), kad5DTOs.get(i).getBka052()));
                        }
                        if (StringUtils.isNotBlank(kad5DTOs.get(i).getAke003())) {
                            kad5DTOs.get(i).setAke003_name(CodetableMapping.getDisplayValue("ake003",
                                    kad5DTOs.get(i).getAke003(), kad5DTOs.get(i).getAke003()));
                        }
                    }
                    this.getInHospitalDTO()
                            .setTotalPage(UtilFunc.isEmpty(kad5DTOs) ? 0
                                    : (kad5DTOs.get(0).getTotleRow() + kad5DTOs.get(0).getPageSize() - 1)
                                    / kad5DTOs.get(0).getPageSize());
                    PagerHelper.getPaginationObj()
                            .setCount(UtilFunc.isEmpty(kad5DTOs) ? 0 : kad5DTOs.get(0).getTotleRow());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(kad5DTOs));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseHospitalDatumMatched";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * @param listResult
     */
    @SuppressWarnings("unchecked")
    private void loadFeeDataItemName(ListResult listResult) {
        if (listResult == null) {
            return;
        }
        if (listResult.getList() == null || listResult.getList().size() == 0) {
            return;
        }
        List<InHospitalDTO> inHospitalDTORows = (List<InHospitalDTO>) listResult.getList();
        for (int i = 0; inHospitalDTORows != null && i < inHospitalDTORows.size(); i++) {
            if (StringUtils.isNotBlank(inHospitalDTORows.get(i).getAka065())) {
                inHospitalDTORows.get(i).setAka065_name(CodetableMapping.getDisplayValue("aka065",
                        inHospitalDTORows.get(i).getAka065(), inHospitalDTORows.get(i).getAka065()));
            }
            if (StringUtils.isNotBlank(inHospitalDTORows.get(i).getAka070())) {
                inHospitalDTORows.get(i).setAka070_name(CodetableMapping.getDisplayValue("aka070",
                        inHospitalDTORows.get(i).getAka070(), inHospitalDTORows.get(i).getAka070()));
            }
            if (StringUtils.isNotBlank(inHospitalDTORows.get(i).getAke003())) {
                inHospitalDTORows.get(i).setAke003_name(CodetableMapping.getDisplayValue("ake003",
                        inHospitalDTORows.get(i).getAke003(), inHospitalDTORows.get(i).getAke003()));
            }
        }
    }

    /**
     * 选择医院已匹配目录,只用于选择套餐时使用
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public String chooseHospitalCatalogMatchedKzh04() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    this.getInHospitalDTO().setSearchType(this.getSearchType());
                    this.getInHospitalDTO().setSearchTerm(this.getSearchTerm());
                    this.getInHospitalDTO().setAke007(this.getAke007());
                    PagerHelper.initPagination(this.getRequest());
                    this.getInHospitalDTO().setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
                    this.getInHospitalDTO().setPageSize(PagerHelper.getPaginationObj().getPageSize());
                    HospCataDTO hospCataDto = new HospCataDTO();
                    hospCataDto.setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
                    hospCataDto.setPageSize(PagerHelper.getPaginationObj().getPageSize());
                    hospCataDto.setAkb020(this.getInHospitalDTO().getAkb020());
                    hospCataDto.setAae100("1");
                    String searchType = this.getInHospitalDTO().getSearchType() != null
                            ? this.getInHospitalDTO().getSearchType()
                            : "";
                    String searchTerm = this.getInHospitalDTO().getSearchTerm() != null
                            ? this.getInHospitalDTO().getSearchTerm()
                            : "";
                    if (StringUtils.isNotBlank(searchType)) {
                        if ("code".equals(searchType)) {
                            hospCataDto.setAke005(searchTerm);
                        } else if ("name".equals(searchType)) {
                            hospCataDto.setAke006(searchTerm);
                        } else if ("py".equals(searchType)) {
                            hospCataDto.setAka020(searchTerm);
                        } else if ("wb".equals(searchType)) {
                            hospCataDto.setAka021(searchTerm);
                        }
                    }
                    HospCataApiService hospCataService = this.getHygeiaServiceManager()
                            .getBeanByClass(HospCataApiService.class, this.getInHospitalDTO().getAkb020());
                    ListResult listResult = hospCataService.queryHospCataPage(hospCataDto);
                    for (Iterator iter = listResult.getList().iterator(); iter.hasNext(); ) {
                        HospCataDTO hospCataDTOT = (HospCataDTO) iter.next();
                        hospCataDTOT.setAka065(CodetableMapping.getDisplayValue("aka065", hospCataDTOT.getAka065(),
                                hospCataDTOT.getAka065()));
                    }
                    PagerHelper.getPaginationObj().setCount(listResult.getCount());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(listResult.getList()));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseHospitalCatalogMatched";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * @return
     */
    public String choosePersonAac001Special() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    PagerHelper.initPagination(this.getRequest());
                    if ("bka100".equals(this.getInHospitalDTO().getArgName())) {
                        this.getInHospitalDTO().setBka100(this.getInHospitalDTO().getQuerystring());
                    }
                    MCCEbizh120001Service mCCEbizh120001Service = this.getHygeiaServiceManager()
                            .getBeanByClass(MCCEbizh120001Service.class, this.getInHospitalDTO().getAkb020());
                    List<InHospitalDTO> inHospitalDTORows = mCCEbizh120001Service
                            .searchPersonInfo(this.getInHospitalDTO());
                    if (inHospitalDTORows == null) {
                        inHospitalDTORows = new ArrayList<InHospitalDTO>();
                    }
                    for (InHospitalDTO inHospitalDTORow : inHospitalDTORows) {
                        if (StringUtils.isNotBlank(inHospitalDTORow.getBka006())) {
                            inHospitalDTORow.setBka006_name(
                                    CodetableMapping.getDisplayValue("bka006$" + this.getInHospitalDTO().getAaa027(),
                                            inHospitalDTORow.getBka006(), inHospitalDTORow.getBka006()));
                        }
                    }
                    PagerHelper.getPaginationObj().setCount(inHospitalDTORows.size());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(inHospitalDTORows));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "choosePersonAac001Special";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 选择参保人信息
     *
     * @return
     */
    public String choosePersonAac001() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    PagerHelper.initPagination(this.getRequest());
                    if ("bka100".equals(this.getInHospitalDTO().getArgName())) {
                        this.getInHospitalDTO().setBka100(this.getInHospitalDTO().getQuerystring());
                    }
                    MCCEbizh120001Service mCCEbizh120001Service = this.getHygeiaServiceManager()
                            .getBeanByClass(MCCEbizh120001ServiceImpl.class, this.getInHospitalDTO().getAkb020());
                    List<InHospitalDTO> inHospitalDTORows = mCCEbizh120001Service
                            .searchPersonInfo(this.getInHospitalDTO());
                    if (inHospitalDTORows == null) {
                        inHospitalDTORows = new ArrayList<>();
                    }
                    PagerHelper.getPaginationObj().setCount(inHospitalDTORows.size());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(inHospitalDTORows));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "choosePersonAac001";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 家庭病床选择参保人信息
     *
     * @return
     */
    public String choosePersonAac001HomeSickbed() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    PagerHelper.initPagination(this.getRequest());
                    if ("bka100".equals(this.getInHospitalDTO().getArgName())) {
                        this.getInHospitalDTO().setBka100(this.getInHospitalDTO().getQuerystring());
                    }
                    MCCEbizh120001Service mCCEbizh120001Service = this.getHygeiaServiceManager()
                            .getBeanByClass(MCCEbizh120001ServiceImpl.class, this.getInHospitalDTO().getAkb020());
                    List<InHospitalDTO> inHospitalDTORows = mCCEbizh120001Service
                            .searchPersonInfo(this.getInHospitalDTO());
                    if (inHospitalDTORows == null) {
                        inHospitalDTORows = new ArrayList<InHospitalDTO>();
                    }
                    for (InHospitalDTO inHospitalDTORow : inHospitalDTORows) {
                        if (StringUtils.isNotBlank(inHospitalDTORow.getBka006())) {
                            inHospitalDTORow.setBka006_name(
                                    CodetableMapping.getDisplayValue("bka006$" + this.getInHospitalDTO().getAaa027(),
                                            inHospitalDTORow.getBka006(), inHospitalDTORow.getBka006()));
                        }
                    }
                    PagerHelper.getPaginationObj().setCount(inHospitalDTORows.size());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(inHospitalDTORows));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "choosePersonAac001HomeSickbed";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 选择疾病诊断
     *
     * @return
     */
    public String chooseDisease() {
        try {
            if (this.isPostRequest()) {
                try {
                    PagerHelper.initPagination(this.getRequest());
                    this.getKa06dto().setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
                    this.getKa06dto().setPageSize(PagerHelper.getPaginationObj().getPageSize());
                    this.getKa06dto().setAkb020(BizHelper.getAkb020());
                    this.getKa06dto().setAaa027(BizHelper.getAaa027());
                    this.getKa06dto().setBke217("2");

                    DiseaseQueryService diseaseQueryService = (DiseaseQueryService) this.getHygeiaServiceManager()
                            .getBean("diseaseQueryServiceesbImpl", this.getKa06dto().getAkb020());
                    List<KA06DTO> cumulativeList = diseaseQueryService.querybke216(this.getKa06dto());
                    String bke216 = "";
                    for (int i = 0; i < cumulativeList.size(); i++) {
                        KA06DTO bke216temp = cumulativeList.get(i);
                        if (bke216temp.getBke216() != null && !"".equals(bke216temp.getBke216())) {
                            bke216 = bke216temp.getBke216();
                        }
                    }
                    if (StringUtils.isBlank(bke216)) {
                    } else {
                        this.getKa06dto().setAaa027(bke216);
                    }
                    ListResult listResult = diseaseQueryService.queryDisease(this.getKa06dto());
                    PagerHelper.getPaginationObj().setCount(listResult.getCount());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(listResult.getList()));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseDisease";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 选择多疾病诊断
     *
     * @return
     */
    public String chooseDiseases() {
        try {
            if (this.isPostRequest()) {
                try {
                    PagerHelper.initPagination(this.getRequest());
                    this.getKa06dto().setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
                    this.getKa06dto().setPageSize(PagerHelper.getPaginationObj().getPageSize());
                    this.getKa06dto().setAkb020(BizHelper.getAkb020());
                    this.getKa06dto().setAaa027(BizHelper.getAaa027());
                    DiseaseQueryService diseaseQueryService = (DiseaseQueryService) this.getHygeiaServiceManager()
                            .getBean("diseaseQueryServiceesb", this.getKa06dto().getAkb020());
                    ListResult listResult = diseaseQueryService.queryDisease(this.getKa06dto());
                    PagerHelper.getPaginationObj().setCount(listResult.getCount());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(listResult.getList()));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseDiseases";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 个人累计查询
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public String cumulativeQuery() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    PagerHelper.initPagination(this.getRequest());
                    MCCEbizh120302Service mCCEbizh120302Service = this.getHygeiaServiceManager()
                            .getBeanByClass(MCCEbizh120302ServiceImpl.class, this.getInHospitalDTO().getAkb020());
                    List cumulativeList = mCCEbizh120302Service.queryCumulative(this.getInHospitalDTO());
                    if (cumulativeList != null) {
                        PagerHelper.getPaginationObj().setCount(cumulativeList.size());
                        DataGridHelper.render(this.getRequest(), this.getResponse(),
                                PagerHelper.getPaginatedList(cumulativeList));
                    }
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "cumulativeQuery";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public String queryMemoryDB() {
        try {
            Map<String, String> retMsg = new HashMap<String, String>();
            retMsg.put("suss", "0");
            String bkm017 = getParameter("bkm017");
            if (StringUtils.isBlank(bkm017)) {
                setJSONReturn(retMsg);
                return NONE;
            }
            Map<String, KAB8DTO> kab8Map = (Map<String, KAB8DTO>) this.memoryDB.getMap(MAP_HYGEIA_BASE_KAB8);
            if (kab8Map == null) {
                throw new HygeiaException("限价药品缓存为空(" + MAP_HYGEIA_BASE_KAB8 + ")!");
            }
            KAB8DTO kab8Dto = kab8Map.get(bkm017);
            if (kab8Dto != null && StringUtils.isNotBlank(kab8Dto.getAke006())) {
                retMsg.put("suss", "1");
                retMsg.put("ake006", kab8Dto.getAke006());
                retMsg.put("bkc102", kab8Dto.getBkc102());
            }
            setJSONReturn(retMsg);
        } catch (Throwable e) {
            this.saveJSONError(e.getMessage());
        }
        return NONE;
    }

    /**
     * 身份证校验
     *
     * @return
     */
    public String identifyCard() {
        try {
            if (this.isPostRequest()) {
                try {
                    this.initCtrlInHospitalDTO();
                    IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager()
                            .getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
                    Map<String, Object> map = identifyPasswordService.checkPassword(this.getInHospitalDTO());
                    setJSONReturn(map);
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "identify";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    /**
     * 身份证校验
     *
     * @return
     */
    public String checkIcCard() {
        try {
            this.initCtrlInHospitalDTO();
            IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager()
                    .getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
            Map<String, Object> map = identifyPasswordService.checkIcCard(this.getInHospitalDTO());
            setJSONReturn(map);
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * 社保卡激活
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public String queryPersonCardInfo() {
        try {
            this.initCtrlInHospitalDTO();
            this.getInHospitalDTO().setAaa027(BizHelper.getAaa027());
            this.getInHospitalDTO().setAkb020(BizHelper.getAkb020());
            IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager()
                    .getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
            Map result = identifyPasswordService.queryPersonCardInfo(this.getInHospitalDTO());
            setJSONReturn(result);
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * 社保卡信息查询
     *
     * @return
     */
    public String queryICcardInfo() {
        try {
            PagerHelper.initPagination(this.getRequest());
            this.getInHospitalDTO().setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
            this.getInHospitalDTO().setPageSize(PagerHelper.getPaginationObj().getPageSize());
            this.getInHospitalDTO().setAkb020(BizHelper.getAkb020());
            this.getInHospitalDTO().setAaa027(BizHelper.getAaa027());
            IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager()
                    .getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
            ListResult listResult = identifyPasswordService.queryICcardInfo(this.getInHospitalDTO());
            PagerHelper.getPaginationObj().setCount(listResult.getCount());
            DataGridHelper.render(this.getRequest(), this.getResponse(),
                    PagerHelper.getPaginatedList(listResult.getList()));
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * 社保卡激活查询医保信息（取基本信息）
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public String queryAC01Info() {
        try {
            PagerHelper.initPagination(this.getRequest());
            this.getInHospitalDTO().setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
            this.getInHospitalDTO().setPageSize(PagerHelper.getPaginationObj().getPageSize());
            this.getInHospitalDTO().setAkb020(BizHelper.getAkb020());
            this.getInHospitalDTO().setAaa027(BizHelper.getAaa027());
            IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager()
                    .getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
            List list = identifyPasswordService.queryAC01Info(this.getInHospitalDTO());
            setJSONReturn(list);
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * 选择手术治疗方式
     *
     * @return
     */
    public String chooseOperation() {
        try {
            if (this.isPostRequest()) {
                try {
                    PagerHelper.initPagination(this.getRequest());
                    this.getKa14dto().setCurrentPage(PagerHelper.getPaginationObj().getPageIndex());
                    this.getKa14dto().setPageSize(PagerHelper.getPaginationObj().getPageSize());
                    this.getKa14dto().setAkb020(BizHelper.getAkb020());
                    DiseaseQueryService diseaseQueryService = (DiseaseQueryService) this.getHygeiaServiceManager()
                            .getBean("diseaseQueryServiceesbImpl", this.getKa14dto().getAkb020());
                    ListResult listResult = diseaseQueryService.queryOperation(this.getKa14dto());
                    PagerHelper.getPaginationObj().setCount(listResult.getCount());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(listResult.getList()));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseOperation";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }

    public String commonAuth() {
        try {
            this.getInHospitalDTO().setAaa027(BizHelper.getAaa027());
            this.getInHospitalDTO().setAkb020(BizHelper.getAkb020());
            String name = URLDecoder.decode(this.getInHospitalDTO().getAac003(), "UTF-8");
            if (name != null && name != "") {// 解决name乱码问题
                this.getInHospitalDTO().setAac003(name);
            }
            IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager().getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
            int num = identifyPasswordService.icCardAuthCards(this.getInHospitalDTO());
            Map<String, String> retMsg = new HashMap<String, String>();
            if (num == 1) {
                retMsg.put("suss", "1");
            }
            setJSONReturn(retMsg);
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * 社保卡领卡点信息
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public String cardReceiveQueryManage() {
        try {
            this.getInHospitalDTO().setAkb020(BizHelper.getAkb020());
            this.getInHospitalDTO().setAaa027(BizHelper.getAaa027());
            IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager()
                    .getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
            List list = identifyPasswordService.cardReceiveQueryManage(this.getInHospitalDTO());
            setJSONReturn(list);
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * 解析二维码信息
     *
     * @return
     */
    public String queryPersonQrcode() {
        try {
            this.initCtrlInHospitalDTO();
            IdentifyPasswordService identifyPasswordService = this.getHygeiaServiceManager()
                    .getBeanByClass(IdentifyPasswordServiceImpl.class, this.getInHospitalDTO().getAkb020());
            Map<String, Object> map = identifyPasswordService.queryPersonQrcode(this.getInHospitalDTO());
            setJSONReturn(map);
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * TS20021100028 结算云增加工单、付款信息线上提示需求 杨世明 20200220
     * 查询派工单确认、回款确认单
     *
     * @return none
     */
    public String queryConfirmList() {
        try {
            ConfirmListServiceImpl confirmListService = this.getHygeiaServiceManager()
                    .getBeanByClass(ConfirmListServiceImpl.class, BizHelper.getAkb020());

            HashMap<String, String> paraMap = new HashMap<>();
            paraMap.put("akb020", BizHelper.getAkb020());
            paraMap.put("aaa027", BizHelper.getAaa027());
            List<Map<String, Object>> list = confirmListService.queryConfirmList(paraMap);

            List<Map<String, Object>> list1 = new ArrayList<>();
            List<Map<String, Object>> list2 = new ArrayList<>();
            for (Map<String, Object> map : list) {
                String paymentDate = MapUtils.getString(map, "payment_date");
                if (StringUtils.isNotBlank(paymentDate)) {
                    map.put("payment_date", TimeUtils.formatDate(TimeUtils.parseDate(paymentDate, "yyyy-MM-dd HH:mm:sss"), "yyyy-MM-dd"));
                }
                if ("001".equals(MapUtils.getString(map, "busi_type"))) {
                    list1.add(map);
                }
                if ("002".equals(MapUtils.getString(map, "busi_type"))) {
                    list2.add(map);
                }

            }

            Map<String, Object> result = new HashMap<>();
            result.put("list1", list1);
            result.put("list2", list2);
            setJSONReturn(result);
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveJSONError(errLogSn + e.getMessage());
        }
        return NONE;
    }

    /**
     * TS20021100028 结算云增加工单、付款信息线上提示需求 杨世明 20200220
     * 保存派工单确认、派工单建议、回款单确认、回款单信息不符等图片
     *
     * @return none
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Transactional
    public String saveImageAndFeedback() {
        String type = getParameter("type");
        String infoData = getParameter("infoData");
        String imgName = getParameter("imgName") + ".jpg";
        String imgData = getParameter("imgData");
        String mac = getParameter("mac");
        String osDisk = getParameter("osDisk");
        String displayStatus = "0";

        List<Map> needUpdateNoticeList = null;
        if ("0".equals(type) && StringUtils.isNotBlank(infoData)) {
            displayStatus = "2";
            needUpdateNoticeList = new ArrayList<>();
            for (String noticeId : infoData.split("&")) {
                Map<String, String> map = new HashMap<>();
                map.put("system_notice_id", noticeId.substring(noticeId.indexOf('=') + 1));
                needUpdateNoticeList.add(map);
            }
        }
        if (type.matches("[13]") && infoData.indexOf('=') >= 0) {
            displayStatus = "3";
            needUpdateNoticeList = new ArrayList<>();
            String noticeIdList = infoData.substring(0, infoData.indexOf('='));
            String memo = infoData.substring(infoData.indexOf('=') + 1);
            if (StringUtils.isNotBlank(noticeIdList)) {
                for (String noticeId : noticeIdList.split("[|]")) {
                    Map<String, String> map = new HashMap<>();
                    map.put("system_notice_id", noticeId);
                    map.put("memo", memo);
                    needUpdateNoticeList.add(map);
                }
            }
        }
        // 只针对回款单确认单内容进行数据分解
        if ("2".equals(type)) {
            displayStatus = "2";
            String[] infoDataArr = infoData.split("&");
            needUpdateNoticeList = new ArrayList<>(infoDataArr.length / 3);
            int num = infoDataArr.length / 3;
            for (int i = 0; i < num; i++) {
                Map<String, String> tempMap = new HashMap<>();
                for (int j = 0; j < 3; j++) {
                    int index = i * 3 + j;
                    int indexOfEqual = infoDataArr[index].indexOf('=');
                    if (indexOfEqual >= 0) {
                        tempMap.put(infoDataArr[index].substring(0, indexOfEqual), infoDataArr[index].substring(indexOfEqual + 1));
                    }
                }
                needUpdateNoticeList.add(tempMap);
            }
        }

        if (CollectionUtils.isNotEmpty(needUpdateNoticeList)) {
            String finalDisplayStatus = displayStatus;
            needUpdateNoticeList.forEach(map -> {
                String relationship = MapUtils.getString(map, "relationship", "");
                if (relationship.matches("[14]")) {
                    map.put("payment_id_card", "");
                }
                map.put("relationship", switchRelationship(relationship));
                map.put("display_status", finalDisplayStatus);
                map.put("network_info", mac);
                map.put("os_disk", osDisk);
                map.put("modifier", BizHelper.getLoginUser());
            });

            //上传图片文件到sftp服务器
            String imgBase64Data = imgData.replace("data:image/jpeg;base64,", "");//去掉浏览器base64图片的前缀
            String imgPath = "/pcloudImage/" + BizHelper.getAaa027();//使用统筹区做为子目录
            try {
                SFTPUtils.uploadImageForBase64(imgBase64Data, imgName, imgPath);

                ConfirmListServiceImpl confirmListService = this.getHygeiaServiceManager()
                        .getBeanByClass(ConfirmListServiceImpl.class, BizHelper.getAkb020());
                confirmListService.updateConfirmList(BizHelper.getAaa027(), needUpdateNoticeList);
            } catch (Exception e) {
                String errLogSn = this.addErrSNInfo();
                this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                        .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                this.saveJSONError(errLogSn + e.getMessage());
            }

        }
        return NONE;
    }

    /**
     * TS20021100028 结算云增加工单、付款信息线上提示需求 杨世明 20200220
     * 获取付款方与购方关系
     *
     * @param relationshipKey 付款方与购方关系数值
     * @return 付款方与购方关系名称
     */
    private String switchRelationship(String relationshipKey) {
        switch (relationshipKey) {
            case "1":
                return "对公账户";
            case "2":
                return "股东";
            case "3":
                return "职工";
            case "4":
                return "总/分店";
            case "5":
                return "亲属";
            case "6":
                return "客户";
            case "7":
                return "朋友";
            default:
                return "";
        }
    }


    /**
     * 选择疾病诊断
     *
     * @return
     */
    public String chooseDisease_remote() {
        try {
            if (this.isPostRequest()) {
                try {
                    PagerHelper.initPagination(getRequest());
                    List disease = this.diagnoseRegisterService.chooseDisease_remote();
                    PagerHelper.getPaginationObj().setCount(disease.size());
                    DataGridHelper.render(this.getRequest(), this.getResponse(),
                            PagerHelper.getPaginatedList(disease));
                } catch (Throwable e) {
                    String errLogSn = this.addErrSNInfo();
                    this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                            .append(this.addNotBlankParameters()).append(":处理结果:").toString());
                    this.saveJSONError(errLogSn + e.getMessage());
                }
                return NONE;
            } else {
                return "chooseDisease_remote";
            }
        } catch (Throwable e) {
            String errLogSn = this.addErrSNInfo();
            this.getCommunalService().error(this.logger, e, new StringBuilder(errLogSn).append("入参:")
                    .append(this.addNotBlankParameters()).append(":处理结果:").toString());
            this.saveError(errLogSn + e.getMessage());
            return ERROR;
        }
    }
}
