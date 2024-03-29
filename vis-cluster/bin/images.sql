CREATE TABLE IF NOT EXISTS `images_[LOD]` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Sequence Key',
  `fotoliaId` int(11) NOT NULL COMMENT 'FotoliaId of representative',
  `words` mediumtext COMMENT 'Textual information, comma seperated',
  `thumbPath` varchar(120) NOT NULL COMMENT 'Thumb path of representative',
  `parent` int(11) NOT NULL DEFAULT '-1' COMMENT 'Parent Key',
  `x` int(11) NOT NULL DEFAULT '-1' COMMENT 'x position',
  `y` int(11) NOT NULL DEFAULT '-1' COMMENT 'y position',
  
  `score` double NULL DEFAULT '-1' COMMENT 'single score',
  `quadScore` double NULL DEFAULT '-1' COMMENT 'quad score',
  
  PRIMARY KEY (`id`) USING BTREE,
  KEY `fotolia_id_idx` (`fotoliaId`),
  KEY `parent_idx` (`parent`),
  KEY `px_idx` (`x`),
  KEY `py_idx` (`y`),
  KEY `s1_idx` (`score`),
  KEY `s2_idx` (`quadScore`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Layer [LOD] Nodes'