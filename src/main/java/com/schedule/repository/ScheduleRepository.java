package com.schedule.repository;

import com.schedule.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository <Schedule,Long>{
    @Query("SELECT FUNCTION('DATE', s.createdAt) as date, COUNT(s) " +
            "FROM Schedule s " +
            "GROUP BY FUNCTION('DATE', s.createdAt) " +
            "ORDER BY FUNCTION('DATE', s.createdAt) ASC")
    List<Object[]> countScheduleByCreatedDate();

}
