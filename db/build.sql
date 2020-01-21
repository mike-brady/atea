SET character_set_server = utf8mb4;

--
-- Current Database: `atea`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `atea` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

ALTER DATABASE atea CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `atea`;

--
-- Table structure for table `abbreviation_expansion`
--

DROP TABLE IF EXISTS `abbreviation_expansion`;
CREATE TABLE `abbreviation_expansion` (
  `abbreviation_id` int(11) NOT NULL,
  `expansion_id` int(11) NOT NULL,
  PRIMARY KEY (`abbreviation_id`,`expansion_id`),
  KEY `expansion_id` (`expansion_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `abbreviations`
--

DROP TABLE IF EXISTS `abbreviations`;
CREATE TABLE `abbreviations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `value` varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `value` (`value`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `context`
--

DROP TABLE IF EXISTS `context`;
CREATE TABLE `context` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `expansion_id` int(11) NOT NULL,
  `word_id` int(11) NOT NULL,
  `distance` int(11) NOT NULL,
  `count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ids` (`expansion_id`,`word_id`,`distance`),
  KEY `word_id` (`word_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `expansions`
--

DROP TABLE IF EXISTS `expansions`;
CREATE TABLE `expansions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `value` varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `value` (`value`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `words`
--

DROP TABLE IF EXISTS `commmon_words`;
CREATE TABLE `common_words` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `examples`
--

DROP TABLE IF EXISTS `examples`;
CREATE TABLE `examples` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `abbreviation_id` int(11) NOT NULL,
  `expansion_id` int(11) NOT NULL,
  `words` TEXT COLLATE utf8mb4_unicode_ci NOT NULL,
  `abbr_index` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;