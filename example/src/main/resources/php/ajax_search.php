<?php

 session_start();
 //if(isset($_SESSION["facebook_id"])) {

     // $json = file_get_contents('php://input');

    if (!empty($_GET["view"])) {
        $view = $_GET["view"] ;
    } else {
        $view = "post" ;
    }

    if (($view == "myhome") && !empty($_SESSION["facebook_id"])) {
        $user_id = $_SESSION["facebook_id"] ;
    } else {
        $user_id = "AAA" ;
    }

    //$user_id = "AAA" ;

    if ($view == "template") {
        $user_id = "fb1319950981416216" ;
    }

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

    /*
     val query = {
          "query": { "match_all": {} },
          "_source" : ["name","type","imgsrc","sumText"],
          "size": 10,
          "sort": [ {  "_timestamp": { "order": "desc" } } ]
        }

     val query = {
          "query": { "match": {"user_id" : "fb150402" } },
          "_source" : ["name","type","imgsrc","sumText"],
          "size": 10,
          "sort": [ {  "_timestamp": { "order": "desc" } } ]
        }

    */

    if ($user_id == "AAA") {
        $match_all->match_all = new stdClass();
    } else {
        $user->user_id = $user_id;
        $match_all->match = $user;
    }

    $order->order = "desc";
    $timestamp->updated = $order;

    $query->query = $match_all;
    $query->_source = Array('name','type','imgsrc','sumText','user_id','updated','user_name', 'user_pic');
    $query->from = $offset;
    $query->size = $size;
    $query->sort = Array($timestamp);


    $json = json_encode($query);


    //echo $json;
    //die("end");


            //echo $dataUrl;
            //try {
                //****** fetch data from ElasticSearch
        $opts = array('http' =>
            array (
                'method' => 'POST',
                'header' => 'Content-type: application/json',
                'content' => $json
            )
        );

        // Create the POST context
        $context  = stream_context_create($opts);
        $dataUrl = $_SERVER["dataBase"] . "/_search" ;
        echo @file_get_contents($dataUrl,false,$context);
        //echo $response;
     //echo "Hello";
//} else {
//        header('HTTP/1.1 500 Internal Server not logged in');
//        header('Content-Type: application/json; charset=UTF-8');
//        die(json_encode(array('message' => 'ERROR', 'code' => 1337)));
//}
?>
