package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.ExtraIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExtraIncomeRepository extends JpaRepository<ExtraIncome, Long> {

    List<ExtraIncome> findByUserId(java.util.UUID userId);

}
