package demo.meeting.service.meeting_demo_api.request_entities;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table()
public class ParticipantEntity {
    @Id
    @Column(nullable = false)
    public String participant;
    
    @Column(nullable = true)
    public LocalDateTime joinTime;

    @Column(nullable = true)
    public Duration joinDelay;

    public ParticipantEntity() {}
}
