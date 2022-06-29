/*
Navicat MySQL Data Transfer

Source Server         : 119.91.214.57
Source Server Version : 50736
Source Host           : 119.91.214.57:3306
Source Database       : gulimall-wms

Target Server Type    : MYSQL
Target Server Version : 50736
File Encoding         : 65001

Date: 2022-06-29 10:29:44
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of undo_log
-- ----------------------------

-- ----------------------------
-- Table structure for wms_purchase
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase`;
CREATE TABLE `wms_purchase` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assignee_name` varchar(255) DEFAULT NULL,
  `phone` char(13) DEFAULT NULL,
  `priority` int(4) DEFAULT NULL,
  `status` int(4) DEFAULT NULL,
  `ware_id` bigint(20) DEFAULT NULL,
  `amount` decimal(18,4) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COMMENT='采购信息';

-- ----------------------------
-- Records of wms_purchase
-- ----------------------------
INSERT INTO `wms_purchase` VALUES ('1', '1', 'admin', '13612345678', '1', '2', null, null, '2022-05-04 12:01:45', '2022-05-04 13:43:51');
INSERT INTO `wms_purchase` VALUES ('2', null, null, null, null, '0', null, null, '2022-05-04 13:43:43', '2022-05-04 13:43:43');
INSERT INTO `wms_purchase` VALUES ('3', '1', 'admin', '13612345678', '1', '2', null, null, '2022-05-04 13:44:40', '2022-05-04 13:44:40');
INSERT INTO `wms_purchase` VALUES ('4', null, null, null, null, '0', null, null, '2022-05-04 13:45:25', '2022-05-04 13:45:25');
INSERT INTO `wms_purchase` VALUES ('5', '1', 'admin', '13612345678', null, '3', null, null, '2022-05-04 14:47:59', '2022-05-04 14:56:07');
INSERT INTO `wms_purchase` VALUES ('6', '1', 'admin', '13612345678', null, '3', null, null, '2022-05-04 15:38:43', '2022-05-04 15:41:08');
INSERT INTO `wms_purchase` VALUES ('7', '1', 'admin', '13612345678', null, '3', null, null, '2022-05-04 15:42:13', '2022-05-04 15:55:33');

-- ----------------------------
-- Table structure for wms_purchase_detail
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_detail`;
CREATE TABLE `wms_purchase_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_id` bigint(20) DEFAULT NULL COMMENT '采购单id',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '采购商品id',
  `sku_num` int(11) DEFAULT NULL COMMENT '采购数量',
  `sku_price` decimal(18,4) DEFAULT NULL COMMENT '采购金额',
  `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
  `status` int(11) DEFAULT NULL COMMENT '状态[0新建，1已分配，2正在采购，3已完成，4采购失败]',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of wms_purchase_detail
-- ----------------------------
INSERT INTO `wms_purchase_detail` VALUES ('1', '1', '1', '10000', null, '1', '4');
INSERT INTO `wms_purchase_detail` VALUES ('2', '1', '2', '200', null, '1', '2');
INSERT INTO `wms_purchase_detail` VALUES ('3', '1', '200', '200', null, '1', '2');
INSERT INTO `wms_purchase_detail` VALUES ('4', '1', '10000', '100', null, '1', '2');
INSERT INTO `wms_purchase_detail` VALUES ('5', '2', '300', '300', null, '2', '1');
INSERT INTO `wms_purchase_detail` VALUES ('6', '4', '120', '120', null, '2', '1');
INSERT INTO `wms_purchase_detail` VALUES ('7', '4', '220', '224', null, '1', '1');
INSERT INTO `wms_purchase_detail` VALUES ('8', '5', '51', '10', null, '1', '3');
INSERT INTO `wms_purchase_detail` VALUES ('9', '5', '52', '10', null, '1', '3');
INSERT INTO `wms_purchase_detail` VALUES ('10', '5', '53', '10', null, '1', '3');
INSERT INTO `wms_purchase_detail` VALUES ('11', '6', '54', '1', null, '2', '1');
INSERT INTO `wms_purchase_detail` VALUES ('12', '6', '55', '1', null, '2', '1');
INSERT INTO `wms_purchase_detail` VALUES ('13', '6', '56', '2', null, '2', '1');
INSERT INTO `wms_purchase_detail` VALUES ('14', '7', '57', '57', null, '57', '3');

-- ----------------------------
-- Table structure for wms_ware_info
-- ----------------------------
DROP TABLE IF EXISTS `wms_ware_info`;
CREATE TABLE `wms_ware_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) DEFAULT NULL COMMENT '仓库名',
  `address` varchar(255) DEFAULT NULL COMMENT '仓库地址',
  `areacode` varchar(20) DEFAULT NULL COMMENT '区域编码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息';

-- ----------------------------
-- Records of wms_ware_info
-- ----------------------------
INSERT INTO `wms_ware_info` VALUES ('1', '西南总仓库', '成都', '5201314');
INSERT INTO `wms_ware_info` VALUES ('2', '南部总仓库', '深圳', '200020');

-- ----------------------------
-- Table structure for wms_ware_order_task
-- ----------------------------
DROP TABLE IF EXISTS `wms_ware_order_task`;
CREATE TABLE `wms_ware_order_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(20) DEFAULT NULL COMMENT 'order_id',
  `order_sn` varchar(255) DEFAULT NULL COMMENT 'order_sn',
  `consignee` varchar(100) DEFAULT NULL COMMENT '收货人',
  `consignee_tel` char(15) DEFAULT NULL COMMENT '收货人电话',
  `delivery_address` varchar(500) DEFAULT NULL COMMENT '配送地址',
  `order_comment` varchar(200) DEFAULT NULL COMMENT '订单备注',
  `payment_way` tinyint(1) DEFAULT NULL COMMENT '付款方式【 1:在线付款 2:货到付款】',
  `task_status` tinyint(2) DEFAULT NULL COMMENT '任务状态',
  `order_body` varchar(255) DEFAULT NULL COMMENT '订单描述',
  `tracking_no` char(30) DEFAULT NULL COMMENT '物流单号',
  `create_time` datetime DEFAULT NULL COMMENT 'create_time',
  `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
  `task_comment` varchar(500) DEFAULT NULL COMMENT '工作单备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COMMENT='库存工作单';

-- ----------------------------
-- Records of wms_ware_order_task
-- ----------------------------
INSERT INTO `wms_ware_order_task` VALUES ('1', null, '202206241026323001540159408003211265', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('3', null, '202206241126144401540174432553721858', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('4', null, '202206241503576171540229223522828290', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('5', null, '202206241513086301540231534571749377', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('6', null, '202206241519515921540233224775544833', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('7', null, '202206241639485971540253344877092866', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('8', null, '202206241651091561540256199323262977', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('9', null, '202206251056126451540529263185260546', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('10', null, '202206251343354681540571385837936641', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('11', null, '202206251643133791540616591668953090', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('12', null, '202206251645591151540617286803554306', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('13', null, '202206251702596291540621567149473794', null, null, null, null, null, null, null, null, null, null, null);
INSERT INTO `wms_ware_order_task` VALUES ('14', null, '202206251707396701540622741718171650', null, null, null, null, null, null, null, null, null, null, null);

-- ----------------------------
-- Table structure for wms_ware_order_task_detail
-- ----------------------------
DROP TABLE IF EXISTS `wms_ware_order_task_detail`;
CREATE TABLE `wms_ware_order_task_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(20) DEFAULT NULL COMMENT 'sku_id',
  `sku_name` varchar(255) DEFAULT NULL COMMENT 'sku_name',
  `sku_num` int(11) DEFAULT NULL COMMENT '购买个数',
  `task_id` bigint(20) DEFAULT NULL COMMENT '工作单id',
  `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
  `lock_status` int(1) DEFAULT NULL COMMENT '1-已锁定  2-已解锁  3-扣减',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COMMENT='库存工作单';

-- ----------------------------
-- Records of wms_ware_order_task_detail
-- ----------------------------
INSERT INTO `wms_ware_order_task_detail` VALUES ('1', '51', '', '2', '1', '1', '1');
INSERT INTO `wms_ware_order_task_detail` VALUES ('2', '52', '', '13', '1', '1', '1');
INSERT INTO `wms_ware_order_task_detail` VALUES ('4', '51', '', '2', '3', '1', '1');
INSERT INTO `wms_ware_order_task_detail` VALUES ('5', '52', '', '13', '3', '1', '1');
INSERT INTO `wms_ware_order_task_detail` VALUES ('6', '51', '', '2', '4', '1', '1');
INSERT INTO `wms_ware_order_task_detail` VALUES ('7', '52', '', '13', '4', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('8', '51', '', '2', '5', '1', '1');
INSERT INTO `wms_ware_order_task_detail` VALUES ('9', '52', '', '13', '5', '1', '1');
INSERT INTO `wms_ware_order_task_detail` VALUES ('10', '51', '', '2', '6', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('11', '52', '', '13', '6', '1', '4');
INSERT INTO `wms_ware_order_task_detail` VALUES ('12', '51', '', '2', '7', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('13', '52', '', '2', '7', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('14', '51', '', '2', '8', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('15', '52', '', '2', '8', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('16', '51', '', '2', '9', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('17', '52', '', '2', '9', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('18', '52', '', '1', '10', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('19', '51', '', '1', '10', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('20', '52', '', '1', '11', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('21', '51', '', '1', '11', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('22', '52', '', '1', '12', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('23', '51', '', '1', '12', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('24', '52', '', '1', '13', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('25', '51', '', '1', '13', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('26', '52', '', '1', '14', '1', '2');
INSERT INTO `wms_ware_order_task_detail` VALUES ('27', '51', '', '1', '14', '1', '2');

-- ----------------------------
-- Table structure for wms_ware_sku
-- ----------------------------
DROP TABLE IF EXISTS `wms_ware_sku`;
CREATE TABLE `wms_ware_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(20) DEFAULT NULL COMMENT 'sku_id',
  `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
  `stock` int(11) DEFAULT NULL COMMENT '库存数',
  `sku_name` varchar(200) DEFAULT NULL COMMENT 'sku_name',
  `stock_locked` int(11) DEFAULT '0' COMMENT '锁定库存',
  PRIMARY KEY (`id`),
  KEY `sku_id` (`sku_id`) USING BTREE,
  KEY `ware_id` (`ware_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COMMENT='商品库存';

-- ----------------------------
-- Records of wms_ware_sku
-- ----------------------------
INSERT INTO `wms_ware_sku` VALUES ('1', '51', '1', '50', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 4G', '9');
INSERT INTO `wms_ware_sku` VALUES ('2', '52', '1', '100', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 8+256GB 5G', '76');
INSERT INTO `wms_ware_sku` VALUES ('3', '53', '1', '10', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 16+256GB 4G', '0');
INSERT INTO `wms_ware_sku` VALUES ('4', '54', '2', '1', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  黑色 16+256GB 5G', '0');
INSERT INTO `wms_ware_sku` VALUES ('5', '55', '2', '1', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  白色 8+256GB 4G', '0');
INSERT INTO `wms_ware_sku` VALUES ('6', '56', '2', '2', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  白色 8+256GB 5G', '0');
INSERT INTO `wms_ware_sku` VALUES ('7', '57', '2', '171', '华为mate40pro 5G手机 夏日胡杨 8+128G全网通（4G版）  白色 16+256GB 4G', '0');
