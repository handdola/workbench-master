<?php

 session_start();
 //if(isset($_SESSION["facebook_id"])) {


    if (!empty($_GET["offset"])) {
        $offset = $_GET["offset"] ;
    } else {
        $offset = 0 ;
    }

    if (!empty($_GET["size"])) {
        $size = $_GET["size"] ;
    } else {
        $size = 10 ;
    }

   $piwik_server = $_SERVER["piwik"] ;
   $query = "{$piwik_server}/index.php?module=API&method=Actions.getPageUrls" .
             "&idSite=1&period=month&date=today&filter_offset=0&search_recursive=1&filter_sort_column=nb_hits" .
             "&filter_sort_order=desc&filter_limit=20&format=PHP";


     if (!empty($_GET["subview"])) {

        $subview = $_GET["subview"] ;
        if (strcmp($subview,"hotcard")==0)
            //$piwikUrl = $_SERVER["piwik"] . $query;
            $piwikUrl = $query;
        else //default view
            //$piwikUrl = $_SERVER["piwik"] . $query;
            $piwikUrl = $query;

        $response = @file_get_contents($piwikUrl);
        $content = unserialize($response);

        //echo $response;
        //die("");


/*
 Build following form to get ajax call
{
http://192.168.1.221:9200/quiznews2/employee/_mget

 "docs" : [
      { "_id" : "AV2G8nMDRgh5OCOAqxc8" ,
        "_source" : ["name","type","imgsrc","sumText"]
      },
      { "_id" : "AV2HggHIRgh5OCOAqxdA" ,
        "_source" : ["name","type","imgsrc","sumText"]
      }
   ]
}



	// Create the POST context
    $context  = stream_context_create($opts);
    echo @file_get_contents($dataUrl,false,$context);

*/
        $obj = new stdClass();
        $obj->docs = array();

        // filter /postAV2HggHIRgh5OCOAqxdA
        foreach ($content as $row) {
            $item = new stdClass();
            $item->_id =  $row['label'] ;
            $item->_source = array("name","type","imgsrc","sumText","user_id","updated","user_name", "user_pic") ;
            if (substr( $item->_id, 0, 5 ) === "/post" && strlen($item->_id) == 25) { //post{AV2HggHIRgh5OCOAqxdA}
                $item->_id =  substr($row['label'],5,20) ;
                $obj->docs[] = $item;
            }
            $startpos = strpos( $item->_id, ".html?a=") ;
            if ($startpos && ($startpos + 28) == strlen($item->_id)) { //.html?a=AV2HggHIRgh5OCOAqxdA
                $item->_id =  substr($row['label'],$startpos+8,20) ;
                $obj->docs[] = $item;
            }
        }





        $json = json_encode($obj);

        $opts = array('http' =>
            array (
                'method' => 'POST',
                'header' => 'Content-type: application/json',
                'content' => $json
            )
        );
        //echo $opts;


        // Create the POST context
        $context  = stream_context_create($opts);
        $dataUrl = $_SERVER["dataBase"] . "/_mget" ;
        echo @file_get_contents($dataUrl,false,$context);


     }

 //}  else {
 //     header('HTTP/1.1 500 Internal Server not logged in');
 //     header('Content-Type: application/json; charset=UTF-8');
 //     die(json_encode(array('message' => 'ERROR', 'code' => 1337)));
 //}

?>
