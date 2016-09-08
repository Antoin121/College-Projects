	<?php

	$username="xxxxxx";
	$password="xxxxxx";
	$database="xxxxxx";
	$url = "xxxxxx";

	$trackID = $_POST['trackID'];

	$start=0;
	$end=400;//134 at 75ms timeout and 400 at 25ms timeout
	$three_secs=120;//40 at 75ms timeout and 120 at 25ms timeout
	$calPostReg=0;
	$calculatePost=false;

	  // Create connection
	$conn = new mysqli($url, $username, $password, $database);
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	} 


	$sql = "SELECT * FROM RMTData WHERE trackID = '$trackID'";

	$result = $conn->query($sql);

	if ($result->num_rows > 0) {
		while($row = $result->fetch_assoc()) {
			$ordering[] = $row["ordering"];
			if($row["latitude"]!=0.00000000000000){
				$latitude[] = $row["latitude"];
			}
			if($row["longitude"]!=0.00000000000000){
				$longitude[] = $row["longitude"];
			}
			
			// if($row["time"]>0{
			// 	$time[] = $row["time"];
			// }
			$time[] = $row["time"]; 
			$accel_x[] = $row["accel_x"];
			$accel_y[] = $row["accel_y"];
			$accel_z[] = $row["accel_z"];


			if($row["latitude"]!=0.00000000000000 || $row["speed"]!=0.0000000){//latitude check added incase speed is actually 0
				//echo " latitude= ".$row["latitude"]." speed= ".$row["speed"];
				$speed[] = $row["speed"];
			}
			
			if($row["direction"]!=0.0000000){
				$direction[] = $row["direction"];
			}



		}
		// echo $result->num_rows;
	}
	else {
		echo "Error: " . $sql . "<br>" . $conn->error;
	}


	while($rowsChecked<$result->num_rows){
		$new_results_pre_tilt = cal_tilt_pre($accel_x,$accel_y,$accel_z,$start,$end);
		
		if($calPostReg===0){

			$result_post = calculatePost($new_results_pre_tilt,$accel_x,$accel_y,$accel_z,$speed,$direction,$three_secs,$start,$end);
			

			$main_rotation = multiply($result_post,$new_results_pre_tilt);	


			$accel_matrix = array
			(
				array($accel_x[804]),
				array($accel_y[804]),
				array($accel_z[804]),
				);

			for($row=0; $row<sizeof($accel_matrix); $row++){ 
				for($col=0; $col<sizeof($accel_matrix[0]); $col++){ 
					echo " accel_matrix= ".$accel_matrix[$row][$col];
				}
			}

			$rotate = multiply($main_rotation,$accel_matrix);

			for($row=0; $row<sizeof($rotate); $row++){ 
				for($col=0; $col<sizeof($rotate[0]); $col++){ 
					echo " rotate= ".$rotate[$row][$col];
				}
			}


		}

		$start+=400; $end+=400;$rowsChecked+=400;$calPostReg++;
		$old_results_pre_tilt=$new_results_pre_tilt;
	}



	$index=0;
		$accel_y_sorted = array();

		//$myfile = fopen("newfile.txt", "w") or die("Unable to open file!");

		for($row=1; $row<sizeof($accel_x)-1; $row++){
			for($i=0; $i<sizeof($ordering); $i++){
				if($row==$ordering[$i]){
					$index=$i;
					//echo " row = ".$row." order = ".$ordering[$i];
					break;
				}

			}
			
			$accel_matrix = array
			(
				array($accel_x[$index]),
				array($accel_y[$index]),
				array($accel_z[$index]),// spike in Potflat at 559
				);


			$rotate = multiplyMain($main_rotation,$accel_matrix);
			$accel_y_sorted[] = $accel_z[$index];
			//echo " z= ".$accel_z[$index];
			//$txt = $rotate[1][0]."\n";
		//$txt = $accel_z[$row]."\n";
	 //fwrite($myfile, $txt);

		}
	//fclose($myfile);

		$s=0;$e=40; // checking for pot holes in 1 second windows
		$secs = floor(sizeof($accel_y_sorted)/$e);
		$limit= $e*$secs;
		
		$lowestPointIndex;
		$nearestPeakIndex;
		$potholes = array();
		
		while($e<=$limit){
			$lastLowPoint=-100;
			$lowPointsChecked=0;
			while($lowPointsChecked<5){
			$lowestPointIndex = findLowestPoint($accel_y_sorted,$s,$e,$lastLowPoint);
			$nearestPeakIndex = findNearestPeak2($accel_y_sorted,$lowestPointIndex,$s,$e);
			//echo " lowest point = ".$accel_y_sorted[$lowestPointIndex]." Highest Peak = ".$accel_y_sorted[$nearestPeakIndex];
			$diffPeaks=$accel_y_sorted[$nearestPeakIndex]-$accel_y_sorted[$lowestPointIndex];
			$diffSqrPeaks=$diffPeaks*$diffPeaks;
			$diffPeaks=sqrt($diffSqrPeaks);
			//echo " Low= ".$lowestPointIndex." diffPeaks= ".$diffPeaks;
			$add=true;
			$speedIndex=round($lowestPointIndex/120);
			if($diffPeaks>11.5 && $speed[$speedIndex]>=6.95){
				if(sizeof($potholes)<1){
					$potholes[]=$nearestPeakIndex;
				}


				for($i=0; $i<sizeof($potholes); $i++){
					
					if($nearestPeakIndex>$potholes[$i]-10 && $nearestPeakIndex<$potholes[$i]+10){
						$add=false;
					}
				}
				if($add==1){
					$potholes[]=$nearestPeakIndex;
				}
				
				//echo " diffPeaks= ".$diffPeaks;
			}
			else if($diffPeaks>5.75 && $speed[$speedIndex]<6.95){
				if(sizeof($potholes)<1){
					$potholes[]=$nearestPeakIndex;
				}
				for($i=0; $i<sizeof($potholes); $i++){
					if($nearestPeakIndex>$potholes[$i]-10 && $nearestPeakIndex<$potholes[$i]+10){
						$add=false;
					}
				}
				if($add==1){
					$potholes[]=$nearestPeakIndex;
				}
				
				//echo " diffPeaks= ".$diffPeaks;
			}
			$lastLowPoint=$accel_y_sorted[$lowestPointIndex]; $lowPointsChecked++;
		}
			$s+=40; $e+=40; 
		}

	$size=ceil(sizeof($accel_x)/120);//number of geolcation readings made during journey
	$numPotholes=array_fill(0,$size,0);//filling array with 0's
	for($i=0; $i<sizeof($potholes); $i++){
			echo " pothole at= ".$potholes[$i]." the peak is  ".$accel_y_sorted[$potholes[$i]];

			$potLocation=round($potholes[$i]/120);//using the index of the peak to round to nearest geolocation reading
			$numPotholes[$potLocation]++; //using the index as the key and value stored as the number of potholes in that area
	}

	for($i=0; $i<sizeof($numPotholes); $i++){
			echo " num potholes at ".$i." = ".$numPotholes[$i];
			$location=$i*120;
			$sql = "UPDATE RMTData SET num_potholes='$numPotholes[$i]' WHERE (trackID = '$trackID' AND ordering = '$location')";

			
			if (!$result = $conn->query($sql))
	    echo "insert failed, error: ", $conn->error;
	  
	}

	function findLowestPoint($accel_y,$start,$end,$lastLowPoint){
		$lowestPointIndex=0;

	for($i=$start; $i<$end; $i++){
				if($accel_y[$i]<$accel_y[$lowestPointIndex] && $accel_y[$i]>$lastLowPoint){
					//echo " new= ".$accel_y[$i]." pre = ".$accel_y[$lowestPointIndex];
				$lowestPointIndex=$i;
				}
				
			}
	return $lowestPointIndex;

	}


	function findNearestPeak2($accel_y,$lowestPointIndex,$start,$end){
		$peak1Index=$lowestPointIndex;
		$peak2Index=$lowestPointIndex;

	for($i=$lowestPointIndex+1; $i<$lowestPointIndex+12; $i++){
				if($accel_y[$i]>$accel_y[$peak1Index]){
					$peak1Index=$i;
				}
			}

			for($i=$lowestPointIndex-1; $i>$lowestPointIndex-12; $i--){
				if($accel_y[$i]>$accel_y[$peak2Index]){
					$peak2Index=$i;
				}
			}

			if($accel_y[$peak1Index]>$accel_y[$peak2Index]){
				return $peak1Index;
			}
			else if($accel_y[$peak2Index]>$accel_y[$peak1Index]){
				return $peak2Index;
			}
	 return 0;
	}

	function findNearestPeak($accel_y,$lowestPointIndex,$start,$end){
		$peak1Index=0;
		$peak2Index=0;

	for($i=$lowestPointIndex+1; $i<$end+3; $i++){
				if($accel_y[$i]>$accel_y[$i-1]){
					$peak1Index=$i;
				}
				else if($accel_y[$i]<$accel_y[$i-1]){
					break;
				}
			}

			for($i=$lowestPointIndex-1; $i>$start; $i--){
				if($accel_y[$i]>$accel_y[$i+1]){
					$peak2Index=$i;
				}
				else if($accel_y[$i]<$accel_y[$i+1]){
					break;
				}
			}

			if($accel_y[$peak1Index]>$accel_y[$peak2Index]){
				return $peak1Index;
			}
			else if($accel_y[$peak2Index]>$accel_y[$peak1Index]){
				return $peak2Index;
			}
	 return 0;
	}

	function cal_tilt_pre($accx,$accy,$accz,$start,$end){
		$avg_x;$avg_y;$avg_z;
		$total_x=0;
		$total_y=0;
		$total_z=0;

	for($num=$start; $num<$end; $num++){ //134 at 75ms timeout and 400 at 25ms timeout
		$total_x+=$accx[$num];
		$total_y+=$accy[$num];
		$total_z+=$accz[$num];
	}

	$avg_x=($total_x/($end-$start))/9.81;
	$avg_y=($total_y/($end-$start))/9.81;
	$avg_z=($total_z/($end-$start))/9.81;

	$theta = acos($avg_y);
	$Phi = atan($avg_z/$avg_x);
	// echo " cos Phi= ".cos($Phi)." sin Phi= ".sin($Phi);
	// echo " cos theta= ".cos($theta)." sin theta= ".sin($theta);

	$tilt  = array
	(
		array(cos($theta),sin($theta),0),
		array((sin($theta))*-1,cos($theta),0),
		array(0,0,1),
		);


	$pre_rotation  = array
	(
		array(cos($Phi),0,(sin($Phi))*-1),
		array(0,1,0),
		array(sin($Phi),0,cos($Phi)),
		);

	$accel_matrix = array
	(
		array($avg_x),
		array($avg_y),
		array($avg_z),
		);


	$result_pre_tilt = multiply($tilt,$pre_rotation);
	return $result_pre_tilt;

	}


	function calculatePost($new_results_pre_tilt,$accel_x,$accel_y,$accel_z,$speed,$direction,$three_secs,$start,$end){

		$straight=false;
		for($i=0;$i<sizeof($speed)-1;$i++){
			$diff = $speed[$i]-$speed[$i+1];//checking for acceleration or decceleration
			$diffSqr=$diff*$diff;
			$diff=(sqrt($diffSqr))/3;
			 //echo "diff = ".$diff." speed = ".$speed[$i];
			if($diff>1){
				$diffDir = $direction[$i]-$direction[$i+1];//checking if car is going straight
				$diffSqrDir=$diffDir*$diffDir;
				$diffDir=sqrt($diffSqrDir);
				if($diff<=60){
					$straight=true;
					break;
				}
				else if($direction[$i+1]>=330 && $direction[$i]<=30){
					$straight=true;
					break;
				}
				else if($direction[$i]>=330 && $direction[$i+1]<=30){
					$straight=true;
					break;
				}
				else if($diff>60){
					$straight=false;
				}
				
			}
			
		}


		$avg_x;$avg_y;$avg_z;
		$total_x=0;
		$total_y=0;
		$total_z=0;


		$avg_x=($total_x/($end-$start))/9.81;
		$avg_y=($total_y/($end-$start))/9.81;
		$avg_z=($total_z/($end-$start))/9.81;
		
		if($diff>2 && $straight==1){
			
			$i+=2;
			$rows=($i*120);
			
			for($row = $rows;$row>=($rows-$three_secs);$row--){


				$total_x+=$accel_x[$row];
				$total_y+=$accel_y[$row];
				$total_z+=$accel_z[$row];


			}

			$avg_x=($total_x/$three_secs)/10;
			$avg_y=($total_y/$three_secs)/10;
			$avg_z=($total_z/$three_secs)/10;

			$accel_matrix = array
			(
				array($avg_x),
				array($avg_y),
				array($avg_z),
				);
			$pre_rotated=multiply($new_results_pre_tilt,$accel_matrix);

			$alpha = atan($pre_rotated[0][0]/$pre_rotated[2][0]);

			$post_rotation  = array
			(
				array(cos($alpha),0,(sin($alpha))*-1),
				array(0,1,0),
				array(sin($alpha),0,cos($alpha)),
				);
			return $post_rotation;
			
		}
	}



	function multiply($a,$b) {
		$aNumRows = sizeof($a); $aNumCols = sizeof($a[0]);
		$bNumRows = sizeof($b); $bNumCols = sizeof($b[0]);
				      $m =  array();  // initialize array of rows
				      for ($r = 0; $r <$aNumRows; ++$r) {
				    $m[$r] = array(); // initialize the current row
				    for ($c = 0; $c < $bNumCols; ++$c) {
				      $m[$r][$c] = 0;             // initialize the current cell
				      for ($i = 0; $i < $aNumCols; ++$i) {
				      	$m[$r][$c] += $a[$r][$i] * $b[$i][$c];
				      }
				  }
				}
				return $m;
			}

			function multiplyMain($a,$b) {
				$aNumRows = sizeof($a); $aNumCols = sizeof($a[0]);
				$bNumRows = sizeof($b); $bNumCols = sizeof($b[0]);
				      $m =  array();  // initialize array of rows
				      for ($r = 0; $r <$aNumRows; ++$r) {
				    $m[$r] = array(); // initialize the current row
				    for ($c = 0; $c < $bNumCols; ++$c) {
				      $m[$r][$c] = 0;             // initialize the current cell
				      for ($i = 0; $i < $aNumCols; ++$i) {
				      	if($r!=1){
				      		$m[$r][$c] += $a[$r][$i] * $b[$i][$c];
				      	}
				      }
				  }
				}

				$m[1][0] += ($a[1][0] * ($b[0][0]-9.81))+ ($a[1][1] * ($b[1][0]-9.81))+ ($a[1][2] * ($b[2][0]-9.81));
				return $m;
			}

			$conn->close();

			?>