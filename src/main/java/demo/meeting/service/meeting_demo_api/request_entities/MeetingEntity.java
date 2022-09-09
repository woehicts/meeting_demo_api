package demo.meeting.service.meeting_demo_api.request_entities;

import java.time.LocalDateTime;

import javax.persistence.*;

import demo.meeting.service.meeting_demo_api.Meeting;
import demo.meeting.service.meeting_demo_api.MeetingCategory;
import demo.meeting.service.meeting_demo_api.MeetingType;

@Entity
@Table()
public class MeetingEntity {
    @Id
    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String organizer;

    @Column(nullable = true)
    public String description;

    @Column(nullable = false)
    public MeetingCategory category;

    @Column(nullable = false)
    public MeetingType type;

    @Column(nullable = false)
    public LocalDateTime start;

    @Column(nullable = false)
    public LocalDateTime end;

    public MeetingEntity() {}

    public MeetingEntity(Meeting meeting){
        name = meeting.getName();
        organizer = meeting.getOrganizerID();
        description = meeting.getDescription();
        category = meeting.getCategory();
        type = meeting.getType();
        start = meeting.getBeginning();
        end = meeting.getEnding();
    }
}
