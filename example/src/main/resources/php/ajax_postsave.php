<?php

 session_start();

 if(!isset($_SESSION["facebook_id"])) {
    include './php/test_fblogin.php' ;
 }

 if(isset($_SESSION["facebook_id"])) {

     $json = file_get_contents('php://input');
     if (!empty($_GET["a"])) {
            $dataUrl = $_SERVER["dataBase"] . "/" . $_GET["a"] . "?pipeline=timestamp";
     } else {
            $dataUrl = $_SERVER["dataBase"] . "/" . "?pipeline=timestamp";
     }

     // add user_id & date
     $user_id = $_SESSION["facebook_id"];
     $json = str_replace("guest_user",$user_id,$json);

     $date=strtotime('now');
     $json = str_replace("1505441175",$date,$json);

     //echo $json;
     //die("End");

    /*
     echo "url:" . $dataUrl;
     echo "json";
     echo $json;
    */

	$opts = array('http' =>
		array (
			'method' => 'POST',
			'header' => 'Content-type: application/json',
			'content' => $json
		)
	);

	// Create the POST context
    $context  = stream_context_create($opts);
    echo @file_get_contents($dataUrl,false,$context);
} else {
        header('HTTP/1.1 500 Internal Server not logged in');
        header('Content-Type: application/json; charset=UTF-8');
        die(json_encode(array('message' => 'ERROR', 'code' => 1337)));
}
?>
