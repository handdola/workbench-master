
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
    echo 'getAccessToken returned an error: ' . $e->getMessage();
} catch(Facebook\Exceptions\FacebookSDKException $e) {
    // When validation fails or other local issues
    echo 'getAccessToken SDK returned an error: ' . $e->getMessage();
}

// Use one of the helper classes to get a Facebook\Authentication\AccessToken entity.
//   $helper = $fb->getRedirectLoginHelper();
//   $helper = $fb->getJavaScriptHelper();
//   $helper = $fb->getCanvasHelper();
//   $helper = $fb->getPageTabHelper();
//echo "Hello2";
if (isset($accessToken)) {
   try {
      // Get the \Facebook\GraphNodes\GraphUser object for the current user.
      // If you provided a 'default_access_token', the '{access-token}' is optional.
      $response = $fb->get('/me',$accessToken);
    } catch(\Facebook\Exceptions\FacebookResponseException $e) {
      // When Graph returns an error
      echo 'fb->get returned an error: ' . $e->getMessage();
    } catch(\Facebook\Exceptions\FacebookSDKException $e) {
      // When validation fails or other local issues
      echo 'fb->get SDK returned an error: ' . $e->getMessage();
    }

    $me = $response->getGraphUser();
    echo 'Logged in as ' . $me->getName();
    $_SESSION['facebook_id'] =  "fb" . $me->getId();
    $_SESSION['facebook_name'] = $me->getName();
} else {
    // Unable to read JavaScript SDK cookie
    echo "fail to login";

    //$_SESSION['facebook_id'] = null;
    //$_SESSION['facebook_name'] = null;
}

?>
