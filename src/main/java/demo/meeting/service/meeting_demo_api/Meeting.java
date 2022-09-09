package demo.meeting.service.meeting_demo_api;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Meeting {

    private String name;

    private String organizer;

    private String description;

    private MeetingCategory category;

    private MeetingType type;

    private LocalDateTime start;

    private LocalDateTime end;

    private Map<String,LocalDateTime> participants;


    public static String asID(String name){
        return name.strip().toLowerCase();
    }

    public Meeting(String name, String organizerID, MeetingCategory category, MeetingType type, LocalDateTime start, LocalDateTime end, String description){
        if (start.isAfter(end)){
            throw new DateTimeException("Attempted to schedule a meeting past its end");
        }
        this.name = name.strip();
        this.organizer = organizerID;
        this.type = type;
        this.description = description;
        this.category = category;
        this.start = start;
        this.end = end;
        participants = new HashMap<String, LocalDateTime>();
    }

    public Meeting(String name, String organizer, MeetingCategory category, MeetingType type, LocalDateTime start, LocalDateTime end){
        this(name, organizer, category, type, start, end, "");
    }

    public String getOrganizerID(){
        return organizer;
    }

    public void setName(String name){
        this.name = name.strip();
    }

    public String getName(){
        return name;
    }

    public void setOrganizerID(String organizerID){
        this.organizer = organizerID;
    }

    public void addParticipant(String participantID, Duration meetingDelay){
        if (participantID != this.getOrganizerID()){

            LocalDateTime timeJoins = LocalDateTime.ofEpochSecond(
                                                                start.toEpochSecond(ZoneOffset.of("Z"))+meetingDelay.getSeconds(),
                                                                start.getNano() + meetingDelay.getNano(), 
                                                                ZoneOffset.of("Z")
                                                    );
            if (timeJoins.isBefore(end)){
                participants.put(participantID, timeJoins);
            }
        }
    }

    public LocalDateTime getBeginning(){
        return start;
    }

    public void setBeginning(LocalDateTime start){
        this.start = start;
    }

    public LocalDateTime getEnding(){
        return end;
    }

    public void setEnding(LocalDateTime end){
        this.end = end;
    }

    public MeetingType getType(){
        return type;
    }

    public void setType(MeetingType type){
        this.type = type;
    }

    public boolean containsDescription(Pattern text){
        Matcher matcher= text.matcher(description);
        return matcher.matches();
    }

    public MeetingCategory getCategory(){
        return category;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public void setCategory(MeetingCategory category){
        this.category = category;
    }

    public int getNumberOfParticipants(){
        return participants.size() + 1;
    }

    public boolean addParticipant(String participantID, LocalDateTime timeJoins){
        if (participantID != this.getOrganizerID() && !participants.containsKey(participantID)){
            if (timeJoins.isBefore(end)){
                participants.put(participantID, timeJoins);
                return true;
            }
        }
        return false;
    }

    public Set<String> getSetOfParticipants(){
        return participants.keySet();
    }

    public String removeParticipant(String participantID){
        String info;
        if (participants.containsKey(participantID)){
            participants.remove(participantID);
            info = "Participant '" + participantID + "' was successfully removed from a meeting.";
        }else{
            info = "Participant '" + participantID + "' is either an organizer or not in a list of members of this meeting.";
        }
        return info;
    }
}
