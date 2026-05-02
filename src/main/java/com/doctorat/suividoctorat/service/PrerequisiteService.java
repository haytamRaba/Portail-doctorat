package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.dto.ProgressDTO;
import com.doctorat.suividoctorat.entity.Publication;
import com.doctorat.suividoctorat.entity.Training;
import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.repository.PublicationRepository;
import com.doctorat.suividoctorat.repository.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrerequisiteService {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    private final int REQUIRED_ARTICLES = 2;
    private final int REQUIRED_CONFERENCES = 2;
    private final int REQUIRED_TRAINING_HOURS = 200;

    public ProgressDTO calculateProgress(User doctorant) {
        // Count articles (Q1/Q2 only)
        int articleCount = publicationRepository.countQ1Q2ArticlesByDoctorant(doctorant);

        // Count conferences
        int conferenceCount = publicationRepository.countConferencesByDoctorant(doctorant);

        // Sum training hours
        Integer trainingHours = trainingRepository.sumHoursByDoctorant(doctorant);
        int totalTrainingHours = (trainingHours != null) ? trainingHours : 0;

        // Check requirements
        boolean articlesMet = articleCount >= REQUIRED_ARTICLES;
        boolean conferencesMet = conferenceCount >= REQUIRED_CONFERENCES;
        boolean trainingMet = totalTrainingHours >= REQUIRED_TRAINING_HOURS;
        boolean readyForDefense = articlesMet && conferencesMet && trainingMet;

        return new ProgressDTO(articleCount, conferenceCount, totalTrainingHours,
                articlesMet, conferencesMet, trainingMet, readyForDefense);
    }

    public Publication addPublication(Publication publication) {
        return publicationRepository.save(publication);
    }

    public Training addTraining(Training training) {
        return trainingRepository.save(training);
    }

    public List<Publication> getPublicationsByDoctorant(User doctorant) {
        return publicationRepository.findByDoctorant(doctorant);
    }

    public List<Training> getTrainingsByDoctorant(User doctorant) {
        return trainingRepository.findByDoctorant(doctorant);
    }

    public void deletePublication(Long id) {
        publicationRepository.deleteById(id);
    }

    public void deleteTraining(Long id) {
        trainingRepository.deleteById(id);
    }
}