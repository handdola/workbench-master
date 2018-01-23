<?php

 session_start();
 if(!isset($_SESSION["facebook_id"])) {
    include './php/test_fblogin.php' ;
 }

 if(isset($_SESSION["facebook_id"])) {


     if (!empty($_GET["a"])) {
            $dataUrl = $_SERVER["dataBase"] . "/" . $_GET["a"] ;
     } else {
            $dataUrl = $_SERVER["dataBase"] ;
     }

    /*
     echo "url:" . $dataUrl;
     echo "json";
     echo $json;
    */

	$opts = array('http' =>
		array (
			'method' => 'DELETE',
			'header' => 'Content-type: application/json'
		)
	);

	// Create the POST context
    $context  = stream_context_create($opts);
    echo @file_get_contents($dataUrl,false,$context);
 }  else {
           header('HTTP/1.1 500 Internal Server not logged in');
           header('Content-Type: application/json; charset=UTF-8');
           die(json_encode(array('message' => 'ERROR', 'code' => 1337)));
 }

?>
