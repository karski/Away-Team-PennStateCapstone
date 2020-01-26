<?php
    require_once('/home/awayteam/api/pub/apiconfig.php');
    require_once('/home/awayteam/api/pub/models/TeamTasks.php');
    require_once('/home/awayteam/api/pub/models/Team.php');
    
    class TeamTasksXUnitTest extends PHPUnit_Framework_TestCase
    {
        var $teamTask0;
        var $teamId;
        var $team0;
        var $teamTaskId;
        
        public function setUp() {
            dbConnect();
            global $db;
            
            $this->team0 = new Team;
            $this->team0->teamName = "team task test team";
            $this->team0->teamLocationName = "san diego";
            $this->team0->teamDescription = "team task test team";
            $this->team0->teamManaged = 0;
            
            $this->teamTask0 = new TeamTasks;
            $this->teamTask0->taskTitle = "unit test create task";
            $this->teamTask0->taskDescription = "unit testing team task class";
            $this->teamTask0->taskCompleted = false;
            $this->teamTask0->taskTeamId = $this->teamId;
        }
        
        public function testTeamInit(){        
            $result = $this->team0->InsertTeam('vuda1');
            $this->team0Id = $result['teamId'];
            $teamTasks = new TeamTasks;
            
            $this->assertEquals(-999, $teamTasks->taskId);
            $this->assertEquals("", $teamTasks->taskTitle);
            $this->assertEquals("", $teamTasks->taskDescription);
            $this->assertEquals("", $teamTasks->taskCompleted);
            $this->assertEquals(-999,$teamTasks->taskTeamId);
        }
        
        public function testInsertTeamTasks() {            
            $this->teamTaskId = $this->teamTask0->InsertTeamTask();
            $this->assertTrue($this->teamTaskId> 0);
        }
        
        public function testSelectTeamTasks() {
            $team = $this->team0->SelectTeamFromTeamName($this->team0->teamName);
            $result = $this->teamTask0->SelectTeamTasks($team[0]->teamId);
            $this->assertNotNull($result);
        }    
        
        public function testModifyTeamTask() {            
            $this->teamTask0->taskTitle = "unit testing modify team task";
            $this->teamTask0->taskDescription = "unit testing modify team task";
            $result = $this->teamTask0->ModifyTeamTask();
            $this->assertTrue($result != NULL);
        }
        
        public function testMarkTeamTaskComplete() {
            $team = $this->team0->SelectTeamFromTeamName($this->team0->teamName);
            $teamTasks = $this->teamTask0->SelectTeamTasks($team[0]->teamId);            
            $result = $this->teamTask0->MarkTeamTaskComplete($teamTasks->taskTeamId,"true");
            $this->assertTrue($result != NULL);
        }
        
        public function testDeleteTeamTask() {  
            $team = $this->team0->SelectTeamFromTeamName($this->team0->teamName);
            $teamTasks = $this->teamTask0->SelectTeamTasks($team[0]->teamId);  
            $result = $this->teamTask0->DeleteTeamTask($teamTasks->taskTeamId);
            $this->assertNotNull($result);
        }
        
   
    }
?>