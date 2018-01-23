<?php

 session_start();
 //if(isset($_SESSION["facebook_id"])) {

     if (!empty($_GET["a"])) {

        $storyId = $_GET["a"] ;
        $dataUrl = $_SERVER["dataBase"] . "/" . $storyId ;

        echo $response = @file_get_contents($dataUrl);
     }

 //}  else {
 //     header('HTTP/1.1 500 Internal Server not logged in');
 //     header('Content-Type: application/json; charset=UTF-8');
 //     die(json_encode(array('message' => 'ERROR', 'code' => 1337)));
 //}

?>
