CREATE TABLE IF NOT EXISTS `[DB]`.`relation_[LOD]` (
  `id1` int(11) NOT NULL,
  `id2` int(11) NOT NULL,
  /*`similarity` float NOT NULL DEFAULT '0',*/
  PRIMARY KEY (`id1`,`id2`),
  KEY `id1_idx` (`id1`) USING BTREE,
  KEY `id2_idx` (`id2`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Layer [LOD] Uplink Relations';