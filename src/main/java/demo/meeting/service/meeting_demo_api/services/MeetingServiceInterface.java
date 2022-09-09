package demo.meeting.service.meeting_demo_api.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.lang.Nullable;

import demo.meeting.service.meeting_demo_api.Meeting;
import demo.meeting.service.meeting_demo_api.MeetingCategory;
import demo.meeting.service.meeting_demo_api.MeetingType;

public interface MeetingServiceInterface {
    public String addParticipant(String meeting, String participant, LocalDateTime joinTime);

    public String addParticipant(String meeting, String participant, Duration joinDelay);

    public String addParticipant(String meeting, String participant);

    public boolean isOrganizer(String meeting, String participant);

    public String createOrRescheduleMeeting(String meeting, String organizer, MeetingType type, MeetingCategory category, LocalDateTime start, LocalDateTime end, String description);

    public void removeMeeting(String meeting);

    public String removeParticipant(String meeting, String participant);

    public Map<String,Meeting> filterByContainsDescription(Map<String,Meeting> meetings, String description);

    public Map<String,Meeting> filterByOrganizer(Map<String,Meeting> meetings, String organizer);

    public Map<String,Meeting> filterByCategory(Map<String,Meeting> meetings, MeetingCategory category);

    public Map<String,Meeting> filterByMeetingType(Map<String,Meeting> meetings, MeetingType type);

    public Map<String,Meeting> filterByTimeRange(Map<String,Meeting> meetings, @Nullable LocalDateTime from, @Nullable LocalDateTime to);

    public Map<String,Meeting> filterByNumberOfAttendees(Map<String,Meeting> meetings, @Nullable int from, @Nullable int to);

}
