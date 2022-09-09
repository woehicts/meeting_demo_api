package demo.meeting.service.meeting_demo_api.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import java.util.logging.Logger;

import org.springframework.lang.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;

import demo.meeting.service.meeting_demo_api.Meeting;
import demo.meeting.service.meeting_demo_api.MeetingCategory;
import demo.meeting.service.meeting_demo_api.MeetingType;
import demo.meeting.service.meeting_demo_api.Participant;
import demo.meeting.service.meeting_demo_api.TimeRange;
import demo.meeting.service.meeting_demo_api.json_adapters.LocalDateTimeAdapter;

public class MeetingManager implements MeetingServiceInterface {
    private Map<String,Meeting> meetings;
    private Map<String,Participant> participants;

    private static final Logger logger = java.util.logging.Logger.getLogger("MeetingManager");

    public static Gson serializer;

    public Map<String,Meeting> getMeetings(){
        return meetings;
    }

    public Map<String,Participant> getParticipants(){
        return participants;
    }

    static {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()); 
        serializer = builder.create();
    }
    

    public void init(){
        MeetingManager m = MeetingManager.fromJsonFile("data.json");
        this.meetings = m.getMeetings();
        this.participants = m.getParticipants();
    }

    public static MeetingManager fromJsonFile(String path){
        logger.info("Building MeetingManager instance");
        var fileObj = new File(System.getProperty("user.dir"),path);
        if (fileObj.exists()){
            try{
                return serializer.fromJson(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir"),path)), StandardCharsets.UTF_8), MeetingManager.class);
            }catch(IOException e){
                logger.info("Loading session data failed with exception: " + e.getMessage());
            }
        }
        return new MeetingManager();
        
    }

    public void close(){
        this.toJsonFile("data.json");
    }

    public void toJsonFile(String path){
        System.out.println("Writing MeetingManager instance");
        File file = new File(System.getProperty("user.dir"), path);
        try(Writer writer = new FileWriter(file)){
            JsonWriter jsonWriter = new JsonWriter(writer);
            serializer.toJson(this, MeetingManager.class, jsonWriter);
        }catch(IOException | JsonIOException e){
            System.out.println("Storing session data failed with exception: " + e.getMessage());
        }
    }

    public MeetingManager(){
        meetings = new HashMap<String,Meeting>();
        participants = new HashMap<String, Participant>();
    }

    @Override
    public String addParticipant(String meeting, String participant, LocalDateTime joinTime){
        String meetingID = Meeting.asID(meeting);
        String participantID = Participant.asID(participant);
        if (meetings.containsKey(meetingID)){
            Meeting m = meetings.get(meetingID);
            boolean participantAdded = m.addParticipant(participantID, joinTime);
            if (participantAdded){
                return assignEventForParticipant(participant, meeting, joinTime, m.getEnding());
            }
            
        }
        return "";
    }

    @Override
    public String addParticipant(String meeting, String participant, Duration meetingDelay){
        String meetingID = Meeting.asID(meeting);
        if (meetings.containsKey(meetingID)){
            Meeting m = meetings.get(meetingID);
            LocalDateTime start = m.getBeginning();
            LocalDateTime timeJoins = LocalDateTime.ofEpochSecond(
                                                        start.toEpochSecond(ZoneOffset.of("Z"))+meetingDelay.getSeconds(),
                                                        start.getNano() + meetingDelay.getNano(), 
                                                        ZoneOffset.of("Z")
                                                    );
            return this.addParticipant(meeting, participant, timeJoins);
        }
        return "";
    }

    @Override
    public String addParticipant(String meeting, String participant){
        return this.addParticipant(meeting, participant, Duration.ofSeconds(0));
    }

    @Override
    public String removeParticipant(String meeting, String participant){
        String meetingID = Meeting.asID(meeting);
        String participantID = Participant.asID(participant);
        removeEventForParticipant(participantID, meetingID);
        String info;
        if (meetings.containsKey(meetingID)){
            Meeting m = meetings.get(meetingID);
            info = m.removeParticipant(participantID);
        }else{
            info = "Meeting '"+meeting+"' has not been registered on the system yet";
        }
        return info;
    }

    @Override
    public boolean isOrganizer(String meeting, String participant) {
        String meetingID = Meeting.asID(meeting);
        if (meetings.containsKey(meetingID)){
            String participantID = Participant.asID(participant);
            Meeting m = meetings.get(meetingID);
            return m.getOrganizerID().equalsIgnoreCase(participantID);
        }
        return false;
    }

    @Override
    public void removeMeeting(String meeting){
        String meetingID = Meeting.asID(meeting);
        if (meetings.containsKey(meetingID)){
            Meeting m = meetings.get(meetingID);
            String organizerID = m.getOrganizerID();
            removeEventForParticipant(organizerID, meetingID);
            for (String participantID : m.getSetOfParticipants()){
                removeEventForParticipant(participantID, meetingID);
            }
            meetings.remove(meetingID);
        }
    }

    @Override
    public String createOrRescheduleMeeting(String meeting, String organizer, MeetingType type, MeetingCategory category, LocalDateTime start, LocalDateTime end, String description){
        String meetingID = Meeting.asID(meeting);
        String organizerID = Participant.asID(organizer);
        String participantStatusString = "";
        if (meetings.containsKey(meetingID)){
            Meeting m = meetings.get(meetingID);
            if (m.getOrganizerID() != organizerID){
                removeEventForParticipant(organizerID, meetingID);
                m.setOrganizerID(organizerID);
                participantStatusString = assignEventForParticipant(organizer, meeting, start, end);
            }
            m.setCategory(category);
            m.setBeginning(start);
            m.setEnding(end);
            m.setType(type);
            m.setDescription(description);
            if (participantStatusString.length() == 0){
                participantStatusString = "Meeting '" + meeting + "' was successfully updated";
            }
        }else{
            Meeting m = new Meeting(meeting, organizerID, category, type, start, end, description);
            meetings.put(meetingID, m);
            participantStatusString = assignEventForParticipant(organizer, meeting, start, end);
            if (participantStatusString.length() == 0){
                participantStatusString = "Meeting '" + meeting + "' was successfully created";
            }
        }
        return participantStatusString;
    }

    @Override
    public Map<String,Meeting> filterByContainsDescription(Map<String,Meeting> meetings, String description){
        Map<String,Meeting> result = new HashMap<String,Meeting>();
        for(Map.Entry<String, Meeting> entry : meetings.entrySet()) {
            Meeting m = entry.getValue();
            Pattern text = Pattern.compile(description, Pattern.CASE_INSENSITIVE);
            if (m.containsDescription(text)){
                result.put(entry.getKey(), m);
            }
        }
        return result;
    }

    @Override
    public Map<String,Meeting> filterByOrganizer(Map<String,Meeting> meetings, String organizer){
        Map<String,Meeting> result = new HashMap<String,Meeting>();
        for(Map.Entry<String, Meeting> entry : meetings.entrySet()) {
            Meeting m = entry.getValue();
            String id = organizer.strip().toLowerCase();
            if (id == m.getOrganizerID()){
                result.put(entry.getKey(), m);
            }
        }
        return result;
    }

    @Override
    public Map<String,Meeting> filterByCategory(Map<String,Meeting> meetings, MeetingCategory category){
        Map<String,Meeting> result = new HashMap<String,Meeting>();
        for(Map.Entry<String, Meeting> entry : meetings.entrySet()) {
            Meeting m = entry.getValue();
            if (m.getCategory() == category){
                result.put(entry.getKey(), m);
            }
        }
        return result;
    }

    @Override
    public Map<String,Meeting> filterByMeetingType(Map<String,Meeting> meetings, MeetingType type){
        Map<String,Meeting> result = new HashMap<String,Meeting>();
        for(Map.Entry<String, Meeting> entry : meetings.entrySet()) {
            Meeting m = entry.getValue();
            if (m.getType() == type){
                result.put(entry.getKey(), m);
            }
        }
        return result;
    }

    @Override
    public Map<String,Meeting> filterByTimeRange(Map<String,Meeting> meetings, @Nullable LocalDateTime from, @Nullable LocalDateTime to){
        Map<String,Meeting> result = new HashMap<String,Meeting>();
        LocalDateTime from_ = Optional.ofNullable(from).orElse(LocalDateTime.MIN);
        LocalDateTime to_ = Optional.ofNullable(to).orElse(LocalDateTime.MAX);
        TimeRange filterRange = new TimeRange(from_, to_);
        for(Map.Entry<String, Meeting> entry : meetings.entrySet()) {
            Meeting m = entry.getValue();
            TimeRange r = new TimeRange(m.getBeginning(), m.getEnding());
            if (r.intersects(filterRange)){
                result.put(entry.getKey(), m);
            }
        }
        return result;
    }

    @Override
    public Map<String,Meeting> filterByNumberOfAttendees(Map<String,Meeting> meetings, @Nullable int from, @Nullable int to){
        Map<String,Meeting> result = new HashMap<String,Meeting>();
        int from_ = Optional.ofNullable(from).orElse(0);
        int to_ = Optional.ofNullable(to).orElse(Integer.MAX_VALUE);
        for(Map.Entry<String, Meeting> entry : meetings.entrySet()) {
            Meeting m = entry.getValue();
            int N = m.getNumberOfParticipants();
            if (N >= from_ && N <= to_){
                result.put(entry.getKey(), m);
            }
        }
        return result;
    }

    private void removeEventForParticipant(String participantID, String eventID){
        if (participants.containsKey(participantID)){
            Participant p = participants.get(participantID);
            if (p.participates(eventID)){
                p.removeEvent(eventID);
            }
            if (!p.isParticipating()){
                participants.remove(participantID);
            }
        }
    }

    private String assignEventForParticipant(String participant, String event, LocalDateTime start, LocalDateTime end){
        String eventID = Meeting.asID(event);
        String participantID = Participant.asID(participant);
        if (participants.containsKey(participantID)){
            Participant p = participants.get(participantID);
            boolean overlappingEvent = p.addOrRescheduleEvent(eventID, start, end);
            if (overlappingEvent){
                return "Warning: " + participant + " participates in '" + event + "' and one more events at the same time.";
            }
        }else{
            Participant p = new Participant(participant);
            participants.put(participantID, p);
            p.addOrRescheduleEvent(eventID, start, end);
        }
        return "";
    }

}
