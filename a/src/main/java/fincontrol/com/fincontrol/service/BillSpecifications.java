package fincontrol.com.fincontrol.service; // Or a 'repository.specification' package

import fincontrol.com.fincontrol.model.Bill;
import fincontrol.com.fincontrol.model.Expense;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.model.enums.BillStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class BillSpecifications {

    public static Specification<Bill> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Bill> hasStatus(BillStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Bill> hasBankId(UUID bankId) {
        return (root, query, criteriaBuilder) -> {
            Join<Bill, Bank> bankJoin = root.join("bank", JoinType.LEFT); // LEFT JOIN for optional bank
            return criteriaBuilder.equal(bankJoin.get("id"), bankId);
        };
    }

    public static Specification<Bill> hasExpenseCategoryId(UUID categoryId) {
        return (root, query, criteriaBuilder) -> {
            Join<Bill, Expense> expenseJoin = root.join("expense");
            Join<Expense, Category> categoryJoin = expenseJoin.join("category");
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }
}
