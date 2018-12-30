package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.Training;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    Training findTopById(Long id);
}