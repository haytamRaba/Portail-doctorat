package com.doctorat.suividoctorat.repository;

import com.doctorat.suividoctorat.entity.Training;
import com.doctorat.suividoctorat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findByDoctorant(User doctorant);

    @Query("SELECT SUM(t.hours) FROM Training t WHERE t.doctorant = :doctorant")
    Integer sumHoursByDoctorant(@Param("doctorant") User doctorant);
}