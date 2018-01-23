<?php

    $storyTitle = "StoryFactory";
    $storyDesc = "StoryDesc";
    $storyImage = "StoryImage";
    if (!empty($_GET["a"])) {
        //echo $_SERVER["dataBase"];
        $storyId = $_GET["a"] ;
        $dataUrl = $_SERVER["dataBase"] . "/" . $storyId . "?_source=name,type,imgsrc,sumText" ;
        //echo $dataUrl;
        //try {
            //****** fetch data from ElasticSearch
            $response = @file_get_contents($dataUrl);
            //echo $response;
            if ($response) {
                $response = json_decode($response,true);
                //print_r ($response);
                $source = $response['_source'];
                //$storyTitle = $source['titleText'];
                $storyDesc = urldecode (base64_decode($source['sumText']));
                $storyImage = urldecode (base64_decode($source['imgsrc']));
                if (strpos($storyImage,"data:image/") !== false)
                   $storyImage = savePreviewImage($storyId,$storyImage);
                else
                   $storyImage = "No Image Data";

            } else {
               echo "No connection or no data";
            }
        //} catch (Exception $e)
        //{
        //    echo $e;
        //}
    }

    //return existing filename of previewDir , otherwise save it to local storage
    function savePreviewImage($storyId,$imgsrc) {

	  $start = strpos($imgsrc, ',') + 1; $length = strlen($imgsrc)- $start ;
	  $Image = base64_decode(substr($imgsrc, $start,$length));
	  $start = 11 ; $length = strpos($imgsrc, ';') - $start  ;
	  $Type = substr($imgsrc, $start,$length);
	  //print("type".$Type);
	  $fileName = $_SERVER["previewDir"]."/preview/".$storyId.".".$Type;
	  //print("filename".$fileName);
      if (!file_exists($fileName))
    	  saveTo($fileName,$Image);
	  $imgsrc = "http://".$_SERVER["domainName"]."/preview/".$storyId.".".$Type;
	  //print("imgsrc".$imgsrc);
	  return $imgsrc;
   }

   // actual save image to preview directory
   function saveTo($filename,$imgsrc) {
      $myfile = fopen($filename, "w+") or die("Unable to open file : " + $filename);
      fwrite($myfile, $imgsrc);
      fclose($myfile);
   }

?>
