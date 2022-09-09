package demo.meeting.service.meeting_demo_api.controllers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;

import demo.meeting.service.meeting_demo_api.Meeting;
import demo.meeting.service.meeting_demo_api.MeetingCategory;
import demo.meeting.service.meeting_demo_api.MeetingType;
import demo.meeting.service.meeting_demo_api.request_entities.MeetingEntity;
import demo.meeting.service.meeting_demo_api.request_entities.ParticipantEntity;
import demo.meeting.service.meeting_demo_api.services.MeetingManager;

@RestController
@RequestMapping(value="/meeting")
public class MeetingController {

    private static String buildSimpleStatusResponse(String status, String info){
        if (info.length() != 0){
            return String.format("{\"status\":\"%s\",\"message\":\"%s\"}", status, info);
        }
        return  String.format("{\"status\":\"%s\"}", status);
    }

    @Resource(name = "getMeetingManager")
    public MeetingManager meetingManager;

    @RequestMapping(value="",
                    method=RequestMethod.POST,
                    consumes = {"application/json"},
                    produces = {"application/json"})
    public ResponseEntity<String> createMeeting(@RequestBody MeetingEntity meeting) {
        String info = meetingManager.createOrRescheduleMeeting(meeting.name, meeting.organizer, meeting.type, meeting.category, meeting.start, meeting.end, meeting.description);
        ResponseEntity<String> response = new ResponseEntity<String>(buildSimpleStatusResponse("OK", info), HttpStatus.CREATED);
        return response;
    }

    @RequestMapping(value="/add/{meeting_id}",
            method=RequestMethod.POST,
            consumes = {"application/json"},
            produces = {"application/json"})
    public ResponseEntity<String> addParticipant(@PathVariable("meeting_id") String meeting_id, @RequestBody ParticipantEntity participant){
        String info;
        if (participant.joinTime != null){
            info = meetingManager.addParticipant(meeting_id, participant.participant, participant.joinTime);
        }else if (participant.joinDelay != null){
            info = meetingManager.addParticipant(meeting_id, participant.participant, participant.joinDelay);
        }else{
            info = meetingManager.addParticipant(meeting_id, participant.participant);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(buildSimpleStatusResponse("ok",info));
    }

    @RequestMapping(value = "/remove/{meeting_id}/{participant_id}",
            method = RequestMethod.DELETE,
            produces = {"application/json"})
    public ResponseEntity<String> deleteParticipant(@PathVariable("meeting_id") String meeting_id, @PathVariable("participant_id") String participant_id){
        String info = meetingManager.removeParticipant(meeting_id, participant_id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(buildSimpleStatusResponse("ok", info));
    }

    @RequestMapping(value = "/delete/{id}",
            method = RequestMethod.DELETE,
            produces = {"application/json"})
    public ResponseEntity<String> deleteMeeting(@RequestHeader(name="Authorization", required = false) String token, @PathVariable("id") String id){
        if (token != null && token.contains("Basic ")){
            token = token.replaceFirst("Basic ", "");
            String auth = new String(Base64Utils.decode(token.getBytes(StandardCharsets.US_ASCII)), StandardCharsets.UTF_8);
            String user = auth.split(":")[0];
            if (meetingManager.isOrganizer(id, user)){
                meetingManager.removeMeeting(id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(buildSimpleStatusResponse("ok", "Delete meeting request completed successfully"));
            }else{
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildSimpleStatusResponse("error", "Only the organizer is allowed to delete this meeting"));
            }
            
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildSimpleStatusResponse("error", "Only the organizer is allowed to delete this meeting"));
    }

    @RequestMapping(value = "/filter",
                    method = RequestMethod.GET,
                    produces = {"application/json"})
    public ResponseEntity<String> filterMeetings(
        @RequestParam(name = "type", required = false) MeetingType type,
        @RequestParam(name = "category", required = false) MeetingCategory category,
        @RequestParam(name ="description", required = false) String description,
        @RequestParam(name ="organizer", required = false) String organizer,
        @RequestParam(name ="attendees_from", required = false) Integer attendees_from,
        @RequestParam(name ="attendees_to", required = false) Integer attendees_to,
        @RequestParam(name ="time_from", required = false) String time_from,
        @RequestParam(name ="time_to", required = false) String time_to ){
            Map<String, Meeting> meetings = meetingManager.getMeetings();
            if (type != null){
                meetings = meetingManager.filterByMeetingType(meetings, type);
            }
            if (category != null){
                meetings = meetingManager.filterByCategory(meetings, category);
            }
            if (description != null){
                meetings = meetingManager.filterByContainsDescription(meetings, description);
            }
            if (organizer != null){
                meetings = meetingManager.filterByOrganizer(meetings, organizer);
            }
            if (attendees_from != null && attendees_to != null){
                meetings = meetingManager.filterByNumberOfAttendees(meetings, attendees_from, attendees_to);
            }
            if (time_from != null && time_to != null){
                LocalDateTime from = null, to = null;
                if (time_from != null){
                    from = LocalDateTime.parse(time_from);
                }
                if (time_to != null){
                    to = LocalDateTime.parse(time_to);
                }
                meetings = meetingManager.filterByTimeRange(meetings, from, to);
            }
            return ResponseEntity.ok().body(MeetingManager.serializer.toJson(meetings.values()));
    }

    public void close(){
        meetingManager.close();
    }
}
