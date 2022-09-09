package demo.meeting.service.meeting_demo_api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import demo.meeting.service.meeting_demo_api.TimeRange;

public class Participant {
    private String name;
    private Map<String, TimeRange> events;

    public static String asID(String name){
        return name.strip().toLowerCase();
    }

    public Participant(String name){
        this.name = name;
        events = new HashMap<String,TimeRange>();
    }

    public Participant(String name, String eventName, LocalDateTime start, LocalDateTime end){
        this.name = name;
        events = new HashMap<String,TimeRange>();
        events.put(eventName, new TimeRange(start, end));
    }

    public boolean addOrRescheduleEvent(String eventName, LocalDateTime start, LocalDateTime end){
       TimeRange eventTime = new TimeRange(start, end);
       boolean nonconcurrent = true;
       if(events.containsKey(eventName)){
        for(Map.Entry<String, TimeRange> entry : events.entrySet()) {
            String name = entry.getKey();
            if (name != eventName){
                TimeRange r = entry.getValue();
                if (r.intersects(eventTime)){
                    nonconcurrent = false;
                    break;
                }
            }
        }
       }else{
            for (TimeRange r : events.values()){
                if (r.intersects(eventTime)){
                    nonconcurrent = false;
                    break;
                }
            }
       }
       events.put(eventName, eventTime);
       return nonconcurrent;
    }

    public boolean participates(String eventID){
        if (events.containsKey(eventID)){
            return true;
        }else{
            return false;
        }
    }

    public void removeEvent(String eventName){
        if (events.containsKey(eventName)){
            events.remove(eventName);
        }
    }

    public boolean isParticipating(){
        if (events.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public String getName(){
        return name;
    }

}
