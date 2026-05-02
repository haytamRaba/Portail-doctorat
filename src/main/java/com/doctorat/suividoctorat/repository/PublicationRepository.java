package com.doctorat.suividoctorat.repository;

import com.doctorat.suividoctorat.entity.Publication;
import com.doctorat.suividoctorat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    List<Publication> findByDoctorant(User doctorant);

    List<Publication> findByDoctorantAndType(User doctorant, String type);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.doctorant = :doctorant AND p.type = 'ARTICLE' AND p.quartile IN ('Q1', 'Q2')")
    int countQ1Q2ArticlesByDoctorant(@Param("doctorant") User doctorant);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.doctorant = :doctorant AND p.type = 'CONFERENCE'")
    int countConferencesByDoctorant(@Param("doctorant") User doctorant);
}