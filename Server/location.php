<?php
header("Content-Type: text/html; charset=UTF-8");

$preTime = $_POST['preTime'];
$curTime = $_POST['curTime'];
$Latitude = $_POST['Latitude'];
$Longitude = $_POST['Longitude'];

$response = array();
$response["preTime"] = $preTime;
$response["curTime"] = $curTime;
$response["Latitude"] = $Latitude;
$response["Longitude"] = $Longitude;

echo json_encode($response);

?>