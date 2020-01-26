<?php

    require_once('/home/awayteam/api/pub/apiconfig.php');
    require_once('/home/awayteam/api/pub/models/TeamEvent.php');
    require_once('/home/awayteam/api/pub/models/Team.php');
    
    class TeamEventXUnitTest extends PHPUnit_Framework_TestCase 
    {
        var $teamEventId;
        var $teamEvent;
        var $team0;
        
        
        public function setUp() {
            dbConnect();
            global $db;
            
            
            $this->team0 = new Team;
            $this->team0->teamName = "team task test team";
            $this->team0->teamLocationName = "san diego";
            $this->team0->teamDescription = "team task test team";
            $this->team0->teamManaged = 0;
            
            $this->teamEvent = new TeamEvent;
            $this->teamEvent->teamEventName = "team event test";
            $this->teamEvent->teamEventDescription = "testing team event";
            $this->teamEvent->teamEventLocationString = "github";
            $this->teamEvent->teamEventStartTime = '2014-6-7 11:30';
            $this->teamEvent->teamEventEndTime = '2013-12-31 12:30';
        }
        
        public function testTeamEventInit() {
            $result = $this->team0->InsertTeam('vuda1');
            
            $teamEvent = new TeamEvent;
            
            $this->assertEquals(-999,$teamEvent->teamEventId);
            $this->assertEquals("", $teamEvent->teamEventName);
            $this->assertEquals("", $teamEvent->teamEventDescription);
            $this->assertEquals("",$teamEvent->teamEventLocationString);
            $this->assertEquals('2013-12-31 0:0',$teamEvent->teamEventStartTime);
            $this->assertEquals('2013-12-31 0:0',$teamEvent->teamEventEndTime);
            $this->assertEquals(-999,$teamEvent->teamEventTeamId);
        }
        
        public function testValidateDateTime0() {
            $result = $this->teamEvent->ValidateDateTIme("2014-6-7 11:30");
            $this->assertTrue($result == true);
        }
        
        public function testValidateDateTime1() {
            $result = $this->teamEvent->ValidateDateTIme("2014-6-32 11:30");
            $this->assertTrue($result == false);
        }
        
        public function testInsertEvent() {    
            $result = $this->team0->SelectTeamFromTeamName("team task test team");
            $this->teamEvent->teamEventTeamId = $result[0]->teamId;
            $this->teamEventId = $this->teamEvent->InsertEvent();
            $this->assertTrue($this->teamEventId > 0);            
        }
        
        public function testSelectAllEvents() {
            $result = $this->teamEvent->SelectAllEvents();
            $this->assertTrue($result != NULL);
        }
        
        public function testSelectEventFromEventName() {
            $result = $this->teamEvent->SelectEventFromEventName($this->teamEvent->teamEventName);
            $this->assertTrue($result != NULL);
        }
        
        public function testSelectEventFromEventId() {
            $teamEvent = $this->teamEvent->SelectEventFromEventName($this->teamEvent->teamEventName);
            $result = $this->teamEvent->SelectEventFromEventId($teamEvent[0]->teamEventId);
            $this->assertTrue($result != NULL);
        }
        
        public function testModifyEvent() {
            $teamEvent = $this->teamEvent->SelectEventFromEventName($this->teamEvent->teamEventName);
            $this->teamEvent->teamEventName = "testing modify event";
            $this->teamEvent->teamEventDescription = "testing modify event description";
            $this->teamEvent->teamEventId = $teamEvent[0]->teamEventId;
            $result = $this->teamEvent->ModifyEvent();
            $this->assertTrue($result != NULL);
        }
        
        public function testModifyEventName() {
            $teamEvent = $this->teamEvent->SelectEventFromEventName("testing modify event");
            $result = $this->teamEvent->ModifyEventName($teamEvent[0]->teamEventId, "testing modify event");
            $this->assertTrue($result != NULL);            
        }
        
        public function testDeleteEvent() {            
            $teamEvent = $this->teamEvent->SelectEventFromEventName("testing modify event");
            $team = $this->team0->SelectTeamFromTeamName("team task test team");
            $result = $this->teamEvent->DeleteEvent($teamEvent[0]->teamEventId);
            $this->assertTrue($result != NULL);
            $this->team0->DeleteTeam($team[0]->teamId);
        }
            
    }
    
    
?>