<?php
    //OWNER: S. NAIMOLI

    header("Access-Control-Allow-Origin: *");
    
    require_once("Rest.inc.php");
    require_once("controllers/UserController.php");
    require_once("controllers/ExpenseController.php");
    require_once("controllers/TeamController.php");
    require_once("models/TeamUtilities.php");
    require_once("controllers/TeamMemberController.php");
    require_once("controllers/TeamTasksController.php");
    require_once("controllers/ManagerController.php");
    require_once("controllers/TeamEventController.php");
    
    require_once("externalControllers/FoursquareController.php");
    
    class API extends REST 
    {
    
        public $data = "";
        
        
        /*
         * Public method for access api.
         * This method dynmically call the method based on the query string
         *
         */
        public function processApi()
        {
            $func = strtolower(trim(str_replace("/","_",$_REQUEST['rquest'])));
            list ($group, $myFunc) = explode("_", $func);
            $myFunc = $group . "_" . $myFunc;
            if((int)method_exists($this,$myFunc) > 0)
                $this->$myFunc();
                #$this->$func();
            else
                $this->response('',404);                
            // If the method not exist with in this class, response would be "Page not found".
        }


//==========================================DO NOT EDIT ABOVE=======================================

        private function Team_CreateTeam() {
            $newTeam = new TeamController;
            $tu = new TeamUtilities;
            $jsonMsg = array();
            $failure = false;
            $idArray = array();
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }   
    
            $teamArray = $this->_request;
            
            $authUser = $this->AuthRequired($teamArray);

            logIt(var_export($teamArray, true));

            if(!isset($teamArray['teamName'])) {
                $jsonMsg = array('status' => 'failure', 'response'=> "teamName is not filled in");
                $failure = true;
            } else if(!isset($teamArray['teamLocationName'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "teamLocationName is not filled in");                
                $failure = true;
            } else if(!isset($teamArray['teamManaged'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "teamManaged is not filled in");
                $failure = true;
            } else if(!isset($teamArray['loginId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "loginId is not filled in");
                $failure = true;
            }
            
            if($failure == false) {
                if($tu->TeamNameUsed($teamArray['teamName']) == true) {
                    $jsonMsg = array('status' => 'failure', 'response'=> "team name is already used");
                    $failure = true;
                }
            }
            
            if($failure == false) {
                $idArray = $newTeam->CreateTeam($teamArray, $teamArray['loginId']);
                if (count($idArray) == 2) {
                    $jsonMsg = array('status' => 'success', 'response'=> $idArray);
                } else {
                    $jsonMsg = array('status' => 'failure', 'response' => "team couldn't be created or creator couldn't be added as team member");
                }
            }
            
            $this->response($this->json($jsonMsg), 200);
        }   
    
        private function Team_GetAllTeams() {
            $selectTeam = new TeamController;
            $teamList = array();
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }   
    
            $teamList = $selectTeam->GetAllTeams();    
            
            if(!empty($teamList)) { 
                $jsonMsg = array('status' => 'success', 'response' => $teamList);
            } else {
                $jsonMsg = array('status' => 'failure', 'response' => "no teams registered");
            }
            
            $this->response($this->json($jsonMsg), 200);
        }
        
        private function Team_SearchAllTeams() {
            $selectTeam = new TeamController;
            $teamList =  array();
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }   
    
            $info = $this->_request;
            
            $authUser = $this->AuthRequired($info);
            
            if(!isset($info['teamName'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "team name not filled in");
                $failure = true;
            }
            
            if($failure == false) {            
                $teamList = $selectTeam->SearchAllTeams($info['teamName']);
                
                if(!empty($teamList)) { 
                    $jsonMsg = array('status' => 'success', 'response' => $teamList);
                } else {
                    $jsonMsg = array('status' => 'failure', 'response' => "no teams have string in team name");
                }
            }
            $this->response($this->json($jsonMsg), 200);            
        }
        
        private function Team_GetTeam() 
        {
            //multiple errors in here. you have the function requiring POST, but you are looking for GET responses.
            //mismatched function calls and parameter values
            $selectTeam = new TeamController;
            $aTeam = new Team;
            $failure = true;

            if($this->get_request_method() != "POST") 
            {
                $this->response('',406);
            }

            $info = $this->_request;            
            $authUser = $this->AuthRequired($info);

            if(isset($info['loginId']))
            {
                $username = $info['loginId'];
                if(isset($info['teamId'])) 
                {
                    $teamId = $info['teamId'];                   
                    //$aTeam = $selectTeam->GetTeamFromID($info['teamId'], $info['loginId']);
                    $x1 = $selectTeam->GetTeamFromID($info['teamId'], $info['loginId']);
                }                
            }

            $message = "";
            if ($x1 == -998)
            {
                //added functionality
                //if this happens, then the user either does not belong to the team
                //OR
                //the user is pendingApproval <--- this is the likely case

                //break team not found if then
                $x1 = null;
                $x1 = array();                
                $x1['teamId'] = -998;
                $x1['teamName'] = "xxx";
            }

            if(($x1['teamId'] == -999) or ($x1['teamName'] == "")) 
            {   
                //might be N/a
                $failure=true;
                $message = "team not found";
            }
            elseif(($x1['teamId'] == -998) or ($x1['teamName'] == "xxx")) 
            {
                $failure = true;
                $message = "team not found or membership still pendingApproval";
            }
            else 
            {
                $failure=false;
            }

            if($failure == true) 
            {
                $respArray = array('status' => 'failure', 'response' => $message);
            } else 
            {
                //$newTeam = get_object_vars($aTeam);
                $newTeam = $x1;
                $respArray = array('status' => 'success', 'response' => $newTeam);
            }

            logIt(var_export($respArray, true));
            $this->response($this->json($respArray), 200);
        }

        private function Team_GetTeamList() {
            $getTeamList = new TeamController;
            $xUser = new UserController;
            $teamList = array();
            $teamListArray = array();
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);

            $xUser = $xUser->GetUserFromLoginID($info['loginId']);            
            $userId = $xUser->userId;

            if(isset($userId)) {              
                $teamList = $getTeamList->GetTeamListForUser($userId);
                if(!empty($teamList)) {
                    $respArray = array('status' => "success", 'response' => $teamList);                
                } else {
                    $respArray = array('status' => "failure", 'response' => 'user not part of any team');
                }
            } else {
                $respArray = array('status' => "failure", 'response' => 'loginId required');
            }
            
           
            
            $this->response($this->json($respArray), 200);

        }

        private function Team_ModifyTeam() {
            $modifyTeam = new TeamController;
            $tm = new TeamMembers;
            $xUser = new UserController;
            $failure = false;

            if($this->get_request_method() != "POST")
            {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            if(!isset($info['loginId'])) {
                $respArray = array('status' => "failure", 'response' => 'login Id is not filled in');
                $failure = true;
            } else if(!isset($info['teamId'])) {
                $respArray = array('status' => "failure", 'response' => 'teamId is not filled in');
                $failure = true;
            }             
            
            $xUser = $xUser->GetUserFromLoginID($info['loginId']);            
            $userId = $xUser->userId;

            if($failure == false) {
                if($tm->VerifyTeamMemberExist($info['teamId'], $userId)) {                    
                    $queryResult = $modifyTeam->ModifyTeam($info,$userId);
                    if($queryResult == true) {
                        $respArray =array('status' => "success" , 'response'=>'change successful');
                    }
                } else {
                    $respArray = array('status' => "failure", 'response' => 'user not part of team');
                }
            }
            
            $this->response($this->json($respArray),200);
        }

        private function Team_ChangeTeamName() {
            $modifyTeamName = new TeamController;
            $tm = new TeamMembers;
            $xUser = new UserController;
            $failure = false;

            if($this->get_request_method() != "POST")
            {
                $this->response('',406);
            }            
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            if(!isset($info['loginId'])) {
                $respArray = array('status' => "failure", 'response' => 'login Id is not filled in');
                $failure = true;
            } else if(!isset($info['teamId'])) {
                $respArray = array('status' => "failure", 'response' => 'teamId is not filled in');
                $failure = true;
            } else if(!isset($info['teamName'])) {
                $respArray = array('status' => "failure", 'response' => 'team name is not filled in');
                $failure = true;
            }
            
            $xUser = $xUser->GetUserFromLoginID($info['loginId']);            
            $userId = $xUser->userId;
            
            if($failure == false ) {
                if($tm->VerifyTeamMemberExist($info['teamId'], $userId)) {
                    $queryResult = $modifyTeamName->ModifyTeamName($info['teamId'],$info['teamName']);
                    if($queryResult == true) {
                        $respArray =array('status' => "success" , 'response'=>'change successful');
                    }
                } else {
                    $respArray = array('status' => "failure", 'response' => 'user not part of team');
                }
            }
            $this->response($this->json($respArray),200);            
        }
        
        private function TeamMember_JoinTeam() {
            $joinTeam = new TeamMemberController;
            $tu = new TeamUtilities;
            $tm = new TeamMembers;
            $user = new User;
            $userController = new UserController;
            
            $failure = false;
            $teamMemberId = -999;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }

            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']);

            if(!isset($info['teamId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "team id is not filled in");
                $failure = true;
            } else if(!isset($info['loginId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "username is not filled in");
                $failure = true;
            } else if($tu->TeamIdExists($info['teamId']) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "team Id does not exist");
                $failure = true;
            } else if($tm->VerifyTeamMemberExist($info['teamId'], $user->userId) == true) {
                $jsonMsg = array('status' => 'failure' ,'response' => "team member already exists");
                $failure = true;
            } else if($tm->VerifyTeamMemberPending($info['teamId'], $user->userId) == true) {
                $jsonMsg = array('status' => 'failure' ,'response' => "team membership already pending");
                $failure = true;
            }
            
            if($failure == false ) {               
                $teamMemberId = $joinTeam->JoinTeam($info['teamId'], $info['loginId']);
                
                if ($teamMemberId > 0) {
                    $jsonMsg = array('status' => 'success', 'response'=> $teamMemberId);
                } else {
                    $jsonMsg = array('status' => 'failure', 'response' => "user couldn't be added to team");
                }
            }          

            $this->response($this->json($jsonMsg), 200);

        }
        
        private function TeamMember_LeaveTeam()  {
            $leaveTeam = new TeamMemberController;
            $tu = new TeamUtilities;
            $tm = new TeamMembers;

            $userController = new UserController;
            $user = new User;

            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']);
            
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            if(!isset($info['teamId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "team id is not filled in");
                $failure = true;
            } else if(!isset($info['loginId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "login id is not filled in");
                $failure = true;
            } else if ($tu->TeamIdExists($info['teamId']) == false ) {
                $jsonMsg = array('status' => 'failure', 'response' => "team not found");
                $failure = true;
            } else if($tm->VerifyTeamMemberExist($info['teamId'],$user->userId) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "user not on team");
                $failure = true;
            } else if (!isset($info['confirmed'])) {
                $jsonMsg = array('status' => 'failure', 'response'=> "is this first or second deletion attempt");
                $failure = true;
            }
            
            
            if($failure == false) {
                if($info['confirmed'] == "false") {
                   
                    $result = $leaveTeam->RemoveTeamMemberFalseConfirmation($info['teamId'],$user->userId);                    
                   
                    if($result == 0) {
                        $jsonMsg = array('status' => 'failure', 'response' => "team will be deleted");                    
                    } else if($result ==1 ){
                        $jsonMsg = array('status' => 'success' ,'response' => "team member successfully deleted");
                    } else {
                        $jsonMsg = array('status' => 'failure', 'response' => "deletion failed");
                    }
                    
                } else {
                    $result = $leaveTeam->DeleteTeamMemberTeamRemove($info['teamId'],$user->userId);
                    
                    if($result == 1) {
                        $jsonMsg = array('status' => 'success', 'response' => "teamMember successfully deleted");
                    } else if($result == 2) {
                        $jsonMsg = array('status' => 'success', 'response' => "team member and team successfully deleted");                        
                    } else {
                        $jsonMsg = array('status' => 'failure', 'response' => "deletion failed");                        
                    }                    
                }
            }
            
            $this->response($this->json($jsonMsg), 200);
        }

        private function User_EmailExist()
        {   
            $xUser = new UserController;
            $failure = true;
            $resp = false;

            if ($this->get_request_Method() != "GET")
            {   
                $this->response('', 406);
            }   
    
            if (isset($_GET['email']) && strlen($_GET['email']) > 0)
            {   
               $resp = $xUser->EmailExist($_GET['email']); 
               $failure = false;
            }   
            else
            {   
                $failure = true;
                $resp = false;
            }   

            if ($failure)
            {   
                $retArray = array ('response' => 'failure', 'message' => 'email not submitted');
                $this->response($this->json($retArray),401);
            }   
            else
            {   
                if ($resp)
                {   
                    $retArray = array ('response' => 'success', 'message' => 'not available');
                    // if true - email is used
                }   
                else
                {   
                    $retArray = array ('response' => 'success', 'message' => 'available');
                    // if false - email is not used and is available for registration
                }   
                $this->response($this->json($retArray),200);
            }   

        }   


        private function User_LoginIDExist()
        {
            $xUser = new UserController;
            $failure = true;
            $resp = false;

            if ($this->get_request_Method() != "GET")
            {
                $this->response('', 406);
            }
    
            if (isset($_GET['loginId']) && strlen($_GET['loginId']) > 0)
            {
               $resp = $xUser->LoginIDExist($_GET['loginId']); 
               $failure = false;
            }
            else
            {
                $failure = true;
                $resp = false;
            }

            if ($failure)
            {
                $retArray = array ('response' => 'failure', 'message' => 'loginId not submitted');
                $this->response($this->json($retArray),401);
            }
            else
            {
                if ($resp)
                {
                    $retArray = array ('response' => 'success', 'message' => 'not available');
                    // if true - user is taken
                }
                else
                {
                    $retArray = array ('response' => 'success', 'message' => 'available');
                    // if false - user is available
                }
                $this->response($this->json($retArray),200);
            }

        }

        private function User_AuthenticatePassword()
        {
            $xUser = new UserController;
            $failure = true;
    
            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }        
            
            $authArray = $this->_request;

            if (isset($authArray['loginId']))
            {
                $xUser = $xUser->GetUserFromLoginID($authArray['loginId']);
                if ($xUser->userId <> "-999")
                {
                    if (isset($authArray['password']))
                    {   
                        //validate hash
                        $good = $xUser->ValidatePasswordHash($authArray['password']);
                        if ($good)
                        {
                            $retArray = array ('response' => 'success', 'userIdentifier' => $xUser->userIdentifier, 'userSecret' => $xUser->userSecret);
                            $failure = false;
                        }
                        else
                        {
                            //bad password
                            $retArray = array ('response' => 'failure', 'message' => 'bad password');
                            $failure = true;
                        }
                    }   
                    else
                    {   
                        //did not submit password
                        $failure = true;
                        $retArray = array ('response' => 'failure', 'message' => 'password not submitted');
                    }   
                }
                else
                {
                    //user not found
                    $failure = true;
                    $retArray = array ('response' => 'failure', 'message' => 'user not found');
                }
                
            }   
            else
            {
                //did not submit user
                $failure = true;
                $retArray = array ('response' => 'failure', 'message' => 'user not submitted');
            }

            if ($failure)
            {
                $this->response($this->json($retArray),401);
            }
            else
            {
                $this->response($this->json($retArray),200);
            }            
             
            // expect loginId and password
            // return userIdentifier and userSecret
        }       
        
        private function User_ResetPassword()
        {
            $xUser = new UserController;
            $failed = true;

            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }

            $info = $this->_request;
            
            if (isset($info['loginId']))
            {
                //user sent
                $xUser   = $xUser->GetUserFromLoginID($info['loginId']);
                if ($xUser->userId <> "-999")
                {
                    $newPass = $xUser->GenerateRandomPassword();
                    $ret     = $xUser->ChangeUserPassword($newPass);

                    if ($ret)
                    {
                        //password changed
                        $failed = false;
                        $message = "password reset";
                        $email   = $xUser->SendPasswordResetEmail($newPass);
                    }
                    else
                    {
                        //password change failure
                        $failed = true;
                        $message = "password change failure";
                    }
                }
                else
                {
                    //user not found
                    $failed = true;
                    $message = "user not found";
                }
            }
            else
            {
                //user not sent
                $failed = true;
                $message = "loginId not sent";
            }

            if ($failed)
            {    
                $retArray = array('response' => 'failure', 'message' => $message);
                $this->response($this->json($retArray),401);
            }    
            else 
            {    
                $retArray = array('response' => 'success', 'message' => $message);
                $this->response($this->json($retArray),200);
            }    
        }

        private function User_ChangePassword()
        {
            //MUST BE AUTHENTICATED
            $xUser = new UserController;
            $failed = true;
            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }

            $info = $this->_request;
            
            //check authentication; assign user; change password.
            $authUser = $this->AuthRequired($info);

            if (isset($info['loginId']))
            {
                $xUser = $xUser->GetUserFromLoginID($info['loginId']);

                if ($xUser->userId <> "-999")
                {
                    if (isset($info['newPassword']))
                    {
                        $ret = $xUser->ChangeUserPassword($info['newPassword']);
                        if ($ret)
                        {
                            //change successful
                            $retArray = array ('response' => 'success', 'message' => 'password changed');
                            $failed = false;
                        }
                        else
                        {
                            //change failed
                            $retArray = array ('response' => 'failure', 'message' => 'password change failed');
                            $failed = true;
                        }   
                    }
                    else
                    {
                        //new password not set
                        $retArray = array ('response' => 'failure', 'message' => 'newPassword not set');
                        $failed = true;
                    }
                }
                else
                {
                    //user not found
                    $retArray = array ('response' => 'failure', 'message' => 'user not found');
                    $failed = true;
                }
            }
            else
            {
                //user not submitted
                $retArray = array ('response' => 'failure', 'message' => 'user not submitted');
                $failed = true;
            }

            if ($failed)
            {
                $this->response($this->json($retArray),401);
            }
            else
            {
                $this->response($this->json($retArray),200);
            }

        }
 
        private function User_CreateUser()
        {
            $xUser = new UserController;
            
            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }
            //pass user info as array
            $usrArray = $this->_request;
            logIt("creating user");
            logIt(var_export($usrArray, true)); 
            $newUid = $xUser->CreateUser($usrArray);
            $jsonMsg = array(); 
            logIt("new uid = " . var_export($newUid, true));

            if ($newUid >= 0)
            {
                $jsonMsg = array('response' => 'success', 'message'=> $newUid);
            }
            else
            {
                $jsonMsg = array('response' => 'failure', 'message'=> $newUid);
            }

            $this->response($this->json($jsonMsg),200);
        }

        private function User_GetUser()
        {
            //GetUserFromLoginID
            
            $xUser = new UserController;
            $failure = true;

            if ($this->get_request_method() != "GET")
            {
                $this->response('',406);
            }
    
            if (isset($_GET['loginId']))
            {
                $loginId = $_GET['loginId'];
                $xUser = $xUser->GetUserFromLoginID($loginId);
            }
            elseif (isset($_GET['userId']))
            {
                $userId = $_GET['userId'];
                $xUser = $xUser->GetUserFromID($userId);
            }
        
            if ($xUser->userId == -999)
            {
                $failure = true;
            }
            else
            {
                $failure = false;
            }

            if ($failure == true)
            {
                $resp = array('response' => "failure", 'message' => "user not found");
            }
            else
            {
                //convert object to array for json
                $xUser = get_object_vars($xUser);
                unset($xUser['userSalt']);
                unset($xUser['userIdentifier']);
                unset($xUser['userSecret']);
                unset($xUser['password']);
                $resp = array('response' => "success", 'message' => $xUser);            
            }
            
            $this->response($this->json($resp),200);
        }

        private function User_ModifyUser()
        {
            $xUser = new UserController;

            if($this->get_request_method() != "POST")
            {
                $this->response('',406);
            }


            $array1         = $this->_request;
            $authUser = $this->AuthRequired($array1);

            $response       = $xUser->ModifyUser($array1);
            $jsonstr        = array('response' => $response);
            $this->response($this->json($jsonstr),200);

        }

        private function User_UpdateLocation()
        {
            $xUser = new UserController;
            
            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }

            $locInfo        = $this->_request;
            $authUser       = $this->AuthRequired($locInfo);
            $xUser          = $xUser->GetUserFromLoginID($locInfo['loginId']);

            $response       = $xUser->UpdateUserLocation($locInfo['lat'], $locInfo['lng']);

            $jsonstr = array();

            if ($response >= 0)
            {
                $jsonstr = array('response' => 'success');
            }
            else
            {
                $jsonstr = array('response' => 'failure');
            }

            $this->response($this->json($jsonstr), 200);
        }

        private function Manager_PendingUsers()
        {
            $xUser = new UserController;
            $xMan  = new ManagerController;

            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }

            $pendingUsers   = array();
            $failure        = true;
            $info           = $this->_request;

            $authUser       = $this->AuthRequired($info);

            $xUser          = $xUser->GetUserFromLoginID($info['loginId']);
            $manUserId      = $xUser->userId;

            if (isset($info['teamId']))
            {  
                $teamId         = $info['teamId'];

                //check for Manager
                if ($xMan->IsManager($teamId, $manUserId) == true)
                {  
                    //is a manager
                    $failure = false;
                    $pendingUsers = $xMan->GetTeamPendingUsers($teamId);
                }
                else
                {  
                    //not a manager
                    $failure = true;
                    $message = "requesting user is not a manager";
                    $pendingUsers = null;
                }
            }
            else
            {  
                //assume you want to know about all teams you're a manager for.
                $failure = false;
                $pendingUsers = $xMan->GetTeamPendingUsersAllTeams($manUserId);
            }

            $jsonstr = array();

            if ($failure)
            {  
                $jsonstr = array('response' => 'failure', 'message' => $message);
                $this->response($this->json($jsonstr), 401);
            }
            else
            {  
                $jsonstr = array('response' => 'success', 'message' => $pendingUsers);
                $this->response($this->json($jsonstr), 200);
            }
        }

        private function Manager_TakeAction()
        {
            $xUser = new UserController;
            $xMan  = new ManagerController;

            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }    

            $failure        = true;
            $info           = $this->_request;

            $authUser       = $this->AuthRequired($info);

            //get info about manager
            $xUser          = $xUser->GetUserFromLoginID($info['loginId']);
            $manUserId      = $xUser->userId;

            //get info about subject user
            $xUser          = new UserController;
            $xUser          = $xUser->GetUserFromLoginID($info['subjectLoginId']);
            $subUserId     = $xUser->userId;

            //team assignment
            $teamId         = $info['teamId'];

            //action
            $action         = $info['action'];

            //check for Manager
            if ($xMan->IsManager($teamId, $manUserId) == true)
            {    
                //is a manager
                $ret = $xMan->TakeAction($teamId, $subUserId, $action);

                if ($ret)
                {
                    $failure = false;
                    //email notify user
                    if ($action == "approve")
                    {
                        $xTu = new TeamUtilities;
                        $teamName = $xTu->GetTeamName($teamId);
                        $emailmsg = "Hi " .  $info['subjectLoginId'] . ", <br><br>" .
                        "Your membership status for a team has been recently approved. <br><br>" .
                        "Welcome to $teamName! <br><br>" . 
                        "Thanks,<br>AwayTeam Admins";
                        $subject = "AwayTeam - Team Membership Approved";

                        //should still be subject User
                        $xUser->EmailUser($emailmsg, $subject);
                    }
                }
                else
                {
                    $message = "$action action failed.";
                    $failure = true;
                }
            }    
            else 
            {    
                //not a manager
                $failure = true;
                $message = "requesting user is not a manager";
            }    

            $jsonstr = array();

            if ($failure)
            {
                $jsonstr = array('response' => 'failure', 'message' => $message);
                $this->response($this->json($jsonstr), 401);
            }
            else
            {
                $jsonstr = array('response' => 'success');
                $this->response($this->json($jsonstr), 200);
            }    
        }
        
        private function TeamTasks_CreateTask() {
            $teamTask = new TeamTasksController;
            $tm = new TeamMembers;
            $user = new User;
            $userController = new UserController;
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']);
            
            if(!isset($info['taskTeamId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task team id is not filled in");
                $failure = true;
            } else if(!isset($info['taskTitle'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task title is not filled in");
                $failure = true;
            } else if(!isset($info['taskDescription'])) {
                $jsonMsg = array('status' => 'failure','response' => "task description is not filled in");
                $failure = true;
            }  else if($tm->VerifyTeamMemberExist($info['taskTeamId'],$user->userId) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "user not on team");
                $failure = true;
            }
            
            if($failure == false) {
                $newId = $teamTask->CreateTeamTasks($info);
                
                if($newId >= 0) {
                    $jsonMsg = array('status' => 'success', 'response' => $newId);
                } else {
                    $jsonMsg = array('status' => 'failure', 'response' => "team task couldn't be created");
                }            
            }

            $this->response($this->json($jsonMsg),200);
        }
        
        private function TeamTasks_UpdateTask() {
            $teamTask = new TeamTasksController;
            $tm = new TeamMembers;
            $use = new User;
            $userController = new UserController;
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']);
            
            if(!isset($info['taskId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task Id is not filled in");
                $failure = true;
            } else if(!isset($info['taskCompleted'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task completion is not filled in");
                $failure = true;
            } else if(!isset($info['taskDeletion'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task deletion is not filled in");
                $failure = true;
            } else if(!isset($info['taskTeamId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task team Id is not filled in");
                $failure = true;
            } else if(!isset($info['loginId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "login Id is not filled in");
                $failure = true;
            }else if($tm->VerifyTeamMemberExist($info['taskTeamId'],$user->userId) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "user not on team");
                $failure = true;
            }
            
            if($failure == false) {
                if($info['taskCompleted'] == "true") {
                    $resultComplete = $teamTask->ChangeTeamTaskToComplete($info['taskId'],$info['taskCompleted']);                   
                } else if($info['taskCompleted'] == "false") {
                    $resultComplete = $teamTask->ChangeTeamTaskToComplete($info['taskId'],$info['taskCompleted']);
                }
                
                if($info['taskDeletion'] == "true") {
                    $resultDeletion = $teamTask->RemoveTeamTask($info['taskId']);
                }
                
                if($resultComplete == true && $resultDeletion == true) {
                    $jsonMsg = array('status' => 'success','response' => "task successfully mark completed or incomplete and then deleted");
                } else if($resultComplete == true) {
                    $jsonMsg = array('status' => 'success', 'response' => "task successfully marked complete or incomplete");
                } else if($resultDeletion == true) {
                    $jsonMsg = array('status' => 'success', 'response' => "task successfully deleted");
                }
            }
            
            $this->response($this->json($jsonMsg),200);
        }
        
        private function TeamTasks_EditTask() {
            $teamTask = new TeamTasksController;
            $tm = new TeamMembers;
            $user = new User;
            $userController = new UserController;
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']);
            
            if(!isset($info['taskId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task Id is not filled in");
                $failure = true;
            } else if(!isset($info['taskTitle'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task title is not filled in");
                $failure = true;
            } else if(!isset($info['taskDescription'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task description is not filled in");
                $failure = true;
            } else if(!isset($info['taskTeamId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "task team Id is not filled in");
                $failure = true;
            } else if(!isset($info['loginId'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "login Id is not filled in");
                $failure = true;
            } else if($tm->VerifyTeamMemberExist($info['taskTeamId'],$user->userId) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "user not on team");
                $failure = true;
            }
            
            if($failure == false) {
                $result = $teamTask->ModifyTeamTaskModel($info);
                
                if($result == true) {
                    $jsonMsg = array('status' => 'success' , 'response' => "change completed successfully");
                } else {
                    $jsonMsg = array('status' => 'failure' , 'response' => "change couldn't be completed");
                }
            }
            
            $this->response($this->json($jsonMsg),200);
        }

        //added by request
        private function TeamTasks_GetTasks()
        {    
            $xTask = new TeamTasksController;

            if ($this->get_request_method() != "POST")
            {    
                $this->response('',406);
            }    

            $array1         = $this->_request;
            $authUser       = $this->AuthRequired($array1);

            $results        = $xTask->GetTeamTasks($array1['taskTeamId']);

            if (is_array($results))
            {    
               // do nothing... good stuff 
            }    
            else 
            {    
                if ($results->taskId == -999)
                {    
                    //nothing found
                    $jsonstr = array('response' => 'failure', 'message' => 'null');
                    $this->response($this->json($jsonstr),200);
                    exit;
                }    
            }    
            //results found
            $jsonstr = array('response' => 'success', 'message' => $results);
            $this->response($this->json($jsonstr),200);
        }    

        
        private function TeamEvent_CreateEvent() {
            $teamEventController = new TeamEventController;
            $aTeamEvent = new TeamEvent;
            $tm = new TeamMembers;
            $user = new User;
            $userController = new UserController;
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']);
            
            if(!isset($info['teamEventTeamId'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "team Id is not filled in");
                $failure = true;
            } else if(!isset($info['loginId'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "login Id is not filled in");
                $failure = true;
            } else if(!isset($info['teamEventName'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event name is not filled in");
                $failure = true;
            } else if(!isset($info['teamEventLocationString'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event location is not filled in");
                $failure = true;
            } else if (!isset($info['teamEventStartTime'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event start time is not filled in");
                $failure = true;
            } else if(!isset($info['teamEventEndTime'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "event end time is not filled in");
                $failure = true;
            } else if (!isset($info['teamEventDescription'])) {
                $jsonMsg = array('status' => 'failure' ,'response' => "event description is not filled in");
                $failure = true;
            } else if($tm->VerifyTeamMemberExist($info['teamEventTeamId'],$user->userId) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "user not on team");
                $failure = true;
            } else if($aTeamEvent->ValidateDateTime($info['teamEventStartTime']) == false) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event start time invalid");
                $failure = true;
            } else if($aTeamEvent->ValidateDateTime($info['teamEventEndTime']) == false) {
                $jsonMsg = array('status' => 'failure' ,'response' => "event end time invalid");
                $failure=true;
            }
            
            if($failure == false) {
                $teamEventId = $teamEventController->CreateTeamEvent($info);
                
                if($teamEventId > 0 ) {
                    $jsonMsg = array('status' => 'success' ,'response' => $teamEventId);
                } else {
                    $jsonMsg = array('status' => 'failure' , 'response' => "team event couldn't be created");
                }
            }
            
            $this->response($this->json($jsonMsg),200);
        }
        
        private function TeamEvent_EditEvent() {
            $teamEventController = new TeamEventController;
            $aTeamEvent = new TeamEvent;
            $tm = new TeamMembers;
            $user = new User;
            $userController = new UserController;
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']); 
            
            if(!isset($info['teamEventTeamId'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "team Id is not filled in");
                $failure = true;
            } else if(!isset($info['loginId'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "login Id is not filled in");
                $failure = true;
            } else if (!isset($info['teamEventId'])) {
                $jsonMsg = array('status' => 'failure' ,'response' => "team event Id is not filled in");
                $failure = true;
            }else if(!isset($info['teamEventName'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event name is not filled in");
                $failure = true;
            } else if(!isset($info['teamEventLocationString'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event location is not filled in");
                $failure = true;
            } else if (!isset($info['teamEventStartTime'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event start time is not filled in");
                $failure = true;
            } else if(!isset($info['teamEventEndTime'])) {
                $jsonMsg = array('status' => 'failure', 'response' => "event end time is not filled in");
                $failure = true;
            } else if (!isset($info['teamEventDescription'])) {
                $jsonMsg = array('status' => 'failure' ,'response' => "event description is not filled in");
                $failure = true;
            } else if($tm->VerifyTeamMemberExist($info['teamEventTeamId'],$user->userId) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "user not on team");
                $failure = true;
            } else if($aTeamEvent->ValidateDateTime($info['teamEventStartTime']) == false) {
                $jsonMsg = array('status' => 'failure' , 'response' => "event start time invalid");
                $failure = true;
            } else if($aTeamEvent->ValidateDateTime($info['teamEventEndTime']) == false) {
                $jsonMsg = array('status' => 'failure' ,'response' => "event end time invalid");
                $failure=true;
            } else if($aTeamEvent->VerifyTeamEventTeamId($info['teamEventTeamId'],$info['teamEventId']) == false) {
                $jsonMsg = array('status' => 'failure' , 'response' => "team event does not belong to the team");
                $failure = true;
            }
            
            if($failure == false) {
                $retCode = $teamEventController->ModifyEventModel($info);
                
                if($retCode == true) {
                    $jsonMsg = array('status' => 'success' ,'response' => "change was successful");
                } else {
                    $jsonMsg = array('status' => 'failure' , 'response' => "change was unsuccessful");
                }
            }
            
            $this->response($this->json($jsonMsg),200);
        }
        
        private function TeamEvent_DeleteEvent() {
            $teamEventController = new TeamEventController;
            $tm = new TeamMembers;
            $user = new User;
            $userController = new UserController;
            $failure = false;
            
            if($this->get_request_method() != "POST") {
                $this->response('',406);
            }
            
            $info = $this->_request;
            $authUser = $this->AuthRequired($info);
            
            $user = $userController->GetUserFromLoginID($info['loginId']);
            
            if(!isset($info['teamEventTeamId'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "team Id is not filled in");
                $failure = true;
            } else if(!isset($info['loginId'])) {
                $jsonMsg = array('status' => 'failure' , 'response' => "login Id is not filled in");
                $failure = true;
            } else if (!isset($info['teamEventId'])) {
                $jsonMsg = array('status' => 'failure' ,'response' => "team event Id is not filled in");
                $failure = true;
            } else if($tm->VerifyTeamMemberExist($info['teamEventTeamId'],$user->userId) == false) {
                $jsonMsg = array('status' => 'failure', 'response' => "user not on team");
                $failure = true;
            }
            
            if($failure == false) {
                $retCode = $teamEventController->RemoveEvent($info['teamEventId']);
                
                if($retCode == true) {
                    $jsonMsg = array('status' => 'success' , 'response' => "deletion successful");
                } else {
                    $jsonMsg = array('status' => 'failure' , 'response' => "deletion unsuccessful");
                }
            }
            
            $this->response($this->json($jsonMsg),200);
        }

        private function Expense_CreateExpense()
        {   
            $xUser    = new UserController;
            $xExpense = new ExpenseController;
    
            if ($this->get_request_method() != "POST")
            {   
                $this->response('', 406);
            }   
            $expArray = $this->_request;

            if (isset($expArray['loginId']))
            {
                $loginId = $expArray['loginId'];
                $xUser = $xUser->GetUserFromLoginID($loginId);
                unset($expArray['loginId']);
                $expArray['userId'] = $xUser->userId;
            }


            $authUser = $this->AuthRequired($expArray);
            $newExp = $xExpense->CreateExpense($expArray);

            $jsonMsg = array(); 

            if ($newExp > 0)
            {   
                $jsonMsg = array('response' => 'success', 'message'=> $newExp);
            }   
            else
            {   
                $jsonMsg = array('response' => 'failure', 'message'=> $newExp);
            }   

            $this->response($this->json($jsonMsg),200);
        }   

        private function Expense_ModifyExpense()
        {   
            $xUser    = new UserController;
            $xExpense = new ExpenseController;

            if($this->get_request_method() != "POST")
            {   
                $this->response('',406);
            }   

            $array1         = $this->_request;
            $authUser       = $this->AuthRequired($array1);

            if (isset($array1['loginId']))
            {    
                $loginId = $array1['loginId'];
                $xUser = $xUser->GetUserFromLoginID($loginId);
                unset($array1['loginId']);
                $array1['userId'] = $xUser->userId;
            }    


            $response       = $xExpense->ModifyExpense($array1);
            
            if ($response)
            {
                $jsonstr = array('response' => 'success', 'message' => 'expense modified');
            }
            else
            {
                $jsonstr = array('response' => 'failure', 'message' => 'expense not modified');
            }
            $this->response($this->json($jsonstr),200);
        }   

        private function Expense_DeleteExpense()
        {
            $xUser    = new UserController;
            $xExpense = new ExpenseController;
            
            if ($this->get_request_method() != "POST")
            {
                $this->response('',406);
            }
            
            $array1                 = $this->_request;
            $authUser               = $this->AuthRequired($array1);

            if (isset($array1['loginId']))
            {    
                $loginId = $array1['loginId'];
                $xUser = $xUser->GetUserFromLoginID($loginId);
                unset($array1['loginId']);
                $array1['userId'] = $xUser->userId;
            }    


            $xExpense->expenseId    = $array1['expenseId'];
            $response               = $xExpense->RemoveExpense();

            if ($response)
            {
                $jsonstr = array('response' => 'success', 'message' => 'expense deleted');
            }
            else
            {
                $jsonstr = array('response' => 'failure', 'message' => 'expense not deleted');
            }           
            $this->response($this->json($jsonstr),200);
        }

        private function Expense_GetExpense()
        {
            $xUser    = new UserController;
            $xExpense = new ExpenseController;

            if ($this->get_request_method() != "POST")
            {
                $this->response('',406);
            }

            $array1         = $this->_request;
            $authUser = $this->AuthRequired($array1);

            if (isset($array1['loginId']))
            {    
                $loginId = $array1['loginId'];
                $xUser = $xUser->GetUserFromLoginID($loginId);
                unset($array1['loginId']);
                $array1['userId'] = $xUser->userId;
            }    


            $results        = $xExpense->GetExpense($array1);

            if (is_array($results))
            {
               // do nothing... good stuff 
            }
            else
            {
                if ($results->expenseId == -999)
                {
                    //nothing found
                    $jsonstr = array('response' => 'no results');
                    $this->response($this->json($jsonstr),200);
                    exit;
                }
            }
            //results found
            $jsonstr = array('response' => $results);
            $this->response($this->json($jsonstr),200);

        }

        private function Expense_GetReceipt()
        {    
            $xExpense = new ExpenseController;
            $xUser    = new UserController;

            if ($this->get_request_method() != "GET")
            {    
                $this->response('', 406);
            }    

            $info = $this->_request;

            if (isset($info['loginId']))
            {    
                $loginId = $info['loginId'];
                $xUser = $xUser->GetUserFromLoginID($loginId);
                unset($info['loginId']);
                $info['userId'] = $xUser->userId;
            }    
     
            $data = $xExpense->GetReceipt($info);

            if ($data != NULL)
            {    
                list($junk,$img) = explode(',', $data);
                $img = base64_decode($img);
                $img = imagecreatefromstring($img);
                header('Content-Type: image/jpeg');
                imagejpeg($img, NULL, 75); 
                imagedestroy($img);
                exit;
            }    
        }  

        private function Expense_PutReceipt()
        {
            $xExpense = new ExpenseController;
            $xUser    = new UserController;

            if ($this->get_request_method() != "POST")
            {
                $this->response('', 406);
            }

            $info = $this->_request;
            $putData = $_FILES['file'];
            $authUser = $this->AuthRequired($info);

            if (isset($info['loginId']))
            {
                $loginId = $info['loginId'];
                $xUser = $xUser->GetUserFromLoginID($loginId);
                unset($info['loginId']);
                $info['userId'] = $xUser->userId;
            }

            $xExpense = $xExpense->GetExpense($info);

            //move_uploaded_file($putData['tmp_name'], "/tmp/newfile.file");

            $type = pathinfo($putData['tmp_name'], PATHINFO_EXTENSION);
            $type = "jpeg";
            $data = file_get_contents($putData['tmp_name']);

            $xExpense->receipt = $data;

            $resp = $xExpense->ApplyReceipt($type);

            $jsonstr = array();

            if ($resp)
            {
                $jsonstr = array('response' => 'success');
            }
            else
            {
                $jsonstr = array('response' => 'failure');
            }

            $this->response($this->json($jsonstr),200);
        }

        private function FQ_GetSpots()
        {
            $xFQ = new FoursquareController;
            
            if ($this->get_request_method() != "POST")
            {
                $this->response('',406);
            }
            
            $array1 = $this->_request;
            $results = $xFQ->FindSpot($array1);

            $jsonstr = array('status' => "success", 'response' => $results);
            $this->response($this->json($jsonstr),200);
        }

//==========================================DO NOT EDIT BELOW=======================================

        /*
         *  Encode array into JSON
        */
        private function json($data)
        {
            if(is_array($data))
            {
                return json_encode($data);
            }
        }

        private function AuthRequired($info)
        {
            $xUser = new UserController;

            //check authentication; assign user; change password.
            if (isset($info['loginId']))
            {   
                if (isset($info['AWT_AUTH']) && isset($info['AWT_AUTH_CHALLENGE']))
                {   
                    $userIdentifier = $info['AWT_AUTH'];
                    $challenge      = $info['AWT_AUTH_CHALLENGE'];

                    $good = $xUser->ValidateAuthenticationChallange($info['loginId'], $userIdentifier, $challenge);

                    if ($good == false)
                    {   
                        $x = array('response' => 'failure', 'message' => 'auth required');
                        $this->response($this->json($x), 200);
                        exit;
                    }   
                    else
                    {   
                        //succesful hash
                        $authenticated['loginId'] = $info['loginId'];
                        $authenticated['userId']  = $xUser->GetUserFromLoginID($info['loginId']);
                    
                        return $authenticated;
                    }   
                }   
                else
                {   
                    //fail
                    $x = array('response' => 'failure', 'message' => 'auth required');
                    $this->response($this->json($x), 401);
                    exit;
                }   
            }

        }
    }
    
    // Initiiate Library if rest call

    if ($_SERVER['PHP_SELF'] == '/api.php')
    {
        $api = new API;
        $api->processApi();
    }

    function callAPIFunction($name, $args)
    {
        $tAPI = new API();
        $ref = new ReflectionClass($tAPI);
        $refMethod = $ref->getMethod($name);
        $refMethod->setAccessible(true);

        return $refMethod->invoke($tAPI, $args);
    }
    

    function xhandler($number,$string,$file,$line,$context)
    {
        //log to text file?

        //log to xml file?

        //store in database?

        //whatever you want to do!


        $stuff = "number: " . $number . " string: " . $string . " file: " . $file . " line: " . $line . " context: " . $context . "\n";

        file_put_contents ('/tmp/phplogtest.txt', $stuff, FILE_APPEND | LOCK_EX);
    }

    set_error_handler('xhandler',E_ALL);

?>
