<?php
    require_once('/home/awayteam/api/pub/apiconfig.php');
    require_once('/home/awayteam/api/pub/models/Team.php');
    
    class TeamXUnitTest extends PHPUnit_Framework_TestCase
    {
        var $team0;
        var $team1;
        var $userId;
        
        public function setUp() {
            dbConnect();
            global $db;
            
            $this->team0 = new Team;
            $this->team0->teamName = "saints";
            $this->team0->teamDescription = "who dat going to beat those saints";
            $this->team0->teamLocationName = "la jolla";
            $this->team0->teamManaged = "false";
            
            $this->team1 = new Team;
            $this->team1->teamName = "49ers";
            $this->team1->teamDescription = "chase for six";
            $this->team1->teamLocationName = "santa clara";
            $this->team1->teamManaged = "true";
            
            $this->teamIdList = array();
            
            $query = "select userId from user where loginId = 'vuda1'";
            $sql = mysql_query($query,$db);
            while($rlt = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                $this->userId = $rlt['userId'];
            }
        }
        
        public function testTeamInit(){
            $team = new Team;
            
            $this->assertEquals(-999, $team->teamId);
            $this->assertEquals("", $team->teamName);
            $this->assertEquals("", $team->teamLocationName);
            $this->assertEquals("", $team->teamDescription);
            $this->assertEquals(false,$team->teamManaged);
        }
        
        public function testInsertUnmanagedTeam() {            
            $teamId = $this->team0->InsertTeam('vuda1');
            $this->assertTrue($teamId> 0);
        }
        
        public function testInsertManagedTeam() {            
            $teamId = $this->team1->InsertTeam('vuda1');   
            $this->assertTrue($teamId> 0);
        }
        
        public function testSelectAllTeams() {
            $team = new Team;
            $result = $this->team0->SelectAllTeams();
            $this->assertNotEmpty($result);
        }
        
        public function testSelectTeamFromTeamName() {
            $team = new Team;
            $result = NULL;           
            $result =$this->team1->SelectTeamFromTeamName("49ers");
            $this->assertNotNull($result);
        }
        
        public function testSelectTeamFromId() {
            $team = new Team;            
            $result = NULL;
            $team = $this->team1->SelectTeamFromTeamName("49ers");
            $teamId = $team[0]->teamId;
            $result = $this->team1->SelectTeamFromId($teamId,'vuda1');
            $this->assertNotNull($result);
        }
        
        public function testGetTeamList() {        
            $team = new Team;
            $result = NULL;            
            $result = $this->team0->GetTeamList($this->userId);
            $this->assertNotNull($result);
        }
        
        public function testModifyTeamModel() {            
            $this->team0->teamName = "saints1";
            $this->team0->teamLocationName = "New Orleans";
            $this->team0->teamDescription = "moved to New Orleans";
            $this->team0->teamManaged = 0;
            $result = NULL;
            $result = $this->team0->ModifyTeamModel("vuda1");
            $this->assertNotNull($result);
        }
        
        public function testModifyTeamNameModel() {
            $team = new Team;
            $result = NULL;
            $team = $this->team1->SelectTeamFromTeamName("Saints");
            $teamId = $team[0]->teamId;
            $result = $this->team1->ModifyTeamNameModel($teamId,"baylor");
            $this->assertNotNull($result);
        }

        public function testDeleteTeam() {
            $team = new Team;
            $result = NULL;
            
            $team = $this->team0->SelectTeamFromTeamName("baylor");
            $team0Id = $team[0]->teamId;            
            $result = $this->team0->DeleteTeam($team0Id);
            $this->assertTrue($result);
            
            $team = $this->team1->SelectTeamFromTeamName("49ers");
            $team1Id = $team[0]->teamId;
            $result = $this->team1->DeleteTeam($team1Id);
            $this->assertTrue($result);
        }
    }
?>
