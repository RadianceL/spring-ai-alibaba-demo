package com.xinwen.ai.repository.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * vms基础商品
 *
 * @author yangzhe
 * @date 2021-09-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VmsProductPO {

    private static final long serialVersionUID = 1L;

    /**
     * 现法CODE
     */
    private String subsidiaryCode;

    /**
     * pk
     */
    private Integer productId;

    /**
     * 公司id
     */
    private Integer companyId;

    /**
     * 商品分类id
     */
    private Integer productCategoryId;

    /**
     * 商品分类id
     */
    private String productCategoryName;

    /**
     * 商品编码
     */
    private String innerCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 本地商品名称
     */
    private String localProductName;

    /**
     * 商品型号
     */
    private String productCode;

    /**
     * 品牌名称(编码)
     */
    private String brandName;

    /**
     * 品牌名称
     */
    private String brandDisp;


    /**
     * 是否是在库品 0不是 1是
     */
    private Integer stockDiv;

    /**
     * 是否是组装品 0不是 1是
     */
    private Integer packageDiv;

    /**
     * 供应商的company_id
     */
    private Integer supplierId;

    /**
     * 供应商id
     */
    private Integer supplierVendorId;

    /**
     * 供应商CODE
     */
    private String supplierCode;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商编码(不维护的)
     */
    private String supplierCodeMark;

    /**
     * 供应商名称(不维护的)
     */
    private String supplierNameMark;

    /**
     * 销售单价
     */
    private BigDecimal salePrice;

    /**
     * 单位
     */
    private String unitName;

    /**
     * 单位销售数量
     */
    private Integer unitSaleNum;

    /**
     * 单位包装数量
     */
    private Integer unitPackNum;

    /**
     * 起订量
     */
    private Integer slideQty;

    /**
     * 上架时间
     */
    private Date shelfAt;

    /**
     * 下架时间
     */
    private Date offShelfAt;

    /**
     * 结算币种
     */
    private String currency;

    /**
     * 生产所需天数
     */
    private Integer needDays;

    /**
     * 扩展字段
     */
    private String featureMapJson;

    /**
     * 是否生效，0：不生效，1：生效
     */
    private Integer isEnabled;

    /**
     * 是否软删除，0：未删除，1：删除
     */
    private Integer delFlag;

    /**
     * 排序，值越大越靠前
     */
    private Integer sort;

    /**
     * 创建人
     */
    private Integer createdBy;

    /**
     * 创建时间
     */
    private Date creationDate;

    /**
     * 最后修改人
     */
    private Integer lastUpdatedBy;

    /**
     * 最后更新时间
     */
    private Date lastUpdateDate;

    /**
     * 删除人
     */
    private Integer deletedBy;

    /**
     * 删除时间
     */
    private Date deletedDate;

    /**
     * 分类是否被删除
     */
    private Integer categoryDelFlag;

}
