
<?php

error_reporting(E_ALL);
ini_set('display_errors', 'on');


require_once __DIR__ . '/facebook/autoload.php';

$fb = new \Facebook\Facebook([
  'app_id' => '1719845414979964',
  'app_secret' => '6a3d19f25900d3469962746e34e99a13',
  'default_graph_version' => 'v2.10',
  //'default_access_token' => '{access-token}', // optional
]);

$jsHelper = $fb->getJavaScriptHelper();
$facebookClient = $fb->getClient();

try {
    $accessToken = $jsHelper->getAccessToken($facebookClient);
} catch(Facebook\Exceptions\FacebookResponseException $e) {
    // When Graph returns an error
    echo 'Graph returned an error: ' . $e->getMessage();
} catch(Facebook\Exceptions\FacebookSDKException $e) {
    // When validation fails or other local issues
    echo 'Facebook SDK returned an error: ' . $e->getMessage();
}

// Use one of the helper classes to get a Facebook\Authentication\AccessToken entity.
//   $helper = $fb->getRedirectLoginHelper();
//   $helper = $fb->getJavaScriptHelper();
//   $helper = $fb->getCanvasHelper();
//   $helper = $fb->getPageTabHelper();
//echo "Hello2";
if (isset($accessToken)) {
   $fb->destroySession();
   session_destroy();
} else {
    // Unable to read JavaScript SDK cookie
    echo "fail to logout";
}

?>
