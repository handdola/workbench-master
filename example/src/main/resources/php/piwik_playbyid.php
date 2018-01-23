<?php

// Usage :
// http://story.miridas.com/php/piwik_playbyid.php?id=AV2G8nMDRgh5OCOAqxc8
// return array[ label: Play/Load/Submit, nb_events : N ]

 session_start();
 //if(isset($_SESSION["facebook_id"])) {

   $piwik_server = $_SERVER["piwik"] ;

    $idstr = $_GET["id"] ;

    $query = "{$piwik_server}/index.php?date=today" .
     "&module=API" .
     "&method=Events.getAction" .
     "&secondaryDimension=eventName" .
//     "&method=Events.getName" .
//     "&secondaryDimension=eventAction" .
     "&idSite=1" .
     "&period=month" .
     //"&flat=1" .
     "&segment=eventName=={$idstr}" .
     "&format=json";

/*http://192.168.1.222/index.php?date=2017-08-24&
      viewDataTable=table&module=Events&action=
      getActionFromNameId&secondaryDimension=eventAction&idSite=1
      &period=month&popover=undefined&random=7184&idSubtable=3*/


    $response = @file_get_contents($query);

    echo $response ;
/*
    $content = unserialize($response);

    foreach ($content as $row) {
       $idsub = $row['idsubdatatable'];
    }

    //echo $idsub;

    $subquery = "{$piwik_server}/index.php?date=today" .
     "&module=API" .
     "&method=Events.getActionFromNameId" .
     "&secondaryDimension=eventAction" .
     "&idSite=1" .
     "&period=month" .
     //"&flat=1" .
     "&idSubtable={$idsub}" .
     "&format=json";

    echo @file_get_contents($subquery);
*/


?>
