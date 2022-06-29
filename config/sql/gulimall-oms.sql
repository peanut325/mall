/*
Navicat MySQL Data Transfer

Source Server         : 119.91.214.57
Source Server Version : 50736
Source Host           : 119.91.214.57:3306
Source Database       : gulimall-oms

Target Server Type    : MYSQL
Target Server Version : 50736
File Encoding         : 65001

Date: 2022-06-29 10:28:11
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(20) DEFAULT NULL COMMENT 'member_id',
  `order_sn` char(64) DEFAULT NULL COMMENT '订单号',
  `coupon_id` bigint(20) DEFAULT NULL COMMENT '使用的优惠券',
  `create_time` datetime DEFAULT NULL COMMENT 'create_time',
  `member_username` varchar(200) DEFAULT NULL COMMENT '用户名',
  `total_amount` decimal(18,4) DEFAULT NULL COMMENT '订单总额',
  `pay_amount` decimal(18,4) DEFAULT NULL COMMENT '应付总额',
  `freight_amount` decimal(18,4) DEFAULT NULL COMMENT '运费金额',
  `promotion_amount` decimal(18,4) DEFAULT NULL COMMENT '促销优化金额（促销价、满减、阶梯价）',
  `integration_amount` decimal(18,4) DEFAULT NULL COMMENT '积分抵扣金额',
  `coupon_amount` decimal(18,4) DEFAULT NULL COMMENT '优惠券抵扣金额',
  `discount_amount` decimal(18,4) DEFAULT NULL COMMENT '后台调整订单使用的折扣金额',
  `pay_type` tinyint(4) DEFAULT NULL COMMENT '支付方式【1->支付宝；2->微信；3->银联； 4->货到付款；】',
  `source_type` tinyint(4) DEFAULT NULL COMMENT '订单来源[0->PC订单；1->app订单]',
  `status` tinyint(4) DEFAULT NULL COMMENT '订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】',
  `delivery_company` varchar(64) DEFAULT NULL COMMENT '物流公司(配送方式)',
  `delivery_sn` varchar(64) DEFAULT NULL COMMENT '物流单号',
  `auto_confirm_day` int(11) DEFAULT NULL COMMENT '自动确认时间（天）',
  `integration` int(11) DEFAULT NULL COMMENT '可以获得的积分',
  `growth` int(11) DEFAULT NULL COMMENT '可以获得的成长值',
  `bill_type` tinyint(4) DEFAULT NULL COMMENT '发票类型[0->不开发票；1->电子发票；2->纸质发票]',
  `bill_header` varchar(255) DEFAULT NULL COMMENT '发票抬头',
  `bill_content` varchar(255) DEFAULT NULL COMMENT '发票内容',
  `bill_receiver_phone` varchar(32) DEFAULT NULL COMMENT '收票人电话',
  `bill_receiver_email` varchar(64) DEFAULT NULL COMMENT '收票人邮箱',
  `receiver_name` varchar(100) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) DEFAULT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(32) DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province` varchar(32) DEFAULT NULL COMMENT '省份/直辖市',
  `receiver_city` varchar(32) DEFAULT NULL COMMENT '城市',
  `receiver_region` varchar(32) DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `note` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `confirm_status` tinyint(4) DEFAULT NULL COMMENT '确认收货状态[0->未确认；1->已确认]',
  `delete_status` tinyint(4) DEFAULT NULL COMMENT '删除状态【0->未删除；1->已删除】',
  `use_integration` int(11) DEFAULT NULL COMMENT '下单时使用的积分',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '确认收货时间',
  `comment_time` datetime DEFAULT NULL COMMENT '评价时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES ('1', '5', '202206211004495421539066780260098050', null, '2022-06-21 10:04:50', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-21 10:04:50');
INSERT INTO `oms_order` VALUES ('2', '5', '202206211010302681539068209339052034', null, '2022-06-21 10:10:32', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-21 10:10:32');
INSERT INTO `oms_order` VALUES ('3', '5', '202206211027080321539072394264588289', null, '2022-06-21 10:27:08', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-21 10:27:08');
INSERT INTO `oms_order` VALUES ('4', '5', '202206211028598291539072863183622146', null, '2022-06-21 10:29:00', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-21 10:29:00');
INSERT INTO `oms_order` VALUES ('5', '5', '202206211037133531539074933106200578', null, '2022-06-21 10:37:13', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-21 10:37:13');
INSERT INTO `oms_order` VALUES ('6', '5', '202206211040464951539075827105320962', null, '2022-06-21 10:40:47', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-21 10:40:47');
INSERT INTO `oms_order` VALUES ('7', '5', '202206211042507421539076348281135106', null, '2022-06-21 10:42:51', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-21 10:42:51');
INSERT INTO `oms_order` VALUES ('9', '5', '202206241124325621540174005288361986', null, '2022-06-24 11:24:33', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-24 11:24:33');
INSERT INTO `oms_order` VALUES ('10', '5', '202206241126144401540174432553721858', null, '2022-06-24 11:26:15', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-24 11:26:15');
INSERT INTO `oms_order` VALUES ('11', '5', '202206241503576171540229223522828290', null, '2022-06-24 15:03:58', null, '91485.0000', '91488.0000', '3.0000', '0.0000', '0.0000', '0.0000', null, null, null, '0', null, null, '7', '91485', '91485', null, null, null, null, null, '迪迦', '18715803213', '123456', '广东省', '深圳', '南部', '深圳南山区', null, null, '0', null, null, null, null, null, '2022-06-24 15:03:58');
INSERT INTO `oms_order` VALUES ('12', '5', '202206241513086301540231534571749377', null, '2022-06-24 15:13:09', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-24 15:13:09');
INSERT INTO `oms_order` VALUES ('13', '5', '202206241519515921540233224775544833', null, '2022-06-24 15:19:52', null, '91485.0000', '91485.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '91485', '91485', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-24 15:19:52');
INSERT INTO `oms_order` VALUES ('14', '5', '202206241639485971540253344877092866', null, '2022-06-24 16:39:49', null, '24396.0000', '24396.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '24396', '24396', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-24 16:39:49');
INSERT INTO `oms_order` VALUES ('15', '5', '202206241651091561540256199323262977', null, '2022-06-24 16:51:09', null, '24396.0000', '24396.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '24396', '24396', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-24 16:51:09');
INSERT INTO `oms_order` VALUES ('16', '5', '202206251056126451540529263185260546', null, '2022-06-25 10:56:13', null, '24396.0000', '24396.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '24396', '24396', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-25 10:56:13');
INSERT INTO `oms_order` VALUES ('17', '5', '202206251343354681540571385837936641', null, '2022-06-25 13:43:36', null, '12198.0000', '12198.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '12198', '12198', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-25 13:43:36');
INSERT INTO `oms_order` VALUES ('18', '5', '202206251643133791540616591668953090', null, '2022-06-25 16:43:14', null, '12198.0000', '12198.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '12198', '12198', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-25 16:43:14');
INSERT INTO `oms_order` VALUES ('19', '5', '202206251645591151540617286803554306', null, '2022-06-25 16:45:59', null, '12198.0000', '12201.0000', '3.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '12198', '12198', null, null, null, null, null, '迪迦', '18715803213', '123456', '广东省', '深圳', '南部', '深圳南山区', null, null, '0', null, null, null, null, null, '2022-06-25 16:45:59');
INSERT INTO `oms_order` VALUES ('20', '5', '202206251702596291540621567149473794', null, '2022-06-25 17:03:00', null, '12198.0000', '12198.0000', '0.0000', '0.0000', '0.0000', '0.0000', null, null, null, '4', null, null, '7', '12198', '12198', null, null, null, null, null, '奥特曼', '18715803210', '641022', '四川省', '成都', '西南', '四川省门头沟大学18栋', null, null, '0', null, null, null, null, null, '2022-06-25 17:03:00');
INSERT INTO `oms_order` VALUES ('21', '5', '202206251707396701540622741718171650', null, '2022-06-25 17:07:40', null, '12198.0000', '12201.0000', '3.0000', '0.0000', '0.0000', '0.0000', null, null, null, '1', null, null, '7', '12198', '12198', null, null, null, null, null, '迪迦', '18715803213', '123456', '广东省', '深圳', '南部', '深圳南山区', null, null, '0', null, null, null, null, null, '2022-06-25 17:07:40');
INSERT INTO `oms_order` VALUES ('22', '5', '202206271718465791541350314689466369', null, '2022-06-27 17:25:44', null, '33.0000', '33.0000', null, null, null, null, null, null, null, '0', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `oms_order` VALUES ('23', '5', '202206271743493781541356617881198594', null, '2022-06-27 17:43:50', null, '33.0000', '33.0000', null, null, null, null, null, null, null, '0', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `oms_order` VALUES ('24', '5', '202206271747568731541357655925301249', null, '2022-06-27 17:47:57', null, '33.0000', '33.0000', null, null, null, null, null, null, null, '0', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(20) DEFAULT NULL COMMENT 'order_id',
  `order_sn` char(64) DEFAULT NULL COMMENT 'order_sn',
  `spu_id` bigint(20) DEFAULT NULL COMMENT 'spu_id',
  `spu_name` varchar(255) DEFAULT NULL COMMENT 'spu_name',
  `spu_pic` varchar(500) DEFAULT NULL COMMENT 'spu_pic',
  `spu_brand` varchar(200) DEFAULT NULL COMMENT '品牌',
  `category_id` bigint(20) DEFAULT NULL COMMENT '商品分类id',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '商品sku编号',
  `sku_name` varchar(255) DEFAULT NULL COMMENT '商品sku名字',
  `sku_pic` varchar(500) DEFAULT NULL COMMENT '商品sku图片',
  `sku_price` decimal(18,4) DEFAULT NULL COMMENT '商品sku价格',
  `sku_quantity` int(11) DEFAULT NULL COMMENT '商品购买的数量',
  `sku_attrs_vals` varchar(500) DEFAULT NULL COMMENT '商品销售属性组合（JSON）',
  `promotion_amount` decimal(18,4) DEFAULT NULL COMMENT '商品促销分解金额',
  `coupon_amount` decimal(18,4) DEFAULT NULL COMMENT '优惠券优惠分解金额',
  `integration_amount` decimal(18,4) DEFAULT NULL COMMENT '积分优惠分解金额',
  `real_amount` decimal(18,4) DEFAULT NULL COMMENT '该商品经过优惠后的分解金额',
  `gift_integration` int(11) DEFAULT NULL COMMENT '赠送积分',
  `gift_growth` int(11) DEFAULT NULL COMMENT '赠送成长值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COMMENT='订单项信息';

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES ('1', null, '202206211004495421539066780260098050', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('2', null, '202206211004495421539066780260098050', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('3', null, '202206211010302681539068209339052034', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('4', null, '202206211010302681539068209339052034', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('5', null, '202206211027080321539072394264588289', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('6', null, '202206211027080321539072394264588289', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('7', null, '202206211028598291539072863183622146', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('8', null, '202206211028598291539072863183622146', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('9', null, '202206211037133531539074933106200578', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('10', null, '202206211037133531539074933106200578', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('11', null, '202206211040464951539075827105320962', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('12', null, '202206211040464951539075827105320962', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('13', null, '202206211042507421539076348281135106', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('14', null, '202206211042507421539076348281135106', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('15', null, '202206241026323001540159408003211265', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('16', null, '202206241026323001540159408003211265', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('17', null, '202206241124325621540174005288361986', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('18', null, '202206241124325621540174005288361986', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('19', null, '202206241126144401540174432553721858', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('20', null, '202206241126144401540174432553721858', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('21', null, '202206241503576171540229223522828290', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('22', null, '202206241503576171540229223522828290', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('23', null, '202206241513086301540231534571749377', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('24', null, '202206241513086301540231534571749377', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('25', null, '202206241519515921540233224775544833', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('26', null, '202206241519515921540233224775544833', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '13', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '79287.0000', '79287', '79287');
INSERT INTO `oms_order_item` VALUES ('27', null, '202206241639485971540253344877092866', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('28', null, '202206241639485971540253344877092866', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('29', null, '202206241651091561540256199323262977', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('30', null, '202206241651091561540256199323262977', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('31', null, '202206251056126451540529263185260546', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('32', null, '202206251056126451540529263185260546', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '2', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '12198.0000', '12198', '12198');
INSERT INTO `oms_order_item` VALUES ('33', null, '202206251343354681540571385837936641', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('34', null, '202206251343354681540571385837936641', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('35', null, '202206251643133791540616591668953090', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('36', null, '202206251643133791540616591668953090', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('37', null, '202206251645591151540617286803554306', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('38', null, '202206251645591151540617286803554306', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('39', null, '202206251702596291540621567149473794', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('40', null, '202206251702596291540621567149473794', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('41', null, '202206251707396701540622741718171650', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '52', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：5G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('42', null, '202206251707396701540622741718171650', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', '225', '51', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', 'https://project-guli-education.oss-cn-chengdu.aliyuncs.com/2022-05-03/842c391d-6a37-441e-8aa4-ce5041b23ecb_23d9fbb256ea5d4a.jpg', '6099.0000', '1', '颜色：黑色;内存：8+256GB;版本：4G', '0.0000', '0.0000', '0.0000', '6099.0000', '6099', '6099');
INSERT INTO `oms_order_item` VALUES ('43', null, '202206271718465791541350314689466369', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, null, '225', null, null, null, null, '1', null, null, null, null, '33.0000', null, null);
INSERT INTO `oms_order_item` VALUES ('44', null, '202206271743493781541356617881198594', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, null, '225', null, null, null, null, '1', null, null, null, null, '33.0000', null, null);
INSERT INTO `oms_order_item` VALUES ('45', null, '202206271747568731541357655925301249', '29', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版） ', null, null, '225', null, null, null, null, '1', null, null, null, null, '33.0000', null, null);

-- ----------------------------
-- Table structure for oms_order_operate_history
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_operate_history`;
CREATE TABLE `oms_order_operate_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `operate_man` varchar(100) DEFAULT NULL COMMENT '操作人[用户；系统；后台管理员]',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  `order_status` tinyint(4) DEFAULT NULL COMMENT '订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】',
  `note` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作历史记录';

-- ----------------------------
-- Records of oms_order_operate_history
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_return_apply
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_apply`;
CREATE TABLE `oms_order_return_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(20) DEFAULT NULL COMMENT 'order_id',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '退货商品id',
  `order_sn` char(32) DEFAULT NULL COMMENT '订单编号',
  `create_time` datetime DEFAULT NULL COMMENT '申请时间',
  `member_username` varchar(64) DEFAULT NULL COMMENT '会员用户名',
  `return_amount` decimal(18,4) DEFAULT NULL COMMENT '退款金额',
  `return_name` varchar(100) DEFAULT NULL COMMENT '退货人姓名',
  `return_phone` varchar(20) DEFAULT NULL COMMENT '退货人电话',
  `status` tinyint(1) DEFAULT NULL COMMENT '申请状态[0->待处理；1->退货中；2->已完成；3->已拒绝]',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `sku_img` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `sku_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `sku_brand` varchar(200) DEFAULT NULL COMMENT '商品品牌',
  `sku_attrs_vals` varchar(500) DEFAULT NULL COMMENT '商品销售属性(JSON)',
  `sku_count` int(11) DEFAULT NULL COMMENT '退货数量',
  `sku_price` decimal(18,4) DEFAULT NULL COMMENT '商品单价',
  `sku_real_price` decimal(18,4) DEFAULT NULL COMMENT '商品实际支付单价',
  `reason` varchar(200) DEFAULT NULL COMMENT '原因',
  `description述` varchar(500) DEFAULT NULL COMMENT '描述',
  `desc_pics` varchar(2000) DEFAULT NULL COMMENT '凭证图片，以逗号隔开',
  `handle_note` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `handle_man` varchar(200) DEFAULT NULL COMMENT '处理人员',
  `receive_man` varchar(100) DEFAULT NULL COMMENT '收货人',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `receive_note` varchar(500) DEFAULT NULL COMMENT '收货备注',
  `receive_phone` varchar(20) DEFAULT NULL COMMENT '收货电话',
  `company_address` varchar(500) DEFAULT NULL COMMENT '公司收货地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单退货申请';

-- ----------------------------
-- Records of oms_order_return_apply
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_return_reason
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_reason`;
CREATE TABLE `oms_order_return_reason` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) DEFAULT NULL COMMENT '退货原因名',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `status` tinyint(1) DEFAULT NULL COMMENT '启用状态',
  `create_time` datetime DEFAULT NULL COMMENT 'create_time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货原因';

-- ----------------------------
-- Records of oms_order_return_reason
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_setting
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_setting`;
CREATE TABLE `oms_order_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `flash_order_overtime` int(11) DEFAULT NULL COMMENT '秒杀订单超时关闭时间(分)',
  `normal_order_overtime` int(11) DEFAULT NULL COMMENT '正常订单超时时间(分)',
  `confirm_overtime` int(11) DEFAULT NULL COMMENT '发货后自动确认收货时间（天）',
  `finish_overtime` int(11) DEFAULT NULL COMMENT '自动完成交易时间，不能申请退货（天）',
  `comment_overtime` int(11) DEFAULT NULL COMMENT '订单完成后自动好评时间（天）',
  `member_level` tinyint(2) DEFAULT NULL COMMENT '会员等级【0-不限会员等级，全部通用；其他-对应的其他会员等级】',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单配置信息';

-- ----------------------------
-- Records of oms_order_setting
-- ----------------------------

-- ----------------------------
-- Table structure for oms_payment_info
-- ----------------------------
DROP TABLE IF EXISTS `oms_payment_info`;
CREATE TABLE `oms_payment_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_sn` char(64) DEFAULT NULL COMMENT '订单号（对外业务号）',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `alipay_trade_no` varchar(50) DEFAULT NULL COMMENT '支付宝交易流水号',
  `total_amount` decimal(18,4) DEFAULT NULL COMMENT '支付总金额',
  `subject` varchar(200) DEFAULT NULL COMMENT '交易内容',
  `payment_status` varchar(20) DEFAULT NULL COMMENT '支付状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `callback_content` varchar(4000) DEFAULT NULL COMMENT '回调内容',
  `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='支付信息表';

-- ----------------------------
-- Records of oms_payment_info
-- ----------------------------
INSERT INTO `oms_payment_info` VALUES ('1', '202206251707396701540622741718171650', null, '2022062522001402510501747034', null, null, 'TRADE_SUCCESS', null, null, null, '2022-06-25 17:08:01');

-- ----------------------------
-- Table structure for oms_refund_info
-- ----------------------------
DROP TABLE IF EXISTS `oms_refund_info`;
CREATE TABLE `oms_refund_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_return_id` bigint(20) DEFAULT NULL COMMENT '退款的订单',
  `refund` decimal(18,4) DEFAULT NULL COMMENT '退款金额',
  `refund_sn` varchar(64) DEFAULT NULL COMMENT '退款交易流水号',
  `refund_status` tinyint(1) DEFAULT NULL COMMENT '退款状态',
  `refund_channel` tinyint(4) DEFAULT NULL COMMENT '退款渠道[1-支付宝，2-微信，3-银联，4-汇款]',
  `refund_content` varchar(5000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款信息';

-- ----------------------------
-- Records of oms_refund_info
-- ----------------------------
