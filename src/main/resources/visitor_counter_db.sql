/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : visitor_counter_db

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 29/06/2025 10:57:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for console_tb
-- ----------------------------
DROP TABLE IF EXISTS `console_tb`;
CREATE TABLE `console_tb`  (
  `id` int NOT NULL COMMENT '日志记录表主键id',
  `visitor_id` int NOT NULL COMMENT '访问量关联表id',
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问源ip记录',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间（精确到秒）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_tb
-- ----------------------------
DROP TABLE IF EXISTS `user_tb`;
CREATE TABLE `user_tb`  (
  `id` int NOT NULL COMMENT '系统用户表主键',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `status` int NOT NULL DEFAULT 1 COMMENT '账号启用状态（0关闭，1开启，默认为1）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for visitor_tb
-- ----------------------------
DROP TABLE IF EXISTS `visitor_tb`;
CREATE TABLE `visitor_tb`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '访问量计数表主键',
  `target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'uuid的网站标识',
  `count` bigint(20) UNSIGNED ZEROFILL NOT NULL DEFAULT 00000000000000000000 COMMENT '网站访问量',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述备注信息',
  `status` int NULL DEFAULT 1 COMMENT '网站访问量功能启用状态（0关闭，1开启，默认为1）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Sample data for console_tb
-- ----------------------------
INSERT INTO `console_tb` (`id`, `visitor_id`, `ip_address`, `create_time`) VALUES
(1, 1, '192.168.1.1', '2025-06-29 10:00:00'),
(2, 2, '192.168.1.2', '2025-06-29 11:00:00');

-- ----------------------------
-- Sample data for user_tb
-- ----------------------------
INSERT INTO `user_tb` (`id`, `username`, `password`, `status`) VALUES
(1, 'admin', '$2a$10$M0vUmPqfXi41e8apr71GkuqWC.8q2Z6glkdikHz8dLA7w/6tGG83m', 1), -- 密码为 '123456'
(2, 'user1', '$2a$10$M0vUmPqfXi41e8apr71GkuqWC.8q2Z6glkdikHz8dLA7w/6tGG83m', 1); -- 密码为 '123456'

-- ----------------------------
-- Sample data for visitor_tb
-- ----------------------------
INSERT INTO `visitor_tb` (`id`, `target`, `count`, `description`, `status`) VALUES
(1, 'site-uuid-001', 00000000000000000005, 'Main website', 1),
(2, 'site-uuid-002', 00000000000000000010, 'Secondary site', 1);
