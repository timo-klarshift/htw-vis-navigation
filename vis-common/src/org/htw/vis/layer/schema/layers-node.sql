CREATE TABLE IF NOT EXISTS `[DB]`.`node_[LOD]` (
	/* node meta data */
	`id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Sequence Key',  
	`fotoliaId` int(11) NOT NULL COMMENT 'FotoliaId of representative',
	`words` mediumtext NULL COMMENT 'Textual information, comma seperated',
	`thumbPath` varchar(120) NOT NULL COMMENT 'Thumb path of representative',
  
	/* tree structure */
	`parent` int(11) NOT NULL DEFAULT -1 COMMENT 'Parent Key',
	`x` int(11) NOT NULL DEFAULT -1 COMMENT 'x-pos',
 	`y` int(11) NOT NULL DEFAULT -1 COMMENT 'y-pos',
  
 	/* indices */
	PRIMARY KEY (`id`) USING BTREE,
	KEY `fotolia_id_idx` (`fotoliaId`),
	KEY `position_x_idx` (`x`),
	KEY `position_y_idx` (`y`),
	KEY `parent_idx` (`parent`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Layer [LOD] Nodes';