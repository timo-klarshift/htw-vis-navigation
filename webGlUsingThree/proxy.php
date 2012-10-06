<?php
header('Content-Type: image/jpeg');    $url = $_GET["url"];    echo file_get_contents($url);?>